package org.slayscale;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Review> reviews;

    @ManyToMany(mappedBy = "following")
    private Set<User> followers;

    @ManyToMany
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<User> following;

    protected User() {}

    public User(String username) {
        setUsername(username);
        setReviews(new HashSet<>());
        setFollowers(new HashSet<>());
        setFollowing(new HashSet<>());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null) throw new IllegalArgumentException("username cannot be null.");
        this.username = username;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        if (reviews == null) throw new IllegalArgumentException("reviews cannot be null.");
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        if (review == null) throw new IllegalArgumentException("review to add cannot be null.");
        this.reviews.add(review);
    }

    public void removeReview(Review review) {
        if (review == null) throw new IllegalArgumentException("review to remove cannot be null.");
        this.reviews.remove(review);
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        if (followers == null) throw new IllegalArgumentException("Followers cannot be null.");
        this.followers = followers;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public void setFollowing(Set<User> following) {
        if (following == null) throw new IllegalArgumentException("following cannot be null.");
        this.following = following;
    }

    public void follow(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");
        if (user.equals(this)) throw new IllegalArgumentException("User cannot follow themselves");
        if (this.following.add(user)) {
            user.followers.add(this);
        }
    }

    public void unfollow(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");
        if (user.equals(this)) throw new IllegalArgumentException("User cannot unfollow themselves");
        if (this.following.remove(user)) {
            user.followers.remove(this);
        }
    }

    public void removeFollower(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null.");
        if (user.equals(this)) throw new IllegalArgumentException("User cannot remove themselves as follower");
        if (this.followers.remove(user)) {
            user.following.remove(this);
        }
    }
}
