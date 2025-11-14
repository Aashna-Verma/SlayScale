package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    public ProductController(ProductRepository repo) {
        this.productRepository = repo;
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Set<Review>> getProductReviews(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(product.getReviews()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Product> createProduct(@RequestBody Map<String,String> body) {
        String url = body.get("url");
        String category = body.get("category");
        if (url == null || url.isBlank() || category == null || category.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Category parsed;
        try {
            parsed = Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Product> existing = productRepository.findByUrl(url.trim());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Product saved = productRepository.save(new Product(parsed, url.trim()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<Product> listProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            try {
                Category cat = Category.valueOf(category.trim().toUpperCase());
                return productRepository.findByCategory(cat);
            } catch (IllegalArgumentException e) {
                return productRepository.findAll();
            }
        }
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
    }
}
