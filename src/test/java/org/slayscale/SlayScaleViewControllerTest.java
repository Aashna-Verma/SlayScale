package org.slayscale;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SlayScaleViewController.class)
class SlayScaleViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserController userController;

    @MockBean
    private ProductController productController;

    @MockBean
    private LambdaController lambdaController;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Product product;

    @BeforeEach
    void setup() {
        user = new User("ibrahim@gmail.com", "Ibrahim");
        user.setId(1L);

        product = new Product(Category.ELECTRONICS,
                "AirPods",
                "https://apple.com/airpods",
                null);
        product.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
    }

    @Test
    void testCreateReviewWorkflowSuccess() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUserId", 1L);

        Map<String, Object> body = Map.of(
                "rating", 5,
                "text", "Amazing product!!"
        );

        Map<String, Object> respBody = Map.of("rating", 5, "text", "Amazing product!!");
        when(userController.createReview(eq(1L), anyMap()))
                .thenReturn(new ResponseEntity<>(respBody, HttpStatus.CREATED));

        mockMvc.perform(
                        post("/SlayScale/products/10/reviews")
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("rating", "5")
                                .param("text", "Amazing product!!")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/SlayScale/products/10*"));

    }

    @Test
    void testCreateReviewNotLoggedIn() throws Exception {
        mockMvc.perform(
                        post("/SlayScale/products/10/reviews")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("rating", "4")
                                .param("text", "Good")
                )
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testCreateReviewInvalidRating() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUserId", 1L);

        when(userController.createReview(eq(1L), anyMap()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mockMvc.perform(
                        post("/SlayScale/products/10/reviews")
                                .session(session)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("rating", "999")
                                .param("text", "Bad")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/SlayScale/products/10*"));
    }

}
