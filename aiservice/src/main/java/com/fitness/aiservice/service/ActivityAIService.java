package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Core AI service that turns a fitness activity into a structured recommendation.
 * <p>
 * Flow: build a prompt from the activity, send it to Gemini through
 * {@link GeminiService}, parse the JSON reply, and convert it into a
 * {@link Recommendation} object. If anything fails, a safe default
 * recommendation is returned instead.
 */
@Service
@Slf4j
@AllArgsConstructor
public class ActivityAIService {
    private final GeminiService geminiService;

    /**
     * Generates an AI-based recommendation for the given activity.
     *
     * @param activity the fitness activity to analyze
     * @return the generated recommendation (or a default one if AI parsing fails)
     */
    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getRecommendations(prompt);
        log.info("RESPONSE FROM AI {} ", aiResponse);
        return processAIResponse(activity, aiResponse);
    }

    /**
     * Parses the raw Gemini response and builds a {@link Recommendation}.
     * <p>
     * Extracts the text part from the response, removes markdown code fences,
     * reads the analysis, improvements, suggestions and safety sections, and
     * falls back to {@link #createDefaultRecommendation(Activity)} on any error.
     *
     * @param activity   the activity that was analyzed
     * @param aiResponse the raw JSON response from Gemini
     * @return the parsed recommendation
     */
    private Recommendation processAIResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);
            // Navigate: candidates[0] -> content -> parts[0] -> text
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .get("parts")
                    .get(0)
                    .path("text");

            // Remove ```json ... ``` markdown wrappers added by the AI
            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```","")
                    .trim();

//            log.info("RESPONSE FROM CLEANED AI {} ", jsonContent);

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            // Combine all analysis sections into one readable text
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafetyGuidelines(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .type(activity.getType().toString())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            // AI response could not be parsed, so return a safe fallback
            return createDefaultRecommendation(activity);
        }
    }

    /**
     * Creates a generic fallback recommendation used when the AI response
     * is missing or cannot be parsed.
     *
     * @param activity the activity the recommendation belongs to
     * @return a default recommendation with basic safety advice
     */
    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getType().toString())
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness consultant"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Reads the "safety" array from the AI response.
     *
     * @param safetyNode the JSON node containing safety points
     * @return list of safety guidelines, or a generic one if none were provided
     */
    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty() ?
                Collections.singletonList("Follow general safety guidelines") :
                safety;
    }

    /**
     * Reads the "suggestions" array and formats each entry as
     * {@code "workout: description"}.
     *
     * @param suggestionsNode the JSON node containing workout suggestions
     * @return list of formatted suggestions, or a placeholder if none were provided
     */
    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ?
                Collections.singletonList("No specific suggestions provided") :
                suggestions;
    }

    /**
     * Reads the "improvements" array and formats each entry as
     * {@code "area: recommendation"}.
     *
     * @param improvementsNode the JSON node containing improvement areas
     * @return list of formatted improvements, or a placeholder if none were provided
     */
    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, detail));
            });
        }
        return improvements.isEmpty() ?
                Collections.singletonList("No specific improvements provided") :
                improvements;

    }

    /**
     * Appends one analysis section (e.g. pace, heart rate) to the full analysis text
     * if that key exists in the AI response.
     * <p>
     * Example: {@code "overall": "This was an excellent"} becomes
     * {@code "Overall: This was an excellent"}.
     *
     * @param fullAnalysis the builder collecting the final analysis text
     * @param analysisNode the JSON "analysis" node from the AI response
     * @param key          the JSON key to read (e.g. "pace")
     * @param prefix       the label to place before the text (e.g. "Pace:")
     */
    //    "overall": "This was an excellent"
    // Overall: This was an excellent
    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
    if (!analysisNode.path(key).isMissingNode()){
     fullAnalysis.append(prefix)
             .append(analysisNode.path(key).asText())
             .append("\n\n");
    }
    }

    /**
     * Builds the prompt sent to Gemini.
     * <p>
     * The prompt asks the AI to reply in a strict JSON format containing
     * analysis, improvements, suggestions and safety tips, and includes the
     * activity's type, duration, calories burned and additional metrics.
     *
     * @param activity the activity to describe in the prompt
     * @return the complete prompt text
     */
    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
