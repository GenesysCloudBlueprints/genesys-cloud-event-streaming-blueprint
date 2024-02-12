package com.genesys.integration.kinesis.streaming.handler;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.lambda.runtime.tests.EventLoader;
import com.genesys.integration.kinesis.streaming.service.ConversationEventProcessor;
import com.genesys.integration.kinesis.streaming.utils.ApplicationContext;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConversationEventProcessorIT {
    private ConversationEventProcessor processor;
    private AutoCloseable openMocks;

    @Mock
    private AmazonKinesis awsKinesis;

    @Captor
    private ArgumentCaptor<PutRecordRequest> awsKinesisPutRecordRequestCaptor;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    public void before() {
        openMocks = MockitoAnnotations.openMocks(this);

        when(applicationContext.getJsonMapper()).thenCallRealMethod();

        PutRecordResult kinesisPutRecordResult = new PutRecordResult();
        HttpResponse kinesisPutRecordHttpResponse = new HttpResponse(null, null, null);
        kinesisPutRecordHttpResponse.setStatusCode(200);
        kinesisPutRecordResult.setSdkHttpMetadata(SdkHttpMetadata.from(kinesisPutRecordHttpResponse));

        when(awsKinesis.putRecord(any(PutRecordRequest.class))).thenReturn(kinesisPutRecordResult);
        when(applicationContext.getAmazonKinesis()).thenReturn(awsKinesis);

        // Mock env vars
        when(applicationContext.getAwsRegion()).thenReturn("us-east-1");
        when(applicationContext.getTargetKinesisStream()).thenReturn("aws-test-kinesis-stream");

        processor = new ConversationEventProcessor(applicationContext);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    public void process_nonConversationEvent() {
        ScheduledEvent event = EventLoader.loadScheduledEvent("input/aws-event-bridge-non-conversation-event.json");
        processor.process(event);

        verify(awsKinesis, never()).putRecord(any(PutRecordRequest.class));
    }

    @Test
    public void process_conversationStartEvent() throws JSONException, IOException {
        ScheduledEvent event = EventLoader.loadScheduledEvent("input/aws-event-bridge-conversation-start-event.json");

        processor.process(event);

        verify(awsKinesis).putRecord(awsKinesisPutRecordRequestCaptor.capture());

        JSONAssert.assertEquals(
                TestUtils.readResourceFile("output/aws-kinesis-conversation-start-event.json"),
                TestUtils.decodeKinesisData(awsKinesisPutRecordRequestCaptor.getValue().getData()),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    public void process_conversationEndEvent() throws JSONException, IOException {
        ScheduledEvent event = EventLoader.loadScheduledEvent("input/aws-event-bridge-conversation-end-event.json");

        processor.process(event);

        verify(awsKinesis).putRecord(awsKinesisPutRecordRequestCaptor.capture());

        JSONAssert.assertEquals(
                TestUtils.readResourceFile("output/aws-kinesis-conversation-end-event.json"),
                TestUtils.decodeKinesisData(awsKinesisPutRecordRequestCaptor.getValue().getData()),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    public void process_conversationEndEvent_noExternalContactId_doesNothing() {
        ScheduledEvent event = EventLoader.loadScheduledEvent(
                "input/aws-event-bridge-conversation-end-event-no-external-contact-id.json");

        processor.process(event);

        verify(awsKinesis, times(0)).putRecord(any());
    }

    @Test
    public void process_error404onFetchingJourneySession() throws JSONException, IOException {
        ScheduledEvent event = EventLoader.loadScheduledEvent("input/aws-event-bridge-conversation-end-event.json");

        processor.process(event);

        verify(awsKinesis).putRecord(awsKinesisPutRecordRequestCaptor.capture());

        JSONAssert.assertEquals(
                TestUtils.readResourceFile("output/aws-kinesis-conversation-end-event-without-journey-session.json"),
                TestUtils.decodeKinesisData(awsKinesisPutRecordRequestCaptor.getValue().getData()),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    public void process_conversationEndEvent_noJourneySession() throws JSONException, IOException {
        ScheduledEvent event = EventLoader.loadScheduledEvent("input/aws-event-bridge-conversation-end-event.json");

        processor.process(event);

        verify(awsKinesis).putRecord(awsKinesisPutRecordRequestCaptor.capture());

        JSONAssert.assertEquals(
                TestUtils.readResourceFile("output/aws-kinesis-conversation-end-event-without-journey-session.json"),
                TestUtils.decodeKinesisData(awsKinesisPutRecordRequestCaptor.getValue().getData()),
                JSONCompareMode.LENIENT
        );
    }

    @Test
    public void process_kinesisReturnsNon200Status() {
        ScheduledEvent event = EventLoader.loadScheduledEvent("input/aws-event-bridge-conversation-start-event.json");

        PutRecordResult kinesisPutRecordResult = new PutRecordResult();
        HttpResponse kinesisPutRecordHttpResponse = new HttpResponse(null, null, null);
        kinesisPutRecordHttpResponse.setStatusCode(400);
        kinesisPutRecordResult.setSdkHttpMetadata(SdkHttpMetadata.from(kinesisPutRecordHttpResponse));

        when(awsKinesis.putRecord(any(PutRecordRequest.class))).thenReturn(kinesisPutRecordResult);

        assertThrows(RuntimeException.class, () -> processor.process(event));
    }

    @Test
    public void process_kinesisReturnsNull() {
        ScheduledEvent event = EventLoader.loadScheduledEvent("input/aws-event-bridge-conversation-start-event.json");

        when(awsKinesis.putRecord(any(PutRecordRequest.class))).thenReturn(null);

        assertThrows(RuntimeException.class, () -> processor.process(event));
    }
}
