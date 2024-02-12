package com.genesys.integration.kinesis.streaming.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.genesys.integration.kinesis.streaming.service.ConversationEventProcessor;
import com.genesys.integration.kinesis.streaming.utils.ApplicationContext;
import com.genesys.integration.kinesis.streaming.utils.Generated;
import lombok.extern.log4j.Log4j2;

@SuppressWarnings("unused")
@Generated
@Log4j2
public class SqsDlqHandler implements RequestHandler<SQSEvent, Void> {
    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("Received SQS event with id {}", sqsEvent.getRecords().get(0).getMessageId());
        new ConversationEventProcessor(new ApplicationContext()).process(sqsEvent);

        return null;
    }
}
