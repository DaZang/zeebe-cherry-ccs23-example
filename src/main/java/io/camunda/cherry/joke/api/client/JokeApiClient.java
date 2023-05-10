package io.camunda.cherry.joke.api.client;

import io.camunda.cherry.joke.api.model.JokeResponse;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class JokeApiClient {

   private static final String BASE_URL = "https://v2.jokeapi.dev/joke/Any";

   private final RestTemplate restTemplate;

   public JokeApiClient(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
   }

   public JokeResponse getRandomJoke(final String category) {
      var resp = restTemplate.getForEntity(BASE_URL, JokeResponse.class,
            Map.of("category", category, "format", "json", "type", "single", "lang", "en"));
      return resp.getBody();
   }
}
