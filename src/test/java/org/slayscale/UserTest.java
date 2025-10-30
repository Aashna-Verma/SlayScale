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

    @AfterEach
    public void tearDown() {
        user = null;
    }

    @Test
    void id_get_set() {
        user.setId(1L);
        assertEquals(1L, user.getId());
    }

    @Test
    void username_get_set() {
        assertEquals("Jian_Yang", user.getUsername());
        user.setUsername("Eric_Bachman");
        assertEquals("Eric_Bachman", user.getUsername());

        assertThrows(IllegalArgumentException.class, () -> user.setUsername(null));
    }

    @Test
    void reviews_get_set() {
        assertEquals(0, user.getReviews().size());
        
        Review review = new Review(2, "description");
        Set<Review> reviews = new HashSet<>();
        reviews.add(review);
        
        user.setReviews(reviews);
        assertEquals(1, user.getReviews().size());

        assertThrows(IllegalArgumentException.class, () -> user.setReviews(null));
    }

    @Test
    void review_add_delete() {
        assertEquals(0, user.getReviews().size());
        Review review = new Review(2, "description");

        user.addReview(review); // add a review
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
    void followers_get_set() {
        assertEquals(0, user.getFollowers().size());
        
        User user2 = new User("Eric_Bachman");
        Set<User> followers = new HashSet<>();
        followers.add(user2);

        user.setFollowers(followers);
        assertEquals(1, user.getFollowers().size());

        assertThrows(IllegalArgumentException.class, () -> user.setFollowers(null));
    }

    @Test
    void following_get_set() {
        assertEquals(0, user.getFollowing().size());

        User user2 = new User("Eric_Bachman");
        Set<User> following = new HashSet<>();
        following.add(user2);

        user.setFollowing(following);
        assertEquals(1, user.getFollowing().size());

        assertThrows(IllegalArgumentException.class, () -> user.setFollowing(null));
    }

    @Test
    void follow_unfollow_removeFollower() {
        assertEquals(0, user.getFollowing().size());
        assertEquals(0, user.getFollowers().size());

        User user2 = new User("Eric_Bachman");
        User user3 = new User("Russ_Hanneman");

        user.follow(user2); // Jian follows Eric
        assertEquals(1, user.getFollowing().size());
        assertEquals(0, user.getFollowers().size());
        assertEquals(0, user2.getFollowing().size());
        assertEquals(1, user2.getFollowers().size());

        user.follow(user3); // Jian follows Russ
        assertEquals(2, user.getFollowing().size());
        assertEquals(0, user.getFollowers().size());
        assertEquals(0, user3.getFollowing().size());
        assertEquals(1, user3.getFollowers().size());

        user.follow(user3); // Jian follows Russ Hanneman
        assertEquals(2, user.getFollowing().size());
        assertEquals(1, user3.getFollowers().size());

        user.unfollow(user2); // Jian unfollows Eric
        assertEquals(1, user.getFollowing().size());
        assertEquals(0, user.getFollowers().size());
        assertEquals(0, user2.getFollowers().size());
        assertEquals(0, user2.getFollowers().size());

        user3.removeFollower(user); // Russ removes Jian
        assertEquals(0, user.getFollowing().size());
        assertEquals(0, user.getFollowers().size());
        assertEquals(0, user2.getFollowing().size());
        assertEquals(0, user2.getFollowers().size());
        assertEquals(0, user3.getFollowing().size());
        assertEquals(0, user3.getFollowers().size());

        assertThrows(IllegalArgumentException.class, () -> user.follow(null));
        assertThrows(IllegalArgumentException.class, () -> user.unfollow(null));
        assertThrows(IllegalArgumentException.class, () -> user.removeFollower(null));

        assertThrows(IllegalArgumentException.class, () -> user.follow(user));
        assertThrows(IllegalArgumentException.class, () -> user.unfollow(user));
        assertThrows(IllegalArgumentException.class, () -> user.removeFollower(user));
    }
}
