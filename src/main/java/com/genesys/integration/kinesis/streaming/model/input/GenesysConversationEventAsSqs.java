package com.genesys.integration.kinesis.streaming.model.input;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenesysConversationEventAsSqs {
    private GenesysConversationEvent requestPayload;
}
