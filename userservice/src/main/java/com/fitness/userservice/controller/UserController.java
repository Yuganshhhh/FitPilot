package com.fitness.userservice.controller;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes user-related endpoints under {@code /api/users}.
 * <p>
 * Handles fetching a user's profile, registering a new user, and
 * validating whether a user exists.
 */
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    /**
     * Fetches the profile of a user.
     *
     * @param userId the unique ID of the user
     * @return 200 OK with the user's profile details
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    /**
     * Registers a new user after validating the request body.
     * If the email is already registered, the existing user is returned.
     *
     * @param request the registration details (email, password, name, keycloakId)
     * @return 200 OK with the registered user's details
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    /**
     * Checks whether a user exists for the given Keycloak user ID.
     * Used by the Activity Service to validate a user before saving an activity.
     *
     * @param userId the Keycloak ID of the user
     * @return 200 OK with {@code true} if the user exists, otherwise {@code false}
     */
    @GetMapping("/{userId}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String userId) {
        return ResponseEntity.ok(userService.existByUserId(userId));
    }
}
