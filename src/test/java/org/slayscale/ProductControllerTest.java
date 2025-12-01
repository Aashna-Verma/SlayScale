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
import java.util.Set;

@DataJpaTest
class ProductControllerAssertTests {

    @Autowired
    ProductRepository repo;

    @Autowired
    UserRepository userRepository;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController(repo, userRepository);
    }

    @AfterEach
    void clean() { repo.deleteAll(); }

    private ProductController controller() {
        return new ProductController(repo, userRepository);
    }

    private Product createProduct(ProductController controller, String name, String url, String imageUrl, String category) {
        ResponseEntity<Product> res = controller.createProduct(
                Map.of("url", url,
                        "category", category,
                        "name", name,
                        "imageUrl", imageUrl));
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNotNull(res.getBody());
        return res.getBody();
    }

    @Test
    void createExistingProduct() {
        ResponseEntity<Product> firstResponse = controller.createProduct(
                Map.of("url", "https://duplicate.com",
                        "category", "ELECTRONICS",
                        "name", "duplicate",
                        "imageUrl", "")
        );
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());
        assertNotNull(firstResponse.getBody());

        ResponseEntity<?> secondResponse = controller.createProduct(
                Map.of("url", "https://duplicate.com",
                        "category", "ELECTRONICS",
                        "name", "duplicate",
                        "imageUrl", "")
        );

        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
    }

    @Test
    void createProductAndGetProductById() {
        var created = createProduct(controller, "product p1", "https://p1.com", "https://image","ELECTRONICS");

        var fetched = controller.getProduct(created.getId());
        assertEquals("https://p1.com", fetched.getUrl());
        assertEquals(Category.ELECTRONICS, fetched.getCategory());
    }

    @Test
    void listProductsNoCategoryReturnsAll() {
        createProduct(controller, "product a", "https://a.com", "https://image","BOOKS");
        createProduct(controller, "product b", "https://b.com", "https://image","ELECTRONICS");

        assertEquals(2, controller.listProducts(null).size());
        assertEquals(2, controller.listProducts("").size());
    }

    @Test
    void listProductsValidCategoryFiltersCorrectly() {
        createProduct(controller, "product a", "https://a.com", "https://image","BOOKS");
        createProduct(controller, "product b", "https://b.com", "https://image","ELECTRONICS");

        assertEquals(1, controller.listProducts("BOOKS").size());
        assertEquals(1, controller.listProducts("books").size());
        assertEquals(1, controller.listProducts("   books   ").size());

        var onlyBooks = controller.listProducts("books");
        assertEquals(Category.BOOKS, onlyBooks.get(0).getCategory());
    }

    @Test
    void listProductsInvalidCategoryReturnsAll() {
        createProduct(controller, "product a", "https://a.com", "https://image","BOOKS");
        createProduct(controller, "product b", "https://b.com", "https://image","ELECTRONICS");

        assertEquals(0, controller.listProducts("toys").size());
    }


    @Test
    void deleteProductRemovesItem() {
        var p1 = createProduct(controller, "product x", "https://x.com", "https://image", "BOOKS");
        createProduct(controller, "product y", "https://y.com", "https://image","ELECTRONICS");

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
        var res = controller.createProduct(
                Map.of("url", "https://x.com",
                        "name", "Product X",
                        "imageUrl", "https://image",
                        "category", "GARDEN"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void createProductBlankUrl() {
        var res = controller.createProduct(
                Map.of("url", "   ",
                        "name", "Product X",
                        "imageUrl", "https://image",
                        "category", "ELECTRONICS"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void createProductBlankCategory() {
        var res = controller.createProduct(
                Map.of("url", "https://x.com",
                        "name", "Product X",
                        "imageUrl", "https://image",
                        "category", "   "));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void createProductBlankName() {
        var res = controller.createProduct(
                Map.of("url", "https://x.com",
                        "name", "   ",
                        "imageUrl", "https://image",
                        "category", "ELECTRONICS"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void deleteProductMissingId() {
        var ex = assertThrows(ResponseStatusException.class,
                () -> controller.deleteProduct(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getProductReviewsSimilarityWithoutBaseUserReturnsBadRequest() {
        var p = createProduct(controller, "Product name", "https://similar-nobase.com", "https://image","BOOKS");

        ResponseEntity<Set<Review>> res =
                controller.getProductReviews(p.getId(), "similarity", 0, null);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }
    @Test
    void getProductReviewsSimilarityWithUnknownUserReturnsBadRequest() {
        var p = createProduct(controller, "Product name", "https://similar-unknown.com", "https://image","BOOKS");

        ResponseEntity<Set<Review>> res =
                controller.getProductReviews(p.getId(), "similarity", 0, 99999L);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }
}
