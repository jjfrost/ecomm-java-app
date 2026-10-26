package com.ecommerce.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI ecommerceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Northwind Commerce API")
                        .version("1.0")
                        .description(
                                "Public reference API for Oracle-to-PostgreSQL conversion demos."));
    }
}
