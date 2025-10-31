package org.slayscale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: put Product class field here for 'product'

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore // avoid infinite recursion
    private User author;

    private int rating; // 0 - 5 stars
    private String text;

    protected Review() {}

    public Review(User author, int rating, String text) {
        setAuthor(author);
        setRating(rating);
        setText(text);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null.");
        }
        this.author = author;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5.");
        }
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null.");
        }
        this.text = text;
    }
}
