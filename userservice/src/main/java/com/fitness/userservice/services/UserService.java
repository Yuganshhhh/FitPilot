package com.fitness.userservice.services;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service layer containing the business logic for user management.
 * <p>
 * Handles user registration, profile retrieval and user existence checks,
 * and converts {@link User} entities into {@link UserResponse} objects.
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository repository;

    /**
     * Registers a new user.
     * <p>
     * If a user with the same email already exists, that existing user is
     * returned instead of creating a duplicate.
     *
     * @param request the registration details sent by the client
     * @return the newly created (or already existing) user as a {@link UserResponse}
     */
    public UserResponse register(RegisterRequest request) {

        // Email already registered: return the existing user instead of creating a new one
        if (repository.existsByEmail(request.getEmail())) {
            User existingUser = repository.findByEmail(request.getEmail());
            UserResponse userResponse = new UserResponse();
            userResponse.setId(existingUser.getId());
            userResponse.setPassword(existingUser.getPassword());
            userResponse.setEmail(existingUser.getEmail());
            userResponse.setFirstName(existingUser.getFirstName());
            userResponse.setLastName(existingUser.getLastName());
            userResponse.setCreatedAt(existingUser.getCreatedAt());
            userResponse.setUpdatedAt(existingUser.getUpdatedAt());
            return userResponse;
        }

        // New user: map request data to the entity and save it
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setKeycloakId(request.getKeycloakId());
        user.setLastName(request.getLastName());
        user.setPassword(request.getPassword());

        User savedUser = repository.save(user);
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setKeycloakId(savedUser.getKeycloakId());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setLastName(savedUser.getLastName());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedAt(savedUser.getUpdatedAt());
        return userResponse;
    }

    /**
     * Retrieves a user's profile by their ID.
     *
     * @param userId the unique ID of the user
     * @return the user's profile as a {@link UserResponse}
     * @throws RuntimeException if no user exists with the given ID
     */
    public UserResponse getUserProfile(String userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setPassword(user.getPassword());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;

    }

    /**
     * Checks whether a user exists for the given Keycloak ID.
     *
     * @param userId the Keycloak ID of the user
     * @return {@code true} if the user exists, otherwise {@code false}
     */
    public Boolean existByUserId(String userId) {
        log.info("Calling User Service for {}", userId);
        return repository.existsByKeycloakId(userId);
    }
}
