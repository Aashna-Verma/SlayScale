package org.slayscale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    // CREATE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody CreateProductDTO body) {
        Category cat = Category.valueOf(body.category().toUpperCase()); // parse safely
        Product p = new Product(cat, body.url()); // Category is a valid Enum<Category> value
        return repo.save(p);
    }

    // LIST (filter? category=BOOKS)
    @GetMapping
    public List<Product> list(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return repo.findByCategory(Category.valueOf(category.toUpperCase()));
        }
        return repo.findAll();
    }

    // READ one
    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    // UPDATE (partial/full)
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody UpdateProductDTO body) {
        return repo.findById(id).map(p -> {
            if (body.url() != null) p.setUrl(body.url());
            if (body.category() != null)
                p.setCategory(Category.valueOf(body.category().toUpperCase()));
            return repo.save(p);
        }).orElse(null);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
