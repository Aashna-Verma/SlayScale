package org.slayscale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

    private final SeedDataService seedDataService;

    public AdminController(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @PostMapping("/seeddata")
    public ResponseEntity<Map<String, String>> seedData() {
        seedDataService.seed();
        return ResponseEntity.ok(Map.of("message", "Seeded missing data"));
    }
}

