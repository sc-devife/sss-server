package com.sss.app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "http://13.51.169.84")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Serves uploaded library images (hotels/escape points/etc.) and org
                // logos back out as plain static files — <img src> requests don't
                // carry our Authorization header, so this path must stay public
                // (see SecurityConfig/JwtAuthenticationFilter's PUBLIC_PATHS).
                String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
                registry.addResourceHandler("/files/**").addResourceLocations(location);
            }
        };
    }
}
