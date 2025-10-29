package org.slayscale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    @Test
    void getId() {
        Review review = new Review(3, "Good product");
        review.setId(3L);
        assertEquals(3L, review.getId());
    }

    @Test
    void setId() {
        Review review = new Review(3, "Good product");
        review.setId(67L);
        assertEquals(67L, review.getId());
    }

    @Test
    void getRating() {
        Review review = new Review(3, "Good product");
        assertEquals(3, review.getRating());
    }

    @Test
    void setRating() {
        Review review = new Review(3, "Good product");

        assertThrows(IllegalArgumentException.class, () -> review.setRating(-1));
        assertThrows(IllegalArgumentException.class, () -> review.setRating(6));

        review.setRating(5);
        assertEquals(5, review.getRating());
        review.setRating(0);
        assertEquals(0, review.getRating());
    }

    @Test
    void getText() {
        Review review = new Review(3, "Good product");
        assertEquals("Good product", review.getText());
    }

    @Test
    void setText() {
        Review review = new Review(3, "Good product");

        review.setText("Great product");
        assertEquals("Great product", review.getText());

        review.setText("");
        assertEquals("", review.getText());

        assertThrows(IllegalArgumentException.class, () -> review.setText(null));
    }
}