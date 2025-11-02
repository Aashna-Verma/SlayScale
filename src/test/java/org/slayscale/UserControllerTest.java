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

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;
    private User user2;
    private Product product1;
    private Product product2;
    private Review review1;

    @BeforeEach
    void setup() {
        // Mock users
        user1 = new User("Alice");
        user1.setId(1L);

        user2 = new User("Bob");
        user2.setId(2L);

        //Mock product
        product1 = new Product(Category.ELECTRONICS, "https://example.com/product");
        product1.setId(50L);

        product2 = new Product(Category.BOOKS, "https://example.com/book");
        product2.setId(51L);

        // Mock review for user1
        review1 = new Review(user1, 5, "Great!", product1);
        review1.setId(100L);
        user1.addReview(review1);
    }
    
    @Test
    void testGetAllUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("Alice")))
                .andExpect(jsonPath("$[1].username", is("Bob")));
    }

    @Test
    void testGetUserByIdFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("Alice")))
                .andExpect(jsonPath("$.reviews[0].rating", is(5)));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        Map<String, String> body = Map.of("username", "Charlie");
        when(userRepository.findByUsername("Charlie")).thenReturn(Optional.empty());
        User savedUser = new User("Charlie");
        savedUser.setId(3L);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(savedUser);
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated()) // endpoint returns 201
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.username", is("Charlie")));
    }

    @Test
    void testCreateUserFailDuplicate() throws Exception {
        Map<String, String> body = Map.of("username", "Alice");
        when(userRepository.findByUsername("Alice")).thenReturn(Optional.of(user1));
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        when(userRepository.existsById(99L)).thenReturn(false);
        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFollowers() throws Exception {
        user2.follow(user1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("Bob")));
    }

    @Test
    void testGetFollowing() throws Exception {
        user1.follow(user2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("Bob")));
    }

    @Test
    void testCreateReviewSuccess() throws Exception {
        // Prepare review request (we only need rating, text, and product for the controller)
        Review reviewRequest = new Review();
        reviewRequest.setRating(2);
        reviewRequest.setText("Meh!");
        reviewRequest.setProduct(product2); // product2 should have an ID set
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(post("/api/users/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(2))
                .andExpect(jsonPath("$.text").value("Meh!"))
                .andExpect(jsonPath("$.productId").value(product2.getId()));
    }

    @Test
    void testGetReviews() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(get("/api/users/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating", is(5)));
    }

    @Test
    void testDeleteReviewSuccess() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(delete("/api/users/1/review/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Review deleted successfully")));

        verify(userRepository).save(user1);
    }

    @Test
    void testDeleteReviewNotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        mockMvc.perform(delete("/api/users/1/review/999"))
                .andExpect(status().isNotFound());
    }
}
