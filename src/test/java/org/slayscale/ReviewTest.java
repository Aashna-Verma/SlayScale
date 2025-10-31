package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {
    private Review review;
    private User user;
    private Product product;

    @BeforeEach
    public void setUp() {
        user = new User("Jian_Yang");
        product = new Product(Category.ELECTRONICS, "http://costco.ca/led-patel-signs/p-24");
        review = new Review(user, 3, "Good product",product);
    }

    @Test
    void getId() {
        review.setId(3L);
        assertEquals(3L, review.getId());
    }

    @Test
    void setId() {
        review.setId(67L);
        assertEquals(67L, review.getId());
    }

    @Test
    void getAuthor() {
        assertEquals(user, review.getAuthor());
    }

    @Test
    void setAuthor() {
        User user2 = new User("Eric_Bachman");
        review.setAuthor(user2);
        assertEquals(user2, review.getAuthor());

        assertThrows(IllegalArgumentException.class, () -> review.setAuthor(null));
    }

    @Test
    void getRating() {
        assertEquals(3, review.getRating());
    }

    @Test
    void setRating() {
        assertThrows(IllegalArgumentException.class, () -> review.setRating(-1));
        assertThrows(IllegalArgumentException.class, () -> review.setRating(6));

        review.setRating(5);
        assertEquals(5, review.getRating());
        review.setRating(0);
        assertEquals(0, review.getRating());
    }

    @Test
    void getText() {
        assertEquals("Good product", review.getText());
    }

    @Test
    void setText() {
        review.setText("Great product");
        assertEquals("Great product", review.getText());

        review.setText("");
        assertEquals("", review.getText());

        assertThrows(IllegalArgumentException.class, () -> review.setText(null));
    }
}