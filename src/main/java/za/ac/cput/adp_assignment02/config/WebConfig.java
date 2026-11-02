package za.ac.cput.adp_assignment02.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * WebConfig.java
 * One place for CORS so the React dev server (Vite) can call the API.
 * (When the frontend runs through the Vite proxy or is served by Spring itself,
 * CORS isn't needed at all - this is just a safety net.)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);   // the login session cookie
    }
}
