package com.hireflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HireFlow API")
                .description("""
                    ## HireFlow — Job Board & Recruitment Platform API
                    
                    A production-grade RESTful API featuring:
                    - **JWT Authentication**
                    - **Role-based access control** (Seeker / Recruiter / Admin)
                    - **Skill-gap matching** using Jaccard similarity
                    - **Async email notifications** via Spring Events
                    - **Redis caching** for high-performance job listings
                    - **Full Swagger/OpenAPI documentation**
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("HireFlow Team")
                    .email("support@hireflow.dev"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local Development"),
                new Server().url("https://api.hireflow.dev").description("Production")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .name("Authorization")
                    .description("Enter JWT token (without 'Bearer ' prefix)")));
    }
}
