package com.logistics.rag;

import com.logistics.rag.infrastructure.ai.AnthropicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AnthropicProperties.class)
public class RagServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagServiceApplication.class, args);
    }
}
