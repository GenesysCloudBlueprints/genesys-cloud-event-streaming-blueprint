package com.genesys.integration.kinesis.streaming.model.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genesys.integration.kinesis.streaming.model.input.GenesysConversationEventBody;
import com.genesys.integration.kinesis.streaming.model.input.GenesysConversationEventDetail;
import com.genesys.integration.kinesis.streaming.model.input.GenesysConversationEvent;
import com.genesys.integration.kinesis.streaming.model.input.GenesysConversationStartEndEventBody;
import com.genesys.integration.kinesis.streaming.model.output.GenesysConversationEventBodyType;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;

@Getter
@ToString
@Slf4j
public class OutputKinesisRecord {
    @JsonProperty("_id")
    private final String id;
    private final String version;
    private final String conversationId;
    private final String topicName;
    private final Date timestamp;
    private final String mediaType;
    private final String provider;
    private final String direction;
    private final String sessionId;
    private final String customerId;

    private String wrapupCode;
    private Long duration;
    private String externalContactId;

    public OutputKinesisRecord(GenesysConversationEvent genesysConversationEvent) {
        final GenesysConversationEventDetail eventDetail = genesysConversationEvent.getDetail();
        final GenesysConversationEventBody eventBody = eventDetail.getEventBody();

        id = genesysConversationEvent.getId();
        version = eventDetail.getVersion();
        conversationId = eventBody.getConversationId();
        topicName = eventDetail.getTopicName();
        timestamp = new Date(eventBody.getEventTime());
        mediaType = eventBody.getMediaType();
        provider = eventBody.getProvider();
        direction = eventBody.getDirection();
        sessionId = eventBody.getSessionId();
        customerId = eventBody.getParticipantId();
        final Optional<String> optionalExternalContactId = eventBody
                .getConversationExternalContactIds()
                .stream()
                .findFirst();
        optionalExternalContactId.ifPresent(s -> externalContactId = s);

        final GenesysConversationEventBodyType eventBodyType = GenesysConversationEventBodyType.getEventBodyType(topicName);

        switch (eventBodyType) {
            case CustomerStart:
                break;
            case CustomerEnd:
                duration = eventBody.getInteractingDurationMs();
                break;
            case AfterCallWorkEvent:
                wrapupCode = eventBody.getWrapupCode();
                break;
            default:
                log.info("No appropriate GenesysConversationEventBodyType");
        }
    }
}
