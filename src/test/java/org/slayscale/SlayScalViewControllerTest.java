package org.slayscale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SlayScaleViewControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void testAddProductFlowEndToEnd() throws Exception {

        mvc.perform(post("/SlayScale/products")
                        .param("category", "ELECTRONICS")
                        .param("url", "https://test.com"))
                .andExpect(status().is3xxRedirection());

        var all = productRepository.findAll();
        assert(all.size() == 1);

        mvc.perform(get("/SlayScale/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("https://test.com")));
    }

    @Test
    void testViewProductsPage() throws Exception {

        mvc.perform(get("/SlayScale/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void testViewUsersPage() throws Exception {

        mvc.perform(get("/SlayScale/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("sortStrategy"));
    }

    @Test
    void testProductDetailUsesFallbackWhenMinRatingIsNull() throws Exception {
        Product p = productRepository.save(
                new Product(Category.ELECTRONICS, "https://example.com")
        );

        mvc.perform(get("/SlayScale/products/" + p.getId())
                        .param("sort", "newest"))
                .andExpect(status().isOk())
                .andExpect(view().name("product_detail"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(model().attribute("minRating", Review.MIN_RATING))
                .andExpect(model().attribute("sort", "newest"));
    }

    @Test
    void testCreateReviewFlowEndToEnd() throws Exception {
        // 1. Create a product
        Product p = productRepository.save(
                new Product(Category.ELECTRONICS, "https://test.com")
        );

        // 2. Create a user
        User u = new User("test@example.com", "tester");
        u = userRepository.save(u);

        // 3. Submit a review (rating + text)
        mvc.perform(post("/SlayScale/products/" + p.getId() + "/reviews")
                        .param("rating", "5")
                        .param("text", "Amazing product!")
                        .sessionAttr("currentUserId", u.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/SlayScale/products/" + p.getId() + "?success=Review+posted."));

        // 4. Verify the review appears
        mvc.perform(get("/SlayScale/products/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product_detail"))
                .andExpect(model().attributeExists("reviews"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Amazing product!")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("(5/5)")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("tester")));
    }

}
