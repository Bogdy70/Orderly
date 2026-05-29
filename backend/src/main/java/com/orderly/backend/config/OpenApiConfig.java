package com.orderly.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI orderlyOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Orderly API")
            .version("0.1.0")
            .description("CRUD API for Orderly users, spaces, blocks, checklist items, table rows and diagrams.")
            .license(new License().name("Private")));
  }
}
