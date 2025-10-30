package org.slayscale;

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
        SpringApplication.run(SlayScaleApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo() {
        return args -> log.info("Application is running...");
    }
}