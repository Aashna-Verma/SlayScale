package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

record CreateProductDTO(String url, String category) {}
record UpdateProductDTO(String url, String category) {}

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }
    @GetMapping("/{id}/reviews")
    @Transactional(readOnly = true)
    public List<Review> reviews(@PathVariable Long id) {
        Product p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return new java.util.ArrayList<>(p.getReviews()); // Set -> List
    }

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody CreateProductDTO body) {
        if (body == null || body.url() == null || body.url().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url is required");
        }
        if (body.category() == null || body.category().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category is required");
        }
        final Category cat;
        try { cat = Category.valueOf(body.category().toUpperCase()); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category"); }
        return repo.save(new Product(cat, body.url().trim()));
    }


    @GetMapping
    public List<Product> list(@RequestParam(required = false) String category) {
        // If category provided:
        if (category != null && !category.isBlank()) {
            try {
                Category cat = Category.valueOf(category.trim().toUpperCase());
                return repo.findByCategory(cat);
            } catch (IllegalArgumentException e) {
                // Invalid category -> return all products
                return repo.findAll();
            }
        }
        // Default: all products
        return repo.findAll();
    }

    // READ one
    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    // UPDATE (partial/full)
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody UpdateProductDTO body) {
        if (body == null || (body.url() == null && body.category() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fields to update");
        }
        return repo.findById(id).map(p -> {
            if (body.url() != null) {
                if (body.url().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "url cannot be blank");
                p.setUrl(body.url().trim());
            }
            if (body.category() != null) {
                try { p.setCategory(Category.valueOf(body.category().toUpperCase())); }
                catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category"); }
            }
            return repo.save(p);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }


    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        repo.deleteById(id);
    }
}
