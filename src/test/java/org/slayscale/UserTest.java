package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("Jian_Yang");
    }

    @Test
    void getSimilarity() {
        Product p1 = new Product(Category.BOOKS, "https://a.co/d/emAuUNh");
        Product p2 = new Product(Category.BOOKS, "https://a.co/d/fJIJBiG");
        Product p3 = new Product(Category.BOOKS, "https://a.co/d/3UzLhtu");
        Product p4 = new Product(Category.BOOKS, "https://a.co/d/6l2BKIa");

        User u1 = new User("Jian_Yang");
        User u2 = new User("Gavin_Belson");

        Review r1 = new Review(u1, 4, "pretty good", p1);
        Review r2 = new Review(u1, 3, "ok", p2);
        Review r3 = new Review(u1, 2, "bad", p3);

        Review r4 = new Review(u2, 5, "amazing", p1);
        Review r5 = new Review(u2, 3, "ok", p2);
        Review r6 = new Review(u2, 4, "great", p3);
        Review r7 = new Review(u2, 5, "the best", p4);

        Review r8 = new Review(u1, 5, "the best again", p4);
        assertEquals(0.0d, u1.getSimilarity(u2));

        u1.addReview(r1);
        u1.addReview(r2);
        u1.addReview(r3);

        u2.addReview(r7);
        assertEquals(0.0d, u1.getSimilarity(u2));

        u2.addReview(r4);
        assertEquals(0.25d, u1.getSimilarity(u2));

        u2.addReview(r5);
        u2.addReview(r6);
        assertEquals(0.75d, u1.getSimilarity(u2));

        u1.addReview(r8);
        assertEquals(1.0d, u1.getSimilarity(u2));
    }

    @Test
    void getSetId() {
        user.setId(1L);
        assertEquals(1L, user.getId());
    }

    @Test
    void getSetUsername() {
        assertEquals("Jian_Yang", user.getUsername());
        user.setUsername("Eric_Bachman");
        assertEquals("Eric_Bachman", user.getUsername());

        assertThrows(IllegalArgumentException.class, () -> user.setUsername(null));

        // Too short (empty string)
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(""));

        // Too short (less than 3 characters)
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("R"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Di"));

        // Too long (over 40 characters)
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("AlwaysBlueAlwaysBlueAlwaysBlueAlwaysBlueAlwaysBlue"));

        // Starts with a hyphen or underscore
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("-RichardHendricks"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("_Dinesh"));

        // Ends with a hyphen or underscore
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Gilfoyle-"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("EricBachman_"));

        // Contains spaces or tabs
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Jared Dunn"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Hooli\tCorp"));

        // Contains consecutive hyphens or underscores
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Pied--Piper"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Hooli__XYZ"));

        // Contains invalid special characters
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Big.Head"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Richard@Piper"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("PiedPiper!"));

        // Leading or trailing spaces
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(" Gilfoyle"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("EricBachman "));
    }

    @Test
    void getSetReviews() {
        assertEquals(0, user.getReviews().size());
        Product product = new Product();
        Review review = new Review(user, 2, "description", product);
        Set<Review> reviews = new HashSet<>();
        reviews.add(review);
        
        user.setReviews(reviews);
        assertEquals(1, user.getReviews().size());

        assertThrows(IllegalArgumentException.class, () -> user.setReviews(null));
    }

    @Test
    void addRemoveReview() {
        assertEquals(0, user.getReviews().size());
        Product product = new Product();
        Review review = new Review(user, 2, "description", product); // add a review
        user.addReview(review);
        assertEquals(1, user.getReviews().size());
        assertEquals(user, review.getAuthor());

        user.addReview(review); // duplicate added review
        assertEquals(1, user.getReviews().size());
        
        user.removeReview(review); // remove a review
        assertEquals(0, user.getReviews().size());

        assertThrows(IllegalArgumentException.class, () -> user.addReview(null));
        assertThrows(IllegalArgumentException.class, () -> user.removeReview(null));
    }

    @Test
    void getSetIncrementDecrementFollowersCount() {
        assertEquals(0, user.getFollowerCount());

        user.setFollowerCount(1);
        assertEquals(1, user.getFollowerCount());

        user.incrementFollowerCount();
        assertEquals(2, user.getFollowerCount());

        user.decrementFollowerCount();
        assertEquals(1, user.getFollowerCount());

        assertThrows(IllegalArgumentException.class, () -> user.setFollowerCount(-1));
    }

    @Test
    void getSetIncrementDecrementFollowingCount() {
        assertEquals(0, user.getFollowingCount());

        user.setFollowingCount(1);
        assertEquals(1, user.getFollowingCount());

        user.incrementFollowingCount();
        assertEquals(2, user.getFollowingCount());

        user.decrementFollowingCount();
        assertEquals(1, user.getFollowingCount());

        assertThrows(IllegalArgumentException.class, () -> user.setFollowingCount(-1));
    }

    @Test
    void getSetIncrementDecrementFollowing() {
        assertEquals(0, user.getFollowing().size());

        User user2 = new User("Eric_Bachman");
        Set<User> following = new HashSet<>();
        following.add(user2);

        user.setFollowing(following);
        assertEquals(1, user.getFollowing().size());

        assertThrows(IllegalArgumentException.class, () -> user.setFollowing(null));
    }

    @Test
    void followUnfollowRemoveFollower() {
        assertEquals(0, user.getFollowingCount());
        assertEquals(0, user.getFollowerCount());

        User user2 = new User("Eric_Bachman");
        User user3 = new User("Russ_Hanneman");

        user.follow(user2); // Jian follows Eric
        assertEquals(1, user.getFollowingCount());
        assertEquals(0, user.getFollowerCount());
        assertEquals(0, user2.getFollowingCount());
        assertEquals(1, user2.getFollowerCount());

        user.follow(user3); // Jian follows Russ
        assertEquals(2, user.getFollowingCount());
        assertEquals(0, user.getFollowerCount());
        assertEquals(0, user3.getFollowingCount());
        assertEquals(1, user3.getFollowerCount());

        user.follow(user3); // Jian follows Russ Hanneman
        assertEquals(2, user.getFollowingCount());
        assertEquals(1, user3.getFollowerCount());

        user.unfollow(user2); // Jian unfollows Eric
        assertEquals(1, user.getFollowingCount());
        assertEquals(0, user.getFollowerCount());
        assertEquals(0, user2.getFollowerCount());
        assertEquals(0, user2.getFollowerCount());

        user3.removeFollower(user); // Russ removes Jian
        assertEquals(0, user.getFollowingCount());
        assertEquals(0, user.getFollowerCount());
        assertEquals(0, user2.getFollowingCount());
        assertEquals(0, user2.getFollowerCount());
        assertEquals(0, user3.getFollowingCount());
        assertEquals(0, user3.getFollowerCount());

        assertThrows(IllegalArgumentException.class, () -> user.follow(null));
        assertThrows(IllegalArgumentException.class, () -> user.unfollow(null));
        assertThrows(IllegalArgumentException.class, () -> user.removeFollower(null));

        assertThrows(IllegalArgumentException.class, () -> user.follow(user));
        assertThrows(IllegalArgumentException.class, () -> user.unfollow(user));
        assertThrows(IllegalArgumentException.class, () -> user.removeFollower(user));
    }
}
