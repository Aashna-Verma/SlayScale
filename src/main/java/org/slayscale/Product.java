package org.slayscale;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Enum<Category> category;

    private String url;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Review> reviews;

    protected Product() {
    }

    public Product(Enum<Category> category, String url) {
        setCategory(category);
        setUrl(url);
        setReviews(new HashSet<>());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enum<Category> getCategory() {
        return category;
    }

    public void setCategory(Enum<Category> category) {
        if (category == null ) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Product url cannot be null or blank.");
        }
        this.url = url;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        if (reviews == null) throw new IllegalArgumentException("Reviews cannot be null.");
        this.reviews = reviews;
    }

    public void addReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null.");
        }
        this.reviews.add(review);
        review.setProduct(this);
    }

    public void removeReview(Review review) {
        if (review == null) throw new IllegalArgumentException("Review cannot be null.");
        this.reviews.remove(review);
        review.setProduct(null);
    }

    @Override
    public String toString() {
        return String.format("Product[id=%d, category=%s, url='%s', reviews=%s]", id, category, url, reviews);
    }

}
