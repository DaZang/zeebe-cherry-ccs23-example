package io.camunda.cherry.joke;

import io.camunda.cherry.joke.api.client.JokeApiClient;
import io.camunda.cherry.joke.api.config.ApplicationConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest (classes = { JokeWorkerTestApplication.class })
@Import (ApplicationConfig.class)
class JokeApiClientTestTest {

   @Autowired
   private JokeApiClient jokeApiClient;

   @Test
   void test() {
      var test = jokeApiClient.getRandomJoke("en");
      assertThat(test.getJoke()).isNotBlank();
   }
}