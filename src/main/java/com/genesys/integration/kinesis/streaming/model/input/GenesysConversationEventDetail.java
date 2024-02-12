package com.genesys.integration.kinesis.streaming.model.input;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class GenesysConversationEventDetail {
    private String topicName;
    private String version;
    private GenesysConversationEventBody eventBody;
    private GenesysConversationEventMetadata metadata;
    private Date timestamp;
}
