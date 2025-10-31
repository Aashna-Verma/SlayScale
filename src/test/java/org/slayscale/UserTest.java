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
    void idGetSet() {
        user.setId(1L);
        assertEquals(1L, user.getId());
    }

    @Test
    void usernameGetSet() {
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
    void reviewsGetSet() {
        assertEquals(0, user.getReviews().size());
        
        Review review = new Review(user, 2, "description");
        Set<Review> reviews = new HashSet<>();
        reviews.add(review);
        
        user.setReviews(reviews);
        assertEquals(1, user.getReviews().size());

        assertThrows(IllegalArgumentException.class, () -> user.setReviews(null));
    }

    @Test
    void reviewAddRemove() {
        assertEquals(0, user.getReviews().size());

        Review review = new Review(user, 2, "description"); // add a review
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
    void followersGetSet() {
        assertEquals(0, user.getFollowers().size());
        
        User user2 = new User("Eric_Bachman");
        Set<User> followers = new HashSet<>();
        followers.add(user2);

        user.setFollowers(followers);
        assertEquals(1, user.getFollowers().size());

        assertThrows(IllegalArgumentException.class, () -> user.setFollowers(null));
    }

    @Test
    void followingGetSet() {
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
