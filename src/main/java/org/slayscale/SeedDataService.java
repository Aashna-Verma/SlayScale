package org.slayscale;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SeedDataService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(SeedDataService.class);

    public SeedDataService(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public void seed() {

        // ---------- USERS ----------
        User u1 = getOrCreateUser("alice@gmail.com", "alice");
        User u2 = getOrCreateUser("bob@gmail.com", "bob");
        User u3 = getOrCreateUser("charlie@gmail.com", "charlie");

        // ---------- FOLLOW RELATIONSHIPS ----------
        ensureFollow(u1, u2);
        ensureFollow(u3, u2);
        ensureFollow(u2, u3);

        // ---------- PRODUCTS ----------
        Product p1 = getOrCreateProduct(
                Category.ELECTRONICS,
                "Bose Headphones",
                "https://www.bose.ca/en/p/headphones/bose-quietcomfort-ultra-headphones/QCUH-HEADPHONEARN.html",
                "https://assets.bosecreative.com/transform/775c3e9a-fcd1-489f-a2f7-a57ac66464e1/SF_QCUH_deepplum_gallery_1_816x612_x2?quality=90"
        );

        Product p2 = getOrCreateProduct(
                Category.TOYS,
                "Lilo and Stitch Plush",
                "https://www.canadiantire.ca/en/pdp/lilo-and-stitch-plush-9-in-1501534p.html",
                "https://apim.canadiantire.ca/v1/product/api/v1/product/image/1501534?baseStoreId=CTR&lang=en_CA"
        );

        Product p3 = getOrCreateProduct(
                Category.BEAUTY,
                "Fwee Lip gloss",
                "https://www.yesstyle.com/en/tcuc.CAD/coc.CA/info.html/pid.1128770343?cpid=1136684779",
                "https://cdn.trendhunterstatic.com/thumbs/538/3d-volumizing-gloss.jpeg"
        );

        // ---------- REVIEWS ----------
        ensureReview(u1, p1, "cute", 5);
        ensureReview(u1, p2, "fluffy", 4);
        ensureReview(u1, p3, "glossy", 5);

        ensureReview(u2, p1, "must have", 4);

        ensureReview(u3, p2, "broke", 2);
        ensureReview(u3, p3, "glossy magic", 5);

        log.info("Seeded individual missing data.");
    }

    // ---------- HELPERS ----------

    private User getOrCreateUser(String email, String username) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User u = new User(email, username);
                    return userRepository.save(u);
                });
    }

    private Product getOrCreateProduct(Category category, String name, String url, String imageUrl) {
        return productRepository.findByUrl(url)
                .orElseGet(() -> {
                    Product p = new Product(category, name, url, imageUrl);
                    return productRepository.save(p);
                });
    }

    private void ensureFollow(User follower, User target) {
        if (!follower.getFollowing().contains(target)) {
            follower.follow(target);
            userRepository.save(follower);
            userRepository.save(target);
        }
    }

    private void ensureReview(User user, Product product, String text, int rating) {
        boolean exists = user.getReviews().stream()
                .anyMatch(r -> r.getProduct().equals(product) && r.getText().equals(text));

        if (!exists) {
            Review r = new Review(user, rating, text, product);
            user.addReview(r);
            product.addReview(r);
            userRepository.save(user);
            productRepository.save(product);
        }
    }
}