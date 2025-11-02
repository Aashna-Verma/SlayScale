package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;

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
        assertNull(c.get(p1.getId())); // returns null when missing
    }

    @Test
    void get_missing_returns_null() {
        var c = controller();
        assertNull(c.get(9999L));
    }
}
