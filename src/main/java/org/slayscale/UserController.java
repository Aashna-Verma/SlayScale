package org.slayscale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            return ResponseEntity.status(201).body(userRepository.save(user));
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

    @PostMapping("/{id}/review")
    public ResponseEntity<Review> createReview(@PathVariable Long id, @RequestBody Review reviewRequest) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        if (reviewRequest == null || reviewRequest.getText() == null) {
            return ResponseEntity.badRequest().build();
        }
        Review review = new Review();
        review.setAuthor(user);
        review.setRating(reviewRequest.getRating());
        review.setText(reviewRequest.getText());
        user.addReview(review);
        userRepository.save(user);
        return ResponseEntity.status(201).body(review);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Set<Review>> getReviews(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
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
        user.getReviews().remove(reviewToDelete);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }
}
