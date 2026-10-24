package com.fitness.aiservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
  // Enables Mongo auditing for this service's documents (same setup as in Activity Service).
}
