package com.genesys.integration.kinesis.streaming.service;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesys.integration.kinesis.streaming.model.input.GenesysConversationEvent;
import com.genesys.integration.kinesis.streaming.model.input.GenesysConversationEventAsSqs;
import com.genesys.integration.kinesis.streaming.model.output.OutputKinesisRecord;
import com.genesys.integration.kinesis.streaming.service.external.AwsKinesisService;
import com.genesys.integration.kinesis.streaming.utils.ApplicationContext;
import com.genesys.integration.kinesis.streaming.utils.ConversationUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * The class is responsible for whole business logic of processing a conversation event from Genesys
 */
@Log4j2
public class ConversationEventProcessor {
    private final AwsKinesisService awsKinesisService;
    private final ObjectMapper jsonMapper;

    public ConversationEventProcessor(ApplicationContext applicationContext) {
        this.jsonMapper = applicationContext.getJsonMapper();
        awsKinesisService = new AwsKinesisService(applicationContext);
    }

    @SneakyThrows
    public void process(SQSEvent sqsEvent) {
        final GenesysConversationEventAsSqs sqsConversationEventAsJson = jsonMapper.readValue(
                sqsEvent.getRecords().get(0).getBody(),
                GenesysConversationEventAsSqs.class
        );

        log.info("sqs conversation event: {}", sqsConversationEventAsJson);

        final GenesysConversationEvent conversationEvent = sqsConversationEventAsJson.getRequestPayload();
        process(conversationEvent);
    }

    @SneakyThrows
    public void process(ScheduledEvent scheduledEvent) {
        if (!ConversationUtils.isConversationEvent(scheduledEvent)) {
            log.warn(
                    "Scheduled event with id {} is not a conversation event, terminating. Topic name: {}",
                    scheduledEvent.getId(),
                    scheduledEvent.getDetailType()
            );

            return;
        }

        log.info("processing event: {}", scheduledEvent);

        final GenesysConversationEvent genesysConversationEvent = jsonMapper.convertValue(scheduledEvent, GenesysConversationEvent.class);
        process(genesysConversationEvent);
    }

    @SneakyThrows
    public void process(GenesysConversationEvent genesysConversationEvent) {

        final String conversationId = ConversationUtils.getConversationIdFromEvent(genesysConversationEvent);

        if (StringUtils.isNullOrEmpty(conversationId)) {
            log.warn("Cannot find conversation ID from the event, terminating.");
            return;
        }

        log.info("Starting processing conversation with id {}", conversationId);

        final OutputKinesisRecord outputKinesisRecord = new OutputKinesisRecord(genesysConversationEvent);

        if (outputKinesisRecord.getExternalContactId() == null) {
            log.error("conversationExternalContactIds of GenesysConversationEvent is empty. Aborting invocation.");
            return;
        }

        log.info("Output: {}", jsonMapper.writeValueAsString(outputKinesisRecord));

        awsKinesisService.putRecord(outputKinesisRecord, ConversationUtils.getCorrelationIdFromEvent(genesysConversationEvent));
    }
}
