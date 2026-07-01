package com.kylewu.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlmClient {

    private final HttpClient httpClient;

    public LlmClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Calls the LLM in non-streaming mode and prints the raw JSON response to see if it includes usage/cost info.
     *
     * @param baseUrl The base URL of the LLM endpoint (e.g., "https://api.openai.com/v1" or "https://api.deepseek.com")
     * @param apiKey  The API key for authorization
     * @param model   The model name (e.g., "gpt-4o", "deepseek-chat")
     * @param prompt  The user prompt
     */
    public void callLlmNonStreaming(String baseUrl, String apiKey, String model, String prompt) {
        System.out.println("=== Starting Non-Streaming Request ===");
        
        // Normalize base URL
        String url = baseUrl.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String completionsEndpoint = url + "/chat/completions";
        System.out.println("Endpoint: " + completionsEndpoint);

        // Construct JSON payload
        // Escape quotes in prompt and model just in case
        String escapedPrompt = escapeJson(prompt);
        String escapedModel = escapeJson(model);
        String jsonPayload = String.format(
                "{\n" +
                "  \"model\": \"%s\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"%s\"\n" +
                "    }\n" +
                "  ]\n" +
                "}",
                escapedModel, escapedPrompt
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(completionsEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(30))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("HTTP Status Code: " + response.statusCode());
            System.out.println("Response Headers:");
            response.headers().map().forEach((k, v) -> System.out.println("  " + k + ": " + v));
            System.out.println("\nRaw JSON Response Body:\n" + response.body());
            System.out.println("======================================\n");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during non-streaming call: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calls the LLM in streaming mode and prints the stream line-by-line.
     * We request usage metrics explicitly via 'stream_options' to verify cost/usage details.
     *
     * @param baseUrl The base URL of the LLM endpoint (e.g., "https://api.openai.com/v1" or "https://api.deepseek.com")
     * @param apiKey  The API key for authorization
     * @param model   The model name (e.g., "gpt-4o", "deepseek-chat")
     * @param prompt  The user prompt
     */
    public void callLlmStreaming(String baseUrl, String apiKey, String model, String prompt) {
        System.out.println("=== Starting Streaming Request ===");
        
        // Normalize base URL
        String url = baseUrl.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String completionsEndpoint = url + "/chat/completions";
        System.out.println("Endpoint: " + completionsEndpoint);

        // Construct JSON payload with stream=true and stream_options.include_usage=true
        String escapedPrompt = escapeJson(prompt);
        String escapedModel = escapeJson(model);
        String jsonPayload = String.format(
                "{\n" +
                "  \"model\": \"%s\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"%s\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"stream\": true,\n" +
                "  \"stream_options\": {\n" +
                "    \"include_usage\": true\n" +
                "  }\n" +
                "}",
                escapedModel, escapedPrompt
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(completionsEndpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(30))
                .build();

        try {
            // Using BodyHandlers.ofInputStream() to read the streaming response line by line
            HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            System.out.println("HTTP Status Code: " + response.statusCode());
            System.out.println("Response Headers:");
            response.headers().map().forEach((k, v) -> System.out.println("  " + k + ": " + v));
            System.out.println("\nStreaming Output (Line-by-Line):");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        System.out.println(line);
                    }
                }
            }
            System.out.println("===================================\n");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during streaming call: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /**
     * A main method to facilitate local testing.
     * You can run this directly in your IDE or via terminal.
     */
    public static void main(String[] args) {
        // Read configuration from Environment Variables or use default placeholder values
        String baseUrl = System.getenv("LLM_BASE_URL");
        String apiKey = System.getenv("LLM_API_KEY");
        String model = System.getenv("LLM_MODEL");
        String prompt = "Say hello and give a very brief 3-word response.";

        // Default placeholder fallback values for demonstration/guidance
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "YOUR_API_KEY_HERE";
        }
        if (model == null || model.isBlank()) {
            model = "gpt-4o-mini";
        }

        System.out.println("Current Configuration:");
        System.out.println("  Base URL: " + baseUrl);
        System.out.println("  API Key : " + (apiKey.equals("YOUR_API_KEY_HERE") ? "NOT SET" : "********"));
        System.out.println("  Model   : " + model);
        System.out.println("  Prompt  : " + prompt);
        System.out.println();

        if (apiKey.equals("YOUR_API_KEY_HERE")) {
            System.out.println("Please set the environment variables LLM_BASE_URL, LLM_API_KEY, and LLM_MODEL, or edit the source file to replace the placeholders.");
            return;
        }

        LlmClient client = new LlmClient();
        
        // 1. Call in non-streaming
        client.callLlmNonStreaming(baseUrl, apiKey, model, prompt);

        // 2. Call in streaming
        client.callLlmStreaming(baseUrl, apiKey, model, prompt);
    }
}
