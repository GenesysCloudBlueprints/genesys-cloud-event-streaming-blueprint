package com.genesys.integration.kinesis.streaming.model.output;

public enum GenesysConversationEventBodyType {
    AfterCallWorkEvent("acw"),
    CustomerStart("customer.start"),
    CustomerEnd("customer.end");

    private final String code;

    GenesysConversationEventBodyType(String code) {
        this.code = code;
    }

    public static GenesysConversationEventBodyType getEventBodyType(String topic) {
        if (topic.contains(CustomerStart.code)) {
            return CustomerStart;
        } else if (topic.contains(CustomerEnd.code)) {
            return CustomerEnd;
        } else if (topic.contains(AfterCallWorkEvent.code)) {
            return AfterCallWorkEvent;
        } else {
            throw new RuntimeException("Invalid topic for body times - only accepts AfterCallWorkEvent, CustomerStart, and CustomerEnd.");
        }
    }
}
