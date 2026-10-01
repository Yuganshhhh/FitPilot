package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Checks whether a user exists by calling the User Service over HTTP.
 *
 * Uses a WebClient (configured in WebClientConfig) pointing at the User Service,
 * so the Activity Service never stores activities for unverified user ids.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {
    private final WebClient userServiceWebClient;

    /**
     * Calls GET /api/users/{userId}/validate on the User Service.
     *
     * @param userId the id to verify
     * @return true if the user is valid; false if the User Service returns an error
     */
    public boolean validateUser(String userId) {
        log.info("Calling User Service for {}", userId);
        try {
            return userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (WebClientResponseException e) {
            e.printStackTrace();
        }
        return false;
    }
}
