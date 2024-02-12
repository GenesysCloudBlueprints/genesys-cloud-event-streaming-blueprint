package com.genesys.integration.kinesis.streaming.model.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class GenesysConversationEvent {
    private String id;
    @JsonProperty("detail-type")
    private String detailType;
    private String source;
    private String account;
    @JsonFormat(timezone = "Etc/UTC")
    private Date time;
    private String region;
    private List<String> resources;
    private GenesysConversationEventDetail detail;
}
