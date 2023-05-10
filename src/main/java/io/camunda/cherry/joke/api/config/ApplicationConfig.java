package io.camunda.cherry.joke.api.config;

import io.camunda.cherry.joke.api.client.JokeApiClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

   @Bean
   public JokeApiClient jokeApiClient(final RestTemplate restTemplate){
      return new JokeApiClient(restTemplate);
   }

   @Bean
   public RestTemplate restTemplate(final RestTemplateBuilder builder){
      return builder.build();
   }
}
