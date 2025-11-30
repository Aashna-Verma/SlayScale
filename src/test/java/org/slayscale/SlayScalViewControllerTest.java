package org.slayscale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SlayScaleViewController.class)
class SlayScaleViewControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProductController productController;

    @MockBean
    private UserController userController;

    @MockBean
    private LambdaController lambdaController;

    @Test
    void testAddProductFlowSuccess() throws Exception {

        Product mock = new Product(Category.ELECTRONICS, "https://test.com");
        mock.setId(1L);

        when(productController.createProduct(
                Map.of("category", "ELECTRONICS", "url", "https://test.com")
        )).thenReturn(new ResponseEntity<>(mock, HttpStatus.CREATED));

        mvc.perform(post("/SlayScale/products")
                        .param("category", "ELECTRONICS")
                        .param("url", "https://test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/SlayScale/products?success=Product+created."));
    }

    @Test
    void testAddProductFlowInvalid() throws Exception {

        when(productController.createProduct(any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/SlayScale/products")
                        .param("category", "BAD")
                        .param("url", "invalid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/SlayScale/products?error=Invalid+category+or+URL."));
    }
}
