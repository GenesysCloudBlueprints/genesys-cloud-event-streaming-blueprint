package com.genesys.integration.kinesis.streaming.service.external;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesys.integration.kinesis.streaming.model.output.OutputKinesisRecord;
import com.genesys.integration.kinesis.streaming.utils.ApplicationContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AwsKinesisService {
    private final AmazonKinesis awsKinesis;
    private final ApplicationContext applicationContext;
    private final ObjectMapper jsonMapper;

    public AwsKinesisService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        jsonMapper = applicationContext.getJsonMapper();
        awsKinesis = applicationContext.getAmazonKinesis();
    }

    /**
     * Puts a record into a kinesis stream. The stream name must be set in an environment variable,
     * {@link ApplicationContext#getTargetKinesisStream()}. AWS Kinesis SDK throws many informative exceptions,
     * {@link com.amazonaws.services.kinesis.AmazonKinesis#putRecord(PutRecordRequest)}, however, an additional validation
     * is applied on the response.
     * @param outputKinesisRecord Conversation event suitable for storing in the external service
     * @throws RuntimeException if an error happen on putting a record into a Kinesis stream which was not caught by AWS Kinesis SDK.
     */
    @SneakyThrows
    public void putRecord(OutputKinesisRecord outputKinesisRecord, String correlationId) {
       final String targetKinesisStream = applicationContext.getTargetKinesisStream();

        final PutRecordResult kinesisPutRecordResult = awsKinesis.putRecord(
                new PutRecordRequest()
                        .withStreamName(targetKinesisStream)
                        .withData(
                                ByteBuffer.wrap(jsonMapper.writeValueAsString(outputKinesisRecord).getBytes(StandardCharsets.UTF_8)))
                        .withPartitionKey(outputKinesisRecord.getConversationId())
        );

        throwOnKinesisError(kinesisPutRecordResult, outputKinesisRecord.getConversationId());

        log.info(
                "Conversation with ID {} put into AWS Kinesis stream {}. CorrelationID: {}",
                outputKinesisRecord.getConversationId(),
                targetKinesisStream,
                correlationId
        );
    }

    private void throwOnKinesisError(PutRecordResult kinesisPutRecordResult, String conversationId) {
        if (kinesisPutRecordResult == null ||
                HttpResponseStatus.OK.code() != kinesisPutRecordResult.getSdkHttpMetadata().getHttpStatusCode()) {
            throw new RuntimeException(kinesisPutRecordResult == null ?
                    String.format("AWS Kinesis returned NULL on putRecord for conversationId %s", conversationId) :
                    String.format("Bad HTTP status %d returned from AWS Kinesis on requestID %s, conversationId %s",
                            kinesisPutRecordResult.getSdkHttpMetadata().getHttpStatusCode(),
                            kinesisPutRecordResult.getSdkResponseMetadata().getRequestId(),
                            conversationId
                    )
            );
        }
    }
}
