package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
public class ActivityController {

    private ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request, @RequestHeader("X-User-ID") String userId) {

        // X-User-ID is set by the gateway (from the JWT) and overrides any userId sent in the request body.
        request.setUserId(userId);
        return ResponseEntity.ok(activityService.trackActivity(request));
    }


    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId) {

        // Returns only the activities belonging to the user identified by the X-User-ID header.
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }
}
