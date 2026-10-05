package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.respository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer that listens for new fitness activities.
 * <p>
 * Whenever the Activity Service publishes an activity to the Kafka topic,
 * this listener picks it up, asks the AI to generate a recommendation,
 * and saves the result in the database.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService activityAIService;
    private final RecommendationRepository recommendationRepository;

    /**
     * Processes an activity message received from the Kafka topic.
     * <p>
     * Topic name is configured via {@code kafka.topic.name} and the
     * consumer group is {@code activity-processor-group}.
     *
     * @param activity the activity received from Kafka
     */
    @KafkaListener(topics = "${kafka.topic.name}", groupId = "activity-processor-group")
    public void processActivity(Activity activity) {
        log.info("Received Activity for processing: {}", activity.getUserId());
        // Generate AI recommendation and persist it
        Recommendation recommendation = activityAIService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
    }
}
