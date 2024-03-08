# Genesys Cloud Information
variable "genesys_cloud_organization_id" {
  type        = string
  description = "Genesys Cloud Organization Id"
}

# AWS Information
variable "aws_account_id" {
  type        = string
  description = "The 12 digit AWS account ID where the event source will be made available for an event bus."
}

variable "aws_region" {
  type        = string
  description = "The AWS region where the event source will be made available for an event bus. (e.g. us-east-1)"
}

# Object Names
variable "aws_event_bus_name" {
  type        = string
  description = "A unique name appended to the Event Source in the AWS account. Maximum of 64 characters consisting of lower/upper case letters, numbers, ., -, _."
}

variable "aws_sqs_queue_name" {
  type        = string
  description = "AWS SQS queue name"
}

variable "aws_stream_name" {
  type        = string
  description = "AWS stream name"
}

variable "aws_iam_poicy_name" {
  type        = string
  description = "AWS IAM policy name"
}

variable "aws_iam_user_name" {
  type        = string
  description = "AWS IAM user name"
}

variable "aws_iam_lambda_role_name" {
  type        = string
  description = "AWS IAM lambda role name"
}

variable "aws_lambda_function_name" {
  type        = string
  description = "AWS lambda function name"
}

variable "aws_eventbridge_name" {
  type        = string
  description = "AWS EventBridge name"
}