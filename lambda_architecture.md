# Architecture

## Overview

```
   AWS EventBridge                 The Lambda           AWS Kinesis
          |                            |                     |
          |                            |                     |
          |     Conversation event     |                     |
          |--------------------------->|                     |
          |                            |      Put record into a kinesis stream
          |                            |-------------------->|
          |                            |                     |
          |                            |                     |
```

## Components

### AWS EventBridge

AWS EventBridge is a service which connects Genesys Cloud with a customer's AWS account. Every time a Genesys Cloud conversation is 
started or finished, the associated event is sent via AWS EventBridge, triggering the Lambda.

### AWS Kinesis

An AWS Kinesis stream is used as a source for your external service. The name of the stream is stored in the `AWS_KINESIS_TARGET_STREAM`
environment variable.

### The Lambda

The Lambda is the core of the integration between Genesys Cloud and your external service. It connects all the components above together.
The Lambda is triggered every time when a Genesys conversation event is received by the customer's EventBridge. Once the conversation event
has been received, the Lambda puts it into a Kinesis stream making it available for consuming by your external service. We filter out
unnecessary information from the Conversation events, filling only what the Kinesis stream needs, and ensure that the data is in the right
format for the your external service.

## Building from sources

### Tools

1. Java 11 (OpenJDK, Amazon Corretto or alternative)
2. Apache Maven 3.5.3 or higher
3. Git
4. Internet connection

### Building steps

1. Clone the repository with the source code: `git clone <REPO_URL>`
2. Open directory with the source code in a console: `cd <PROJECT_ROOT>`
3. Run command `mvn clean verify -U`. It will download dependencies, compile, test and pack everything into a jar file
4. If no errors happened on the previous steps, the jar file ready to upload to AWS Lambda: `<PROJECT_ROOT>/target/genesys-external-streaming-integration.jar`
