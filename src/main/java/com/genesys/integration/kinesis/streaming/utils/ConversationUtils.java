package com.genesys.integration.kinesis.streaming.utils;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.genesys.integration.kinesis.streaming.model.input.GenesysConversationEvent;

import java.util.Objects;

public class ConversationUtils {

    /**
     * Helper method for reading a conversation id from a conversation event issued by Genesys
     *
     * @param event input conversation event
     * @return conversationId
     */
    public static String getConversationIdFromEvent(GenesysConversationEvent event) {
        return Objects.requireNonNull(event.getDetail().getEventBody()).getConversationId();
    }

    /**
     * Helper method for reading a correlation id from a conversation event issued by Genesys
     *
     * @param event input conversation event
     * @return conversationId
     */
    public static String getCorrelationIdFromEvent(GenesysConversationEvent event) {
        return Objects.requireNonNull(event.getDetail().getMetadata()).getCorrelationId();
    }

    /**
     * Checks that the input event is a conversation event
     *
     * @param scheduledEvent event triggered the Lambda
     * @return true if the input event is a well-formed Genesys conversation start or end event
     */
    // (perhaps a list of allowed starting things etc.)
    public static boolean isConversationEvent(ScheduledEvent scheduledEvent) {
        return Objects.requireNonNull(scheduledEvent.getDetailType()).toLowerCase().startsWith("v2.detail");
    }
}
