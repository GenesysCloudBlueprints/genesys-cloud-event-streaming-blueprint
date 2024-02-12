package com.genesys.integration.kinesis.streaming.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.genesys.integration.kinesis.streaming.service.ConversationEventProcessor;
import com.genesys.integration.kinesis.streaming.utils.ApplicationContext;
import com.genesys.integration.kinesis.streaming.utils.Generated;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@SuppressWarnings("unused")
@Generated
@NoArgsConstructor
@Log4j2
public class EventBridgeHandler implements RequestHandler<ScheduledEvent, Void> {

    /**
     * The Lambda starts executing from this method. It is responsible only for creating
     * new instances of {@link ApplicationContext} and {@link ConversationEventProcessor} and passing the event there.
     *
     * @param scheduledEvent a Genesys conversation event from AWS Event Bridge which triggers the Lambda
     */
    @Override
    public Void handleRequest(ScheduledEvent scheduledEvent, Context context) {
        log.info("Received ScheduledEvent with id {}", scheduledEvent.getId());
        new ConversationEventProcessor(new ApplicationContext()).process(scheduledEvent);

        return null;
    }
}
