package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Test
    void getProduct() {
        assertEquals(product, review.getProduct());
    }

    @Test
    void setProduct() {
        Product product2 = new Product(Category.BOOKS, "http://amazon.com/some-book/p-45");
        review.setProduct(product2);
        assertEquals(product2, review.getProduct());

        assertThrows(IllegalArgumentException.class, () -> review.setProduct(null));
    }
    @Test
    void sortReviewsByRatingDescending() {
        Review r1 = new Review(user, 3, "okay", product);
        r1.setId(1L);
        Review r2 = new Review(user, 5, "great", product);
        r2.setId(2L);
        Review r3 = new Review(user, 1, "bad", product);
        r3.setId(3L);

        List<Review> reviews = Arrays.asList(r1, r2, r3);
        reviews = reviews.stream()
                .sorted(Comparator.comparingInt(Review::getRating).reversed()
                        .thenComparing(Review::getId, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        assertEquals(5, reviews.get(0).getRating());
        assertEquals(3, reviews.get(1).getRating());
        assertEquals(1, reviews.get(2).getRating());
    }

    @Test
    void filterReviewsByMinimumRating() {
        Review r1 = new Review(user, 5, "amazing", product);
        Review r2 = new Review(user, 4, "good", product);
        Review r3 = new Review(user, 2, "meh", product);
        Review r4 = new Review(user, 1, "bad", product);

        List<Review> reviews = Arrays.asList(r1, r2, r3, r4);

        int minRating = 4;
        List<Review> filtered = reviews.stream()
                .filter(r -> r.getRating() >= minRating)
                .collect(Collectors.toList());

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(r -> r.getRating() >= 4));
    }
}