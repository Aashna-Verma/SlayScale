package org.slayscale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Category category;
    private String url;

    @OneToMany(mappedBy = "product", orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Review> reviews;

    protected Product() {
    }

    public Product(Category category, String url) {
        setCategory(category);
        setUrl(url);
        this.reviews = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return url != null && url.equals(product.getUrl());
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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
    }

    public void removeReview(Review review) {
        if (review == null) throw new IllegalArgumentException("Review cannot be null.");
        this.reviews.remove(review);
    }
}
