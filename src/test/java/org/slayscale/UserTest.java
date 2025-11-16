package org.slayscale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private User u1;

    @BeforeEach
    public void setUp() {
        u1 = new User("jianyang@piedpiper.com", "Jian_Yang");
    }

    @Test
    void getSimilarity() {
        Product p1 = new Product(Category.BOOKS, "https://a.co/d/emAuUNh");
        Product p2 = new Product(Category.BOOKS, "https://a.co/d/fJIJBiG");
        Product p3 = new Product(Category.BOOKS, "https://a.co/d/3UzLhtu");
        Product p4 = new Product(Category.BOOKS, "https://a.co/d/6l2BKIa");

        User u2 = new User("gavinbelson@hooli.com", "Gavin_Belson");

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
        u1.setId(1L);
        assertEquals(1L, u1.getId());
    }

    @Test
    void getSetEmail() {
        assertEquals("jianyang@piedpiper.com", u1.getEmail());

        // Valid update
        u1.setEmail("ericbachman@piedpiper.com");
        assertEquals("ericbachman@piedpiper.com", u1.getEmail());

        // Null
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail(null));

        // Empty string
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail(""));

        // Missing @
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("richardgmail.com"));

        // Missing domain
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("richard@"));

        // Missing TLD (.com, .ca)
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("richard@hooli"));

        // Invalid characters in local part
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("richard!hendricks@piper.com"));
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("gilfoyle#system@hooli.xyz"));

        // Spaces
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("big head@hooli.com"));
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("gilfoyle@ hooli.com"));
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("    jared@hooli.com"));

        // Consecutive dots
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("richard..hendricks@piper.com"));

        // Starts with a dot or hyphen
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail(".dinesh@aviato.com"));
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("-dinesh@aviato.com"));

        // Domain starting or ending with hyphen
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("dinesh@-aviato.com"));
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("dinesh@aviato-.com"));

        // Invalid TLD (1-char)
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("jared@hooli.c"));

        // Tab characters
        assertThrows(IllegalArgumentException.class, () -> u1.setEmail("jared\t@hooli.com"));
    }

    @Test
    void getSetUsername() {
        assertEquals("Jian_Yang", u1.getUsername());
        u1.setUsername("Eric_Bachman");
        assertEquals("Eric_Bachman", u1.getUsername());

        assertThrows(IllegalArgumentException.class, () -> u1.setUsername(null));

        // Too short (empty string)
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername(""));

        // Too short (less than 3 characters)
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("R"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Di"));

        // Too long (over 40 characters)
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("AlwaysBlueAlwaysBlueAlwaysBlueAlwaysBlueAlwaysBlue"));

        // Starts with a hyphen or underscore
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("-RichardHendricks"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("_Dinesh"));

        // Ends with a hyphen or underscore
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Gilfoyle-"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("EricBachman_"));

        // Contains spaces or tabs
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Jared Dunn"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Hooli\tCorp"));

        // Contains consecutive hyphens or underscores
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Pied--Piper"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Hooli__XYZ"));

        // Contains invalid special characters
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Big.Head"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("Richard@Piper"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("PiedPiper!"));

        // Leading or trailing spaces
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername(" Gilfoyle"));
        assertThrows(IllegalArgumentException.class, () -> u1.setUsername("EricBachman "));
    }

    @Test
    void getSetReviews() {
        assertEquals(0, u1.getReviews().size());
        Product product = new Product();
        Review review = new Review(u1, 2, "description", product);
        Set<Review> reviews = new HashSet<>();
        reviews.add(review);
        
        u1.setReviews(reviews);
        assertEquals(1, u1.getReviews().size());

        assertThrows(IllegalArgumentException.class, () -> u1.setReviews(null));
    }

    @Test
    void addRemoveReview() {
        assertEquals(0, u1.getReviews().size());
        Product product = new Product();
        Review review = new Review(u1, 2, "description", product); // add a review
        u1.addReview(review);
        assertEquals(1, u1.getReviews().size());
        assertEquals(u1, review.getAuthor());

        u1.addReview(review); // duplicate added review
        assertEquals(1, u1.getReviews().size());
        
        u1.removeReview(review); // remove a review
        assertEquals(0, u1.getReviews().size());

        assertThrows(IllegalArgumentException.class, () -> u1.addReview(null));
        assertThrows(IllegalArgumentException.class, () -> u1.removeReview(null));
    }

    @Test
    void getSetIncrementDecrementFollowersCount() {
        assertEquals(0, u1.getFollowerCount());

        u1.setFollowerCount(1);
        assertEquals(1, u1.getFollowerCount());

        u1.incrementFollowerCount();
        assertEquals(2, u1.getFollowerCount());

        u1.decrementFollowerCount();
        assertEquals(1, u1.getFollowerCount());

        assertThrows(IllegalArgumentException.class, () -> u1.setFollowerCount(-1));
    }

    @Test
    void getSetIncrementDecrementFollowingCount() {
        assertEquals(0, u1.getFollowingCount());

        u1.setFollowingCount(1);
        assertEquals(1, u1.getFollowingCount());

        u1.incrementFollowingCount();
        assertEquals(2, u1.getFollowingCount());

        u1.decrementFollowingCount();
        assertEquals(1, u1.getFollowingCount());

        assertThrows(IllegalArgumentException.class, () -> u1.setFollowingCount(-1));
    }

    @Test
    void getSetIncrementDecrementFollowing() {
        assertEquals(0, u1.getFollowing().size());

        User user2 = new User("ericbachman@piedpiper.com", "Eric_Bachman");
        Set<User> following = new HashSet<>();
        following.add(user2);

        u1.setFollowing(following);
        assertEquals(1, u1.getFollowing().size());

        assertThrows(IllegalArgumentException.class, () -> u1.setFollowing(null));
    }

    @Test
    void followUnfollowRemoveFollower() {
        assertEquals(0, u1.getFollowingCount());
        assertEquals(0, u1.getFollowerCount());

        User user2 = new User("ericbachman@piedpiper.com", "Eric_Bachman");
        User user3 = new User("russhanneman@piedpiper.com", "Russ_Hanneman");

        u1.follow(user2); // Jian follows Eric
        assertEquals(1, u1.getFollowingCount());
        assertEquals(0, u1.getFollowerCount());
        assertEquals(0, user2.getFollowingCount());
        assertEquals(1, user2.getFollowerCount());

        u1.follow(user3); // Jian follows Russ
        assertEquals(2, u1.getFollowingCount());
        assertEquals(0, u1.getFollowerCount());
        assertEquals(0, user3.getFollowingCount());
        assertEquals(1, user3.getFollowerCount());

        u1.follow(user3); // Jian follows Russ Hanneman
        assertEquals(2, u1.getFollowingCount());
        assertEquals(1, user3.getFollowerCount());

        u1.unfollow(user2); // Jian unfollows Eric
        assertEquals(1, u1.getFollowingCount());
        assertEquals(0, u1.getFollowerCount());
        assertEquals(0, user2.getFollowerCount());
        assertEquals(0, user2.getFollowerCount());

        user3.removeFollower(u1); // Russ removes Jian
        assertEquals(0, u1.getFollowingCount());
        assertEquals(0, u1.getFollowerCount());
        assertEquals(0, user2.getFollowingCount());
        assertEquals(0, user2.getFollowerCount());
        assertEquals(0, user3.getFollowingCount());
        assertEquals(0, user3.getFollowerCount());

        assertThrows(IllegalArgumentException.class, () -> u1.follow(null));
        assertThrows(IllegalArgumentException.class, () -> u1.unfollow(null));
        assertThrows(IllegalArgumentException.class, () -> u1.removeFollower(null));

        assertThrows(IllegalArgumentException.class, () -> u1.follow(u1));
        assertThrows(IllegalArgumentException.class, () -> u1.unfollow(u1));
        assertThrows(IllegalArgumentException.class, () -> u1.removeFollower(u1));
    }
}
