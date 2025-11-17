package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LambdaController {

    private final LambdaService lambdaService;

    public LambdaController(LambdaService  lambdaService ) {
        this.lambdaService  = lambdaService ;
    }

    @Async
    @GetMapping("/sendEmail")
    public ResponseEntity<String> lambdaEmail(String email) {
        try {
            String response = lambdaService.sendEmail(email);
            return ResponseEntity.ok("Lambda response: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calling Lambda: " + e.getMessage());
        }
    }
}

