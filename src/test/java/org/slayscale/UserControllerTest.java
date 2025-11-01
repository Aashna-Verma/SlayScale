package org.slayscale;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        // Mock users
        user1 = new User("Alice");
        user1.setId(1L);

        user2 = new User("Bob");
        user2.setId(2L);

        // Mock review for user1
        Review review1 = new Review(user1, 5, "Great!");
        review1.setId(100L);
        user1.addReview(review1);
    }

    // Test retrieving all users
    @Test
    void testGetAllUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("Alice")))
                .andExpect(jsonPath("$[1].username", is("Bob")));
    }

    // Test retrieving a single user by ID when found
    @Test
    void testGetUserByIdFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("Alice")))
                .andExpect(jsonPath("$.reviews[0].rating", is(5)));
    }

    // Test retrieving a single user by ID when not found
    @Test
    void testGetUserByIdNotFound() throws Exception {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/3"))
                .andExpect(status().isNotFound());
    }

    // Test creating a new user successfully
    @Test
    void testCreateUserSuccess() throws Exception {
        Map<String, String> body = Map.of("username", "Charlie");
        User newUser = new User("Charlie");
        newUser.setId(3L);
        when(userRepository.findByUsername("Charlie")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(newUser);
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.username", is("Charlie")));
    }

    // Test creating a user with a duplicate username (failure)
    @Test
    void testCreateUserFailDuplicate() throws Exception {
        Map<String, String> body = Map.of("username", "Alice");
        when(userRepository.findByUsername("Alice")).thenReturn(Optional.of(user1));
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // Test updating an existing user successfully
    @Test
    void testUpdateUserSuccess() throws Exception {
        User updated = new User("AliceUpdated");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(updated);
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());
    }

    // Test updating a user that does not exist
    @Test
    void testUpdateUserNotFound() throws Exception {
        User updated = new User("AliceUpdated");
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isNotFound());
    }

    // Test deleting a user successfully
    @Test
    void testDeleteUserSuccess() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
        verify(userRepository).deleteById(1L);
    }

    // Test deleting a user that does not exist
    @Test
    void testDeleteUserNotFound() throws Exception {
        when(userRepository.existsById(99L)).thenReturn(false);
        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // Test following another user successfully
    @Test
    void testFollowUserSuccess() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        mockMvc.perform(post("/api/users/1/follow/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is now following")));
    }

    // Test following oneself (should fail)
    @Test
    void testFollowUserSelfFail() throws Exception {
        mockMvc.perform(post("/api/users/1/follow/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("cannot follow themselves")));
    }

    // Test unfollowing a user successfully
    @Test
    void testUnfollowUserSuccess() throws Exception {
        user1.follow(user2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        mockMvc.perform(post("/api/users/1/unfollow/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("has unfollowed")));
    }

    // Test getting followers of a user
    @Test
    void testGetFollowers() throws Exception {
        user2.follow(user1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("Bob")));
    }

    // Test getting users that the user is following
    @Test
    void testGetFollowing() throws Exception {
        user1.follow(user2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("Bob")));
    }

    // Test creating a review successfully
    @Test
    void testCreateReviewSuccess() throws Exception {
        Map<String, Object> body = Map.of("rating", 4, "text", "Nice!");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(post("/api/users/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(4)))
                .andExpect(jsonPath("$.text", is("Nice!")));
    }

    // Test retrieving all reviews of a user
    @Test
    void testGetReviews() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating", is(5)));
    }

    // Test deleting a user's review successfully
    @Test
    void testDeleteReviewSuccess() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(delete("/api/users/1/review/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Review deleted successfully")));

        verify(userRepository).save(user1);
    }

    // Test deleting a review that does not exist
    @Test
    void testDeleteReviewNotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(delete("/api/users/1/review/999"))
                .andExpect(status().isNotFound());
    }
}
