package org.slayscale;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/SlayScale")
@SessionAttributes("currentUserId")
public class SlayScaleViewController {
    private final UserController userController;
    private final ProductController productController;

    public SlayScaleViewController(UserController userController, ProductController productController) {
        this.userController = userController;
        this.productController = productController;
    }

    @ModelAttribute("currentUserId")
    public Long currentUserId() { return null; }

    @GetMapping("/signup")
    public String signupForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) model.addAttribute("error", error);
        return "signup";
    }

    @PostMapping("/signup")
    public String performSignup(@RequestParam("username") String username, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> body = Map.of("username", username.trim());
            ResponseEntity<User> response  = userController.createUser(body);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                session.setAttribute("currentUserId", response.getBody().getId());
                return "redirect:/SlayScale/products";
            } else {
                redirectAttributes.addAttribute("error", "That username is already taken or invalid.");
                return "redirect:/SlayScale/signup";
            }
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Something went wrong: " + e.getMessage());
            return "redirect:/SlayScale/signup";
        }
    }

    @GetMapping("/products")
    public String productsPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "success", required = false) String success,
                               Model model) {
        List<Product> products = productController.listProducts("");
        model.addAttribute("products", products);
        model.addAttribute("categories", Category.values());

        // set active tab so the template can highlight it
        model.addAttribute("activeTab", "products");
        if (error != null) model.addAttribute("error", error);
        if (success != null) model.addAttribute("success", success);

        return "products";
    }

    @PostMapping("/products")
    public String createProduct(@RequestParam("category") String category,
                                @RequestParam("url") String url,
                                RedirectAttributes ra) {
        try {
            Map<String, String> body = Map.of(
                    "category", category.trim(),
                    "url", url.trim()
            );

            ResponseEntity<Product> resp = productController.createProduct(body);

            if (resp.getStatusCode() == HttpStatus.CREATED) {
                ra.addAttribute("success", "Product created.");
            } else {
                ra.addAttribute("error", "Invalid category or URL.");
            }
        } catch (HttpClientErrorException.BadRequest e) {
            ra.addAttribute("error", "Invalid category or URL.");
        } catch (Exception e) {
            ra.addAttribute("error", "Something went wrong: " + e.getMessage());
        }
        return "redirect:/SlayScale/products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id,
                                @SessionAttribute(value = "currentUserId", required = false)
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "success", required = false) String success,
                                Model model) {

        Product product = productController.getProduct(id);
        model.addAttribute("product", product);

        ResponseEntity<Set<Review>> reviewsResp = productController.getProductReviews(id);

        Set<Review> reviews = reviewsResp != null && reviewsResp.getBody() != null ? reviewsResp.getBody() : Set.of();
        model.addAttribute("reviews", reviews);

        if (error != null) model.addAttribute("error", error);
        if (success != null) model.addAttribute("success", success);

        return "product_detail";
    }

    @PostMapping("/products/{id}/reviews")
    public String createReviewForProduct(@PathVariable Long id,
                                         @RequestParam("rating") Integer rating,
                                         @RequestParam("text") String text,
                                         @ModelAttribute("currentUserId") Long currentUserId,
                                         RedirectAttributes ra) {
        if (currentUserId == null) {
            ra.addAttribute("error", "Please sign up/sign in before posting a review.");
            return "redirect:/SlayScale/signup";
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("productId", id);
            body.put("rating", rating);
            body.put("text", text.trim());

            ResponseEntity<Map<String, Object>> resp = userController.createReview(currentUserId, body);

            if (resp.getStatusCode() == HttpStatus.CREATED) {
                ra.addAttribute("success", "Review posted.");
            } else {
                ra.addAttribute("error", "Could not create review.");
            }
        } catch (Exception e) {
            ra.addAttribute("error", "Something went wrong: " + e.getMessage());
        }

        return "redirect:/SlayScale/products/{id}";
    }

    @GetMapping("/users")
    public String usersPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "success", required = false) String success,
                               Model model) {
        List<User> users = userController.getAllUsers().getBody();
        model.addAttribute("users", users);

        // set active tab so the template can highlight it
        model.addAttribute("activeTab", "users");
        if (error != null) model.addAttribute("error", error);
        if (success != null) model.addAttribute("success", success);

        return "users";
    }

    @GetMapping("/users/{id}")
    public String specificUserPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "success", required = false) String success,
                                   @ModelAttribute("currentUserId") Long currentUserId,
                                   Model model) {

        User user = userController.getUserById(currentUserId).getBody();
        Set<Review> reviews = userController.getReviews(user.getId()).getBody();
        model.addAttribute("user", user);
        model.addAttribute("reviews", reviews);

        // set active tab so the template can highlight it
        model.addAttribute("activeTab", "users");
        if (error != null) model.addAttribute("error", error);
        if (success != null) model.addAttribute("success", success);
        return "user-detail";
    }
}
