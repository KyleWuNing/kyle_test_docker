package com.kylewu.chat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class LlmClientTest {

    @Test
    void testLlmCalls() {
        // Customize these values for your test
        String baseUrl = "https://ai.ctaigw.cn/v1";
        String apiKey = "sk-DSDGq5ilewcrQXe3X8Rn41rshro";
        String model = "glm-5.1";
        String prompt = "Say hello in 3 words.";

        LlmClient client = new LlmClient();

        // 1. Call non-streaming and print response (usage/cost details)
        client.callLlmNonStreaming(baseUrl, apiKey, model, prompt);

        // 2. Call streaming and print response (usage/cost details)
        client.callLlmStreaming(baseUrl, apiKey, model, prompt);
    }
}
