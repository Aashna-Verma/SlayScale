package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

    @Test
    void create_and_get_by_id() {
        var c = controller();
        var created = c.create(new CreateProductDTO("https://p1.com", "ELECTRONICS"));
        assertNotNull(created.getId());

        var fetched = c.get(created.getId());
        assertNotNull(fetched);
        assertEquals("https://p1.com", fetched.getUrl());
        assertEquals(Category.ELECTRONICS, fetched.getCategory());
    }

    @Test
    void list_all_and_filter_by_category() {
        var c = controller();
        c.create(new CreateProductDTO("https://a.com", "BOOKS"));
        c.create(new CreateProductDTO("https://b.com", "ELECTRONICS"));

        assertEquals(2, c.list(null).size());                // all
        assertEquals(1, c.list("books").size());             // filter (case-insensitive)
        assertEquals(0, c.list("toys").size());              // invalid category -> empty list
    }

    @Test
    void update_changes_url_only() {
        var c = controller();
        var p = c.create(new CreateProductDTO("https://old.com", "ELECTRONICS"));

        var updated = c.update(p.getId(), new UpdateProductDTO("https://new.com", null));
        assertEquals("https://new.com", updated.getUrl());
        assertEquals(Category.ELECTRONICS, updated.getCategory()); // unchanged
    }

    @Test
    void delete_removes_item() {
        var c = controller();
        var p1 = c.create(new CreateProductDTO("https://x.com", "BOOKS"));
        c.create(new CreateProductDTO("https://y.com", "ELECTRONICS"));

        c.delete(p1.getId());
        assertEquals(1, c.list(null).size());
        assertThrows(ResponseStatusException.class, () -> c.get(p1.getId()));  // return 404
    }

    @Test
    void get_missing_throws_404() {
        ProductController c = controller();
        assertThrows(ResponseStatusException.class, () -> c.get(9999L)); // 404
    }

    @Test
    void create_invalid_category_throws_400() {
        ProductController c = controller();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.create(new CreateProductDTO("https://x.com", "GRADEN")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void create_blank_url_throws_400() {
        ProductController c = controller();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.create(new CreateProductDTO("   ", "ELECTRONICS")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void create_blank_category_throws_400() {
        ProductController c = controller();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.create(new CreateProductDTO("https://x.com", "   ")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void update_invalid_category_throws_400() {
        ProductController c = controller();
        Product p = c.create(new CreateProductDTO("https://x.com", "BOOKS"));
         ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.update(p.getId(), new UpdateProductDTO(null, "GARDEN")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void update_blank_url_throws_400() {
        ProductController c = controller();
        Product p = c.create(new CreateProductDTO("https://x.com", "BOOKS"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.update(p.getId(), new UpdateProductDTO("   ", null)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void update_no_fields_throws_400() {
        ProductController c = controller();
        Product p = c.create(new CreateProductDTO("https://x.com", "BOOKS"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.update(p.getId(), new UpdateProductDTO(null, null)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void delete_missing_id_throws_404() {
        ProductController c = controller();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.delete(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void reviews_missing_product_throws_404() {
        ProductController c = controller();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> c.reviews(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @org.springframework.transaction.annotation.Transactional
    @Test
    void reviews_existing_product_returns_empty_list_when_no_reviews() {
        ProductController c = controller();
        Product p = c.create(new CreateProductDTO("https://norev.com", "BOOKS"));
        assertTrue(c.reviews(p.getId()).isEmpty());
    }

}
