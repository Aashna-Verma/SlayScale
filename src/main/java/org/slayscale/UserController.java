package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    public UserController(UserRepository userRepository, ProductRepository productRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = (List<User>) userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank() || userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            User user = new User(username);
            return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<Set<Map<String, ? extends Serializable>>> getFollowers(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    Set<Map<String, ? extends Serializable>> followers = user.getFollowers().stream()
                            .map(f -> Map.of(
                                    "id", f.getId(),
                                    "username", f.getUsername()
                            ))
                            .collect(Collectors.toSet());
                    return ResponseEntity.ok(followers);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<Set<Map<String, ? extends Serializable>>> getFollowing(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    Set<Map<String, ? extends Serializable>> following = user.getFollowing().stream()
                            .map(f -> Map.of(
                                    "id", f.getId(),
                                    "username", f.getUsername()
                            ))
                            .collect(Collectors.toSet());
                    return ResponseEntity.ok(following);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/review")
    public ResponseEntity<Map<String, Object>> createReview(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Long productId = Long.valueOf(body.get("productId").toString());
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        Product product = optionalProduct.get();
        String text = (String) body.get("text");
        Integer rating = (Integer) body.get("rating");
        if (text == null) {
            return ResponseEntity.badRequest().build();
        }
        Review review = new Review(user, rating, text, product);
        user.addReview(review);
        product.addReview(review);
        userRepository.save(user);
        productRepository.save(product);
        Map<String, Object> response = new HashMap<>();
        response.put("rating", review.getRating());
        response.put("text", review.getText());
        response.put("productId", review.getProduct().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/follow/{targetId}")
    public ResponseEntity<String> followUser(@PathVariable Long id, @PathVariable Long targetId) {
        if (id.equals(targetId)) {
            return ResponseEntity.badRequest().body("User cannot follow themselves");
        }
        Optional<User> followerOpt = userRepository.findById(id);
        Optional<User> targetOpt = userRepository.findById(targetId);
        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User follower = followerOpt.get();
        User target = targetOpt.get();
        if (follower.getFollowing().contains(target)) {
            return ResponseEntity.ok(follower.getUsername() + " is already following " + target.getUsername());
        }
        follower.follow(target);
        userRepository.save(follower);
        userRepository.save(target);
        return ResponseEntity.ok(follower.getUsername() + " is now following " + target.getUsername());
    }

    @PostMapping("/{id}/unfollow/{targetId}")
    public ResponseEntity<String> unfollowUser(@PathVariable Long id, @PathVariable Long targetId) {
        if (id.equals(targetId)) {
            return ResponseEntity.badRequest().body("User cannot unfollow themselves");
        }
        Optional<User> followerOpt = userRepository.findById(id);
        Optional<User> targetOpt = userRepository.findById(targetId);
        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User follower = followerOpt.get();
        User target = targetOpt.get();
        if (!follower.getFollowing().contains(target)) {
            return ResponseEntity.ok(follower.getUsername() + " isn't following " + target.getUsername());
        }
        follower.unfollow(target);
        userRepository.save(follower);
        userRepository.save(target);
        return ResponseEntity.ok(follower.getUsername() + " has unfollowed " + target.getUsername());
    }

    @GetMapping("/{userId}/reviews")
    public ResponseEntity<Set<Review>> getReviews(@PathVariable Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        return ResponseEntity.ok(user.getReviews());
    }

    @DeleteMapping("/{userId}/review/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteUserReview(@PathVariable Long userId, @PathVariable Long reviewId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        Review reviewToDelete = user.getReviews().stream().filter(r -> r.getId().equals(reviewId)).findFirst().orElse(null);
        if (reviewToDelete == null) {
            return ResponseEntity.notFound().build();
        }
        Long productId = reviewToDelete.getProduct().getId();
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product =  optionalProduct.get();
        user.removeReview(reviewToDelete);
        product.removeReview(reviewToDelete);
        userRepository.save(user);
        productRepository.save(product);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }
}
