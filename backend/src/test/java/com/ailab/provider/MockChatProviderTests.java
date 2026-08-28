package com.ailab.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MockChatProviderTests {

    private final MockChatProvider provider = new MockChatProvider("mock response");

    @Test
    void returnsConfiguredResponse() {
        assertThat(provider.chat("hello")).isEqualTo("mock response");
    }

    @Test
    void streamsConfiguredResponse() {
        assertThat(provider.stream("hello").collectList().block())
                .containsExactly("mock response");
    }
}
