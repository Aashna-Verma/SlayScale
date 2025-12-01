package org.slayscale;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SlayScaleApplication {
    private static final Logger log = LoggerFactory.getLogger(SlayScaleApplication.class);

    public static void main(String[] args) {
        // Load dotenv BEFORE Spring initializes properties
        try {
            Dotenv dotenv = Dotenv.configure().load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

        } catch (Exception e) {
            System.out.println(".env not found, skipping");
        }
        SpringApplication.run(SlayScaleApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo() {
        return args -> log.info("Application is running...");
    }
}