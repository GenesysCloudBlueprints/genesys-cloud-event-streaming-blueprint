package com.genesys.integration.kinesis.streaming.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenesysConversationEventMetadata {
    @JsonProperty("CorrelationId")
    private String correlationId;
}
