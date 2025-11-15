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
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("test1");
        Product product = new Product(Category.AUTOMOTIVE,"http:/asdadads");
        user1.addReview(new Review(user1,2,"asdad",product));
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("test2");
        user2.addReview(new Review(user2,2,"asdad",product));

        try {
            String response = lambdaService.getSimilarity(user1, user2);
            return ResponseEntity.ok("Lambda response: " + response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calling Lambda: " + e.getMessage());
        }
    }
}

