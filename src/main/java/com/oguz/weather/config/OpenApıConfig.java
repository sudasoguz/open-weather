package com.oguz.weather.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApıConfig {

  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI();
  }
}
