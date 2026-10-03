package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.respository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for fetching stored AI recommendations.
 * <p>
 * Acts as a bridge between {@link com.fitness.aiservice.controller.RecommendationController}
 * and the {@link RecommendationRepository}.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationRepository recommendationRepository;

    /**
     * Retrieves all recommendations generated for a particular user.
     *
     * @param userId the unique ID of the user
     * @return list of recommendations belonging to the user (empty if none)
     */
    public List<Recommendation> getUserRecommendation(String userId) {
        return recommendationRepository.findByUserId(userId);
    }

    /**
     * Retrieves the recommendation generated for a specific activity.
     *
     * @param activityId the unique ID of the activity
     * @return the recommendation linked to the given activity
     * @throws RuntimeException if no recommendation exists for the activity
     */
    public Recommendation getActivityRecommendation(String activityId) {
        return recommendationRepository.findByActivityId(activityId)
                .orElseThrow(() -> new RuntimeException("No recommendation found for this activity: " + activityId));
    }
}
