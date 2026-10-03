package com.fitness.aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Service responsible for communicating with the Google Gemini API.
 * <p>
 * It sends a text prompt to the Gemini endpoint (configured via
 * {@code gemini.api.url}) using Spring WebClient and returns the raw
 * JSON response. Authentication is done with the API key configured
 * via {@code gemini.api.key}.
 */
@Service
public class GeminiService {
    private final WebClient webClient;

    /** Gemini API endpoint URL, read from application configuration. */
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    /** Gemini API key, read from application configuration. */
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    /**
     * Creates the service and builds the WebClient used for API calls.
     *
     * @param webClientBuilder Spring-provided builder for creating the WebClient
     */
    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Sends the given prompt to the Gemini API and returns its response.
     * <p>
     * The request body follows the Gemini format:
     * {@code contents -> parts -> text}. This call is blocking.
     *
     * @param details the prompt text describing the user's fitness activity
     * @return the raw JSON response returned by the Gemini API
     */
    public String getRecommendations(String details) {
        // Build the request body in the structure Gemini expects
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", details)
                        })
                }
        );

        // Send POST request with the API key in the header and wait for the response
        String response = webClient.post()
                .uri(geminiApiUrl)
                .header("Content-Type","application/json")
                .header("X-goog-api-key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return response;
    }
}
