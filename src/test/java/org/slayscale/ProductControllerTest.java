package org.slayscale;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void clean() { repo.deleteAll(); }

    private ProductController controller() {
        return new ProductController(repo);
    }

    private Product createProduct(ProductController c, String url, String category) {
        ResponseEntity<Product> res = c.createProduct(Map.of("url", url, "category", category));
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNotNull(res.getBody());
        return res.getBody();
    }

    @Test
    void createProductAndGetProductById() {
        var c = controller();
        var created = createProduct(c, "https://p1.com", "ELECTRONICS");

        var fetched = c.getProduct(created.getId());
        assertEquals("https://p1.com", fetched.getUrl());
        assertEquals(Category.ELECTRONICS, fetched.getCategory());
    }

    @Test
    void listProductsReturnsAllAndFiltersByCategory() {
        var c = controller();
        createProduct(c, "https://a.com", "BOOKS");
        createProduct(c, "https://b.com", "ELECTRONICS");

        assertEquals(2, c.listProducts(null).size());
        assertEquals(1, c.listProducts("books").size());
        assertEquals(0, c.listProducts("toys").size());
    }

    @Test
    void deleteProductRemovesItem() {
        var c = controller();
        var p1 = createProduct(c, "https://x.com", "BOOKS");
        createProduct(c, "https://y.com", "ELECTRONICS");

        c.deleteProduct(p1.getId());
        assertEquals(1, c.listProducts(null).size());
        assertThrows(ResponseStatusException.class, () -> c.getProduct(p1.getId()));
    }

    @Test
    void getProductMissingProduct() {
        var c = controller();
        assertThrows(ResponseStatusException.class, () -> c.getProduct(9999L));
    }

    @Test
    void createProductInvalidCategory() {
        var c = controller();
        var res = c.createProduct(Map.of("url", "https://x.com", "category", "GARDEN"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void createProductBlankUrl() {
        var c = controller();
        var res = c.createProduct(Map.of("url", "   ", "category", "ELECTRONICS"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void createProductBlankCategory() {
        var c = controller();
        var res = c.createProduct(Map.of("url", "https://x.com", "category", "   "));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void deleteProductMissingId() {
        var c = controller();
        var ex = assertThrows(ResponseStatusException.class,
                () -> c.deleteProduct(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getProductReviewsMissingProduct() {
        var c = controller();
        var res = c.getProductReviews(99999L);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Transactional
    @Test
    void getProductReviewsExistingProductReturnsEmptySetWhenNoReviews() {
        var c = controller();
        var p = createProduct(c, "https://norev.com", "BOOKS");

        var res = c.getProductReviews(p.getId());
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().isEmpty());
    }
}
