package com.sih.innovationplatform.ai;

import com.sih.innovationplatform.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoteAiGatewayTimeoutTest {

    @Test
    void configuredTimeoutIsAppliedToConnectAndRead() throws Exception {
        AppProperties props = new AppProperties();
        props.getAi().setTimeoutMs(1234);

        SimpleClientHttpRequestFactory factory = RemoteAiGateway.requestFactory(props);

        assertEquals(1234, intField(factory, "connectTimeout"));
        assertEquals(1234, intField(factory, "readTimeout"));
    }

    private static int intField(SimpleClientHttpRequestFactory factory, String name) throws Exception {
        Field field = SimpleClientHttpRequestFactory.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(factory);
    }
}
