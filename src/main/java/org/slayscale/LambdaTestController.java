package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class LambdaTestController {

    private final LambdaService lambdaService;

    public LambdaTestController(LambdaService  lambdaService ) {
        this.lambdaService  = lambdaService ;
    }

    @GetMapping("/lambda")
    public ResponseEntity<String> testLambda() {
        try {
            String response = lambdaService.seedData();
            return ResponseEntity.ok("Lambda response: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calling Lambda: " + e.getMessage());
        }
    }
}

