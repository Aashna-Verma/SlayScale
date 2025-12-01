package org.slayscale;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SlayScaleApplication {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    private static final Logger log = LoggerFactory.getLogger(SlayScaleApplication.class);

    public static void main(String[] args) {
        // Load dotenv BEFORE Spring initializes properties
        try {
            Dotenv dotenv = Dotenv.configure().load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

        } catch (Exception e) {
            System.out.println(".env not found, skipping");
        }
        SpringApplication.run(SlayScaleApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo() {
        return args -> {

            // Only seed if empty (prevents duplicate inserts on restart)
            if (userRepository.count() == 0) {
                User u1 = new User("alice@gmail.com", "alice");
                User u2 = new User("bob@gmail.com", "bob");
                User u3 = new User("charlie@gmail.com", "charlie");

                //users need to persist before they can follow one another
                userRepository.save(u1);
                userRepository.save(u2);
                userRepository.save(u3);

                u1.follow(u2);
                u3.follow(u2);
                u2.follow(u3);

                if (productRepository.count() == 0) {
                    Product p1 = new Product(
                            Category.ELECTRONICS,
                            "Bose Headphones",
                            "https://www.bose.ca/en/p/headphones/bose-quietcomfort-ultra-headphones/QCUH-HEADPHONEARN.html",
                            "https://assets.bosecreative.com/transform/775c3e9a-fcd1-489f-a2f7-a57ac66464e1/SF_QCUH_deepplum_gallery_1_816x612_x2?quality=90");
                    Product p2 = new Product(
                            Category.TOYS,
                            "Lilo and Stitch Plush",
                            "https://www.canadiantire.ca/en/pdp/lilo-and-stitch-plush-9-in-1501534p.html?utm_content=shopping&gclsrc=aw.ds&gad_source=1&gad_campaignid=21840584553&gbraid=0AAAAADojZpjbI8kSZ5DA7WljF5l-nky6y&gclid=CjwKCAiA86_JBhAIEiwA4i9Ju2uQXOuGq6CBUfhu1wXcxIOD1cchh1PtFfXbNLbn4ttxLiOSJrnEqxoCUbAQAvD_BwE#store=442",
                            "https://apim.canadiantire.ca/v1/product/api/v1/product/image/1501534?baseStoreId=CTR&lang=en_CA&subscription-key=c01ef3612328420c9f5cd9277e815a0e&imwidth=1244&impolicy=gZoom");
                    Product p3 = new Product(
                            Category.BEAUTY,
                            "Fwee Lip gloss",
                            "https://www.yesstyle.com/en/tcuc.CAD/coc.CA/info.html/pid.1128770343?cpid=1136684779&googtrans=en&utm_source=GoogleAds&utm_campaign=1416779218&utm_term=&utm_content=59766761030_272378536269&utm_medium=Shopping&bac=H6XM7U8D&mcg=paidsearch&gad_source=1&gad_campaignid=1416779218&gbraid=0AAAAAD3WTkmubxnAOQXz6NnRNT69eSt1J&gclid=CjwKCAiA86_JBhAIEiwA4i9Ju3oE_FKroPEI9r8NhV2ZnfGWys8C1q7Fr9JXuaeMA0rndOcl6c4P6hoC1aMQAvD_BwE",
                            "https://cdn.trendhunterstatic.com/thumbs/538/3d-volumizing-gloss.jpeg");

                    u1.addReview(new Review(u1, 5, "cute", p1));
                    u1.addReview(new Review(u1, 4, "fluffy", p2));
                    u1.addReview(new Review(u1, 5, "glossy", p3));

                    u2.addReview(new Review(u2, 4, "must have", p1));

                    u3.addReview(new Review(u3, 2, "broke", p2));
                    u3.addReview(new Review(u3, 5, "glossy magic", p3));

                    productRepository.save(p1);
                    productRepository.save(p2);
                    productRepository.save(p3);
                }

                userRepository.save(u1);
                userRepository.save(u2);
                userRepository.save(u3);
            }

            log.info("Seed data created!");
        };
    }
}