package com.genesys.integration.kinesis.streaming.utils;

import com.genesys.integration.kinesis.streaming.utils.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApplicationContextTest {
    @Test
    public void environmentVariablesAreNotSetTest() {
        assertThrows(RuntimeException.class, () -> new ApplicationContext().getTargetKinesisStream());
        assertThrows(RuntimeException.class, () -> new ApplicationContext().getTargetKinesisStream());
    }
}
