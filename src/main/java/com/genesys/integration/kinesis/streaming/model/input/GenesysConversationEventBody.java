package com.genesys.integration.kinesis.streaming.model.input;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenesysConversationEventBody {
    private Long eventTime;
    private String conversationId;
    private String participantId;
    private String sessionId;
    private String mediaType;
    private String provider;
    private String direction;
    private List<String> conversationExternalContactIds;
    private String externalContactId;
    private String externalOrganizationId;
    private Long interactingDurationMs;
    private String userId;
    private String divisionId;
    private String queueId;
    private String wrapupCode;
    private String wrapupDurationms;
}
