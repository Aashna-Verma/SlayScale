package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductController(ProductRepository repo, UserRepository userRepository) {
        this.productRepository = repo;
        this.userRepository = userRepository;
    }

    public ResponseEntity<Set<Review>> getProductReviews(Long id) {
        return getProductReviews(id, "newest", 0, null);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Set<Review>> getProductReviews(
            @PathVariable Long id,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(value = "minRating", required = false, defaultValue = "0") Integer minRating,
            @RequestParam(value = "baseUserId", required = false) Long baseUserId) {

        //Find product or return 404
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Product product = productOpt.get();
        Set<Review> reviewsSet = product.getReviews();
        if (reviewsSet == null) {
            reviewsSet = Set.of();
        }

        var stream = reviewsSet.stream()
                .filter(r -> r.getRating() >= (minRating == null ? 0 : minRating));

        String sortOption = (sort == null ? "newest" : sort.toLowerCase());

        Comparator<Review> cmp;
        switch (sortOption) {
            case "oldest":
                cmp = Comparator.comparing(Review::getId);
                break;

            case "rating_desc":
                cmp = Comparator.comparingInt(Review::getRating).reversed()
                        .thenComparing(Review::getId, Comparator.reverseOrder());
                break;

            case "rating_asc":
                cmp = Comparator.comparingInt(Review::getRating)
                        .thenComparing(Review::getId);
                break;

            case "similarity":
                if (baseUserId == null) {
                    return ResponseEntity.badRequest().build();
                }
                var baseUserOpt = userRepository.findById(baseUserId);
                if (baseUserOpt.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                User baseUser = baseUserOpt.get();

                cmp = (r1, r2) -> Double.compare(
                        r2.getAuthor().getSimilarity(baseUser),
                        r1.getAuthor().getSimilarity(baseUser)
                );
                break;

            case "newest":
            default:
                cmp = Comparator.comparing(Review::getId).reversed();
                break;
        }

        //Sort and return
        Set<Review> sortedSet = new LinkedHashSet<>(
                stream.sorted(cmp).collect(Collectors.toList())
        );
        return ResponseEntity.ok(sortedSet);
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
