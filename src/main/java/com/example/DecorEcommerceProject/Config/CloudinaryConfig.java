package com.example.DecorEcommerceProject.Config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary("cloudinary://979626945494346:QyVM2ikOQXOMBkiWtt5wpKFGBAg@dg1unpaqq");
    }
}
