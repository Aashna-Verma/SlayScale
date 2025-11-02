package org.slayscale;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByUrl(String url);
    List<Product> findByCategory(Category category);
}