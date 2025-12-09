package com.vm.skeleton.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Skeleton API")
                .description("Skeleton API reference for developers").version("1.0").contact(contact()));
    }

    private Contact contact() {
        Contact contact = new Contact();
        contact.setName("Matainer");
        contact.setEmail("abc@gmail.com");
        return contact;
    }
}

