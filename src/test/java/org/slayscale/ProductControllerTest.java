package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductControllerAssertTests {

    @Autowired
    ProductRepository repo;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController(repo);
    }

    @AfterEach
    void clean() { repo.deleteAll(); }

    private ProductController controller() {
        return new ProductController(repo);
    }

    private Product createProduct(ProductController controller, String url, String category) {
        ResponseEntity<Product> res = controller.createProduct(Map.of("url", url, "category", category));
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNotNull(res.getBody());
        return res.getBody();
    }

    @Test
    void createExistingProduct() {
        ResponseEntity<Product> firstResponse = controller.createProduct(
                Map.of("url", "https://duplicate.com", "category", "ELECTRONICS")
        );
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());
        assertNotNull(firstResponse.getBody());

        ResponseEntity<?> secondResponse = controller.createProduct(
                Map.of("url", "https://duplicate.com", "category", "ELECTRONICS")
        );

        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
    }

    @Test
    void createProductAndGetProductById() {
        var created = createProduct(controller, "https://p1.com", "ELECTRONICS");

        var fetched = controller.getProduct(created.getId());
        assertEquals("https://p1.com", fetched.getUrl());
        assertEquals(Category.ELECTRONICS, fetched.getCategory());
    }

    @Test
    void listProductsReturnsAllAndFiltersByCategory() {
        createProduct(controller, "https://a.com", "BOOKS");
        createProduct(controller, "https://b.com", "ELECTRONICS");

        assertEquals(2, controller.listProducts(null).size());
        assertEquals(1, controller.listProducts("books").size());
        assertEquals(0, controller.listProducts("toys").size());
    }

    @Test
    void deleteProductRemovesItem() {
        var p1 = createProduct(controller, "https://x.com", "BOOKS");
        createProduct(controller, "https://y.com", "ELECTRONICS");

        controller.deleteProduct(p1.getId());
        assertEquals(1, controller.listProducts(null).size());
        assertThrows(ResponseStatusException.class, () -> controller.getProduct(p1.getId()));
    }

    @Test
    void getProductMissingProduct() {
        assertThrows(ResponseStatusException.class, () -> controller.getProduct(9999L));
    }

    @Test
    void createProductInvalidCategory() {
        var res = controller.createProduct(Map.of("url", "https://x.com", "category", "GARDEN"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void createProductBlankUrl() {
        var res = controller.createProduct(Map.of("url", "   ", "category", "ELECTRONICS"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void createProductBlankCategory() {
        var res = controller.createProduct(Map.of("url", "https://x.com", "category", "   "));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void deleteProductMissingId() {
        var ex = assertThrows(ResponseStatusException.class,
                () -> controller.deleteProduct(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getProductReviewsMissingProduct() {
        var res = controller.getProductReviews(99999L);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Transactional
    @Test
    void getProductReviewsExistingProductReturnsEmptySetWhenNoReviews() {
        var p = createProduct(controller, "https://norev.com", "BOOKS");

        var res = controller.getProductReviews(p.getId());
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().isEmpty());
    }


}
