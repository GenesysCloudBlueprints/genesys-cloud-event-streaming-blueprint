package com.genesys.integration.kinesis.streaming.utils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class ApplicationContext {
    private static final String ENVVAR_AWS_REGION = "AWS_REGION";
    private static final String ENVVAR_TARGET_KINESIS_STREAM = "AWS_KINESIS_TARGET_STREAM";
    private volatile AmazonKinesis amazonKinesis;
    private volatile ObjectMapper jsonMapper;

    /**
     * Reads value of 'AWS_REGION' environment variable, available in all Lambdas.
     *
     * @return AWS region as defined in {@link com.amazonaws.regions.Regions}
     * @throws EnvVarNotSetException if the value is empty/null/not found
     */
    public String getAwsRegion() {
        return getEnvVarOrThrow(ENVVAR_AWS_REGION);
    }

    /**
     * Reads a name of a target AWS Kinesis stream from an environment variable. The environment variable
     * should be set by Terraform/CloudFormation, or manually in AWS Lambda Web Console.
     *
     * @return name of a target AWS Kinesis Stream which is used as a source by the external service.
     * @throws EnvVarNotSetException if the value is empty/null/not found
     */
    public String getTargetKinesisStream() {
        return getEnvVarOrThrow(ENVVAR_TARGET_KINESIS_STREAM);
    }

    public synchronized AmazonKinesis getAmazonKinesis() {
        if (amazonKinesis != null) {
            return amazonKinesis;
        }

        amazonKinesis = AmazonKinesisClientBuilder
                .standard()
                .withRegion(Regions.fromName(getAwsRegion()))
                .build();

        return amazonKinesis;
    }

    public synchronized ObjectMapper getJsonMapper() {
        if (jsonMapper != null) {
            return jsonMapper;
        }

        jsonMapper = JsonMapper.builder()
                .addModule(new JodaModule())
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();

        return jsonMapper;
    }

    private String getEnvVarOrThrow(String envVarName) {
        String envVarValue = System.getenv(envVarName);

        if (StringUtils.isNullOrEmpty(envVarValue)) {
            throw new EnvVarNotSetException(envVarName);
        }

        return envVarValue;
    }
}
