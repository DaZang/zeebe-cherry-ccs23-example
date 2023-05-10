package io.camunda.cherry.joke;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.camunda.cherry.joke.api")
public class JokeWorkerTestApplication {

   public static void main(String[] args) {
      SpringApplication.run(JokeWorkerTestApplication.class, args);
   }
}
