###########################
## Genesys Cloud Objects ##
###########################

# Genesys Cloud EventBridge Integration
module "AwsEventBridgeIntegration" {
   source              = "git::https://github.com/GenesysCloudDevOps/aws-event-bridge-module.git?ref=main"
   aws_account_id      = var.aws_account_id
   aws_account_region  = var.aws_region
   event_source_suffix = var.aws_event_bus_name
   topic_filters       = ["v2.detail.events.conversation.{id}.customer.end","v2.detail.events.conversation.{id}.customer.start"]
}

data "aws_cloudwatch_event_source" "genesys_event_bridge" {
  depends_on  = [ module.AwsEventBridgeIntegration ]
  name_prefix = "aws.partner/genesys.com/cloud/${var.genesys_cloud_organization_id}/${var.aws_event_bus_name}"
}

resource "aws_cloudwatch_event_bus" "genesys_audit_event_bridge" {
  name              = data.aws_cloudwatch_event_source.genesys_event_bridge.name
  event_source_name = data.aws_cloudwatch_event_source.genesys_event_bridge.name
}

#################
## AWS Objects ##
#################

# AWS SQS
resource "aws_sqs_queue" "queue" {
  name                        = var.aws_sqs_queue_name
  visibility_timeout_seconds  = 900
  message_retention_seconds   = 1209600
}

# AWS Kinesis Data Stream
resource "aws_kinesis_stream" "stream" {
  name = var.aws_stream_name
  stream_mode_details {
    stream_mode = "ON_DEMAND"
  }
}

# AWS IAM Policy
resource "aws_iam_policy" "policy" {
  name   = var.aws_iam_poicy_name
  policy = jsonencode({
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "stmntServiceReadFromTestKinesisStream",
        "Effect": "Allow",
        "Action": [
          "kinesis:GetShardIterator",
          "kinesis:GetRecords",
          "kinesis:DescribeStream"
        ],
        "Resource": aws_kinesis_stream.stream.arn
      },
      {
        "Sid": "stmntServiceListAllKinesisStreams",
        "Effect": "Allow",
        "Action": "kinesis:ListStreams",
        "Resource": "*"
      }
    ]
  })
}

# AWS IAM User
resource "aws_iam_user" "user" {
  name = var.aws_iam_user_name
}

resource "aws_iam_user_policy_attachment" "test-attach" {
  user       = aws_iam_user.user.name
  policy_arn = aws_iam_policy.policy.arn
}

# AWS Lambda
# Defines the Lambda execution role
data "aws_iam_policy_document" "assume_role" {
  statement {
    effect  = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
    sid     = "LambdaExecutionRole"
  }
}

# Create the Lambda execution role
resource "aws_iam_role" "iam_for_lambda" {
  name               = var.aws_iam_lambda_role_name
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

locals {
  lambda_payload_filename = "${path.module}/../target/genesys-external-streaming-integration.jar"
}

resource "aws_lambda_function" "test_lambda" {
  filename          = local.lambda_payload_filename
  function_name     = var.aws_lambda_function_name
  role              = aws_iam_role.iam_for_lambda.arn
  source_code_hash  = "${base64sha256(filebase64(local.lambda_payload_filename))}"
  runtime           = "java21"
  handler           = "EventBridgeHandler.java"
}

# AWS EventBridge
resource "aws_cloudwatch_event_rule" "audit_events_rule" {
  depends_on      = [ aws_cloudwatch_event_bus.genesys_audit_event_bridge ]
  name            = var.aws_eventbridge_name
  event_bus_name  = data.aws_cloudwatch_event_source.genesys_event_bridge.name
  event_pattern   = jsonencode({
      "source": [{
        "prefix": "aws.partner/genesys.com/cloud/${var.genesys_cloud_organization_id}/${var.aws_event_bus_name}"
      }]
    })
}

resource "aws_cloudwatch_event_target" "audit_rule" {  
  rule            = aws_cloudwatch_event_rule.audit_events_rule.name
  arn             = aws_lambda_function.test_lambda.arn
  event_bus_name  = data.aws_cloudwatch_event_source.genesys_event_bridge.name
}