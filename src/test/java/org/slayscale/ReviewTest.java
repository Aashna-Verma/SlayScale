package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {
    private Review review;

    @BeforeEach
    public void setUp() {
        review = new Review(3, "Good product");
    }

    @AfterEach
    public void tearDown() {
        review = null;
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
        assertNull(review.getAuthor());
    }

    @Test
    void setAuthor() {
        User user = new User("Jian_Yang");
        review.setAuthor(user);
        assertEquals("Jian_Yang", review.getAuthor().getUsername());

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