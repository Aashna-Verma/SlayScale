package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public UserController(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(required = false, defaultValue = "DEFAULT") UserSortStrategy sortStrategy,
            @RequestParam(required = false) Long baseUserId) {
        List<User> users = (List<User>) userRepository.findAll();

        // Use a registry to store sorting strategies with lambdas to implement them.
        // Sorting strategies that don't need a base user ID can simply ignore it.
        // Default strategy does not need to be implemented because we just return users.
        final Map<UserSortStrategy, BiConsumer<List<User>, Long>> sortStrategyRegistry = Map.of(

                UserSortStrategy.SIMILARITY, (list, id) -> {
                    if (id == null) {
                        throw new IllegalArgumentException("baseUserId is required for SIMILARITY sorting");
                    }
                    userRepository.findById(id).ifPresent(baseUser ->
                            list.sort((u1, u2) -> Double.compare(
                                    u2.getSimilarity(baseUser),
                                    u1.getSimilarity(baseUser)
                            ))
                    );
                }
                // Register more sorting strategies here:
        );

        try {
            sortStrategyRegistry
                    .getOrDefault(sortStrategy, (l, i) -> {})
                    .accept(users, baseUserId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/similarUsers")
    public ResponseEntity<List<Map<String, Object>>> getSimilarUsers(@PathVariable Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User baseUser = optionalUser.get();
        List<Map<String, Object>> similarUsers = new ArrayList<>();
        List<User> users = (List<User>) userRepository.findAll();
        for (User user : users) {
            if (user.equals(baseUser)) {
                continue;
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("similarity", user.getSimilarity(baseUser));
            similarUsers.add(userMap);
        }

        // sort by similarity descending
        similarUsers.sort(Comparator.comparing(u -> (Double) u.get("similarity"), Comparator.reverseOrder()));
        return ResponseEntity.ok(similarUsers);
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
