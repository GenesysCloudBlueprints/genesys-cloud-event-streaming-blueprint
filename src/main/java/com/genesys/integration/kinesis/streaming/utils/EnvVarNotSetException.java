package com.genesys.integration.kinesis.streaming.utils;

public class EnvVarNotSetException extends RuntimeException {

    public EnvVarNotSetException(String envVarName) {
        super("Environment variable with name " + envVarName + " is not set");
    }
}
