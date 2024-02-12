package com.genesys.integration.kinesis.streaming.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TestUtils {
    public static String readResourceFile(String path) throws IOException {
        final ClassLoader loader = TestUtils.class.getClassLoader();
        try (final InputStream inputStream = loader.getResourceAsStream(path)) {
            assert inputStream != null;
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            final BufferedReader reader = new BufferedReader(inputStreamReader);

            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public static String decodeKinesisData(ByteBuffer binaryData) {
        return StandardCharsets.UTF_8.decode(binaryData).toString();
    }
}
