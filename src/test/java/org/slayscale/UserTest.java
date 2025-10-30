package org.slayscale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserTest {
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("Jian", "Yang", "Jian@Yang.ca", "pwd");
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
    void firstName_get_set() {
        assertEquals("Jian", user.getFirstName());
        user.setFirstName("Eric");
        assertEquals("Eric", user.getFirstName());
    }

    @Test
    void lastName_get_set() {
        assertEquals("Yang", user.getLastName());
        user.setLastName("Yan");
        assertEquals("Yan", user.getLastName());
    }

    @Test
    void email_get_set() {
        assertEquals("Jian@Yang.ca", user.getEmail());
        user.setEmail("Eric@Bachman.ca");
        assertEquals("Eric@Bachman.ca", user.getEmail());
    }

    @Test
    void password_get_set() {
        assertEquals("pwd", user.getPassword());
        user.setPassword("pwd2");
        assertEquals("pwd2", user.getPassword());
    }

    @Test
    void reviews_get_set() {
        assertEquals(0, user.getReviews().size());
        
        Review review = new Review(2, "description");
        Set<Review> reviews = new HashSet<>();
        reviews.add(review);
        
        user.setReviews(reviews);
        assertEquals(1, user.getReviews().size());
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
        assertNull(review.getAuthor());
    }
    
    @Test
    void followers_get_set() {
        assertEquals(0, user.getFollowers().size());
        
        User user2 = new User("Eric", "Bachman", "Eric@Bachman.ca", "pwd2");
        Set<User> followers = new HashSet<>();
        followers.add(user2);

        user.setFollowers(followers);
        assertEquals(1, user.getFollowers().size());
    }

    @Test
    void following_get_set() {
        assertEquals(0, user.getFollowing().size());

        User user2 = new User("Eric", "Bachman", "Eric@Bachman.ca", "pwd2");
        Set<User> following = new HashSet<>();
        following.add(user2);

        user.setFollowing(following);
        assertEquals(1, user.getFollowing().size());
    }

    @Test
    void follow_unfollow_removeFollower() {
        assertEquals(0, user.getFollowing().size());
        assertEquals(0, user.getFollowers().size());

        User user2 = new User("Eric", "Bachman", "Eric@Bachman.ca", "pwd2");
        User user3 = new User("Russ", "Hanneman", "Russ@Hanneman.ca", "pwd2");

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
    }
}
