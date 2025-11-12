package org.slayscale;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


public class ProductTest {
    private Product product;
    private Review review;
    private User user;

    @BeforeEach
    public void setUp() {
        product = new Product(Category.ELECTRONICS, "http://costco.ca/led-patel-signs/p-24");
        user = new User("rajesh");
        review = new Review(user, 4, "vhat a beautiful sign", product);
    }

    @Test
    public void equals() {
        Product p2 = new Product(Category.ELECTRONICS, "http://costco.ca/led-patel-signs/p-24");
        assertEquals(product, p2); // products with same URL should be equal
        assertEquals(product.hashCode(), p2.hashCode()); // hashCode must match

        Product p3 = new Product(Category.ELECTRONICS, "http://a");
        assertNotEquals(product, p3);
        assertNotEquals(product.hashCode(), p3.hashCode());
    }

    @Test
    public void getSetId() {
        product.setId(10L);
        assert (product.getId() == 10L);
    }

    @Test
    public void getSetCategory() {
        assert (product.getCategory() == Category.ELECTRONICS);
        product.setCategory(Category.BOOKS);
        assert (product.getCategory() == Category.BOOKS);
        assertThrows(IllegalArgumentException.class, () -> product.setCategory(null));
    }

    @Test
    public void setUrl() {
        assert (product.getUrl().equals("http://costco.ca/led-patel-signs/p-24"));
        product.setUrl("http://amazon.ca/kindle/p-99");
        assert (product.getUrl().equals("http://amazon.ca/kindle/p-99"));
        assertThrows(IllegalArgumentException.class, () -> product.setUrl(" "));
    }

    @Test
    public void getSetReviews() {
        assert (product.getReviews().isEmpty());
        product.getReviews().add(review);
        assert (product.getReviews().size() == 1);
        assertThrows(IllegalArgumentException.class, () -> product.setReviews(null));
    }

    @Test
    public void addReview() {
        product.addReview(review);
        assert (product.getReviews().size() == 1);
        assert (review.getProduct() == product);
        assertThrows(IllegalArgumentException.class, () -> product.addReview(null));
    }

    @Test
    public void removeReview() {
        product.addReview(review);
        product.removeReview(review);
        assert (product.getReviews().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> product.removeReview(null));
    }
}
