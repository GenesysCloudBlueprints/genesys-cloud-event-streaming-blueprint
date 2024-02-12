package com.genesys.integration.kinesis.streaming.model.input;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenesysConversationStartEndEventBody extends GenesysConversationEventBody {
    private String externalOrganizationId;
    private Long interactingDurationMs;
}
