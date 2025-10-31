package org.slayscale;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertThrows;


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
    public void testConstructor() {
        assert (product.getCategory() == Category.ELECTRONICS);
        assert (product.getUrl().equals("http://costco.ca/led-patel-signs/p-24"));
        assert (product.getReviews().isEmpty());
    }

    @Test
    public void testConstructorNull() {
        assertThrows(IllegalArgumentException.class, () -> new Product(null, "https://test.com"));
    }

    @Test
    public void testSetCategoryNull() {
        assertThrows(IllegalArgumentException.class, () -> product.setCategory(null));
    }

    @Test
    public void testSetUrlNull() {
        assertThrows(IllegalArgumentException.class, () -> product.setUrl(null));
    }

    @Test
    public void testSetRatingNull() {
        assertThrows(IllegalArgumentException.class, () -> product.setUrl(null));
    }

    @Test
    public void testReviewsGetSet() {
        assert (product.getReviews().isEmpty());

        product.getReviews().add(review);
        assert (product.getReviews().size() == 1);
    }

    @Test
    public void testSetReviewsNull() {
        assertThrows(IllegalArgumentException.class, () -> product.setReviews(null));
    }

    @Test
    public void testAddReview() {
        product.addReview(review);
        assert (product.getReviews().size() == 1);
        assert (review.getProduct() == product);
    }

    @Test
    public void testAddReviewNull() {
        assertThrows(IllegalArgumentException.class, () -> product.addReview(null));
    }

    @Test
    public void testRemoveReview() {
        product.addReview(review);
        product.removeReview(review);
        assert (product.getReviews().isEmpty());
    }

    @Test
    public void testRemoveReviewNull() {
        assertThrows(IllegalArgumentException.class, () -> product.removeReview(null));
    }

    @Test
    public void testToString() {
        String expected = "Product[id=null, category=ELECTRONICS, url='http://costco.ca/led-patel-signs/p-24', reviews=[]]";
        System.out.println(product.toString());
        assert (product.toString().equals(expected));
    }


}
