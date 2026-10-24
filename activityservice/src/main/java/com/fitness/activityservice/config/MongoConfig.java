package com.fitness.activityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
  // @EnableMongoAuditing turns on Spring Data auditing, so annotations such as
  // @CreatedDate and @LastModifiedDate are filled in automatically when documents are saved.
}
