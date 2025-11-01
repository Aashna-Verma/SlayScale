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

    //View all users
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        userRepository.findAll().forEach(user -> users.add(convertUserToMap(user)));
        return ResponseEntity.ok(users);
    }

    //View user by id
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(convertUserToMap(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    //Helper method to format all user aspects
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("reviews", user.getReviews().stream()
                .map(r -> Map.of(
                        "reviewId", r.getId(),
                        "rating", r.getRating(),
                        "comment", r.getText()
                ))
                .collect(Collectors.toList()));
        map.put("followers", user.getFollowers().stream()
                .map(f -> Map.of(
                        "id", f.getId(),
                        "username", f.getUsername()
                ))
                .collect(Collectors.toList()));
        map.put("following", user.getFollowing().stream()
                .map(f -> Map.of(
                        "id", f.getId(),
                        "username", f.getUsername()
                ))
                .collect(Collectors.toList()));
        return map;
    }

    //Create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank() || userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User user;
        try {
            user = new User(username);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // Update username
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        try {
            user.setUsername(updatedUser.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Follow another user
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

        follower.follow(target);
        userRepository.save(follower);
        userRepository.save(target);
        return ResponseEntity.ok(follower.getUsername() + " is now following " + target.getUsername());
    }

    // Unfollow another user
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
        follower.unfollow(target);
        userRepository.save(follower);
        userRepository.save(target);
        return ResponseEntity.ok(follower.getUsername() + " has unfollowed " + target.getUsername());
    }

    // Get followers
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

    // Get following
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

    // Post review
    @PostMapping("/{id}/review")
    public ResponseEntity<Map<String, Object>> createReview(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        Integer rating = (Integer) body.get("rating");
        String text = (String) body.get("text");
        if (rating == null || text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating and text must be provided"));
        }
        Review review;
        try {
            review = new Review(user, rating, text);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        user.addReview(review);
        userRepository.save(user);
        Map<String, Object> response = Map.of(
                "rating", review.getRating(),
                "text", review.getText(),
                "authorId", user.getId()
        );
        return ResponseEntity.ok(response);
    }

    // Get reviews
    @GetMapping("/{id}/reviews")
    public ResponseEntity<Set<Map<String, ? extends Serializable>>> getReviews(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        Set<Map<String, ? extends Serializable>> reviews = user.getReviews().stream()
                .map(r -> Map.of(
                        "reviewId", r.getId(),
                        "rating", r.getRating(),
                        "text", r.getText()
                ))
                .collect(Collectors.toSet());
        return ResponseEntity.ok(reviews);
    }

    // Delete a specific review for a user
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
