package com.sss.app.configuration;

import com.sss.app.jwtToken.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Kept in sync with JwtAuthenticationFilter.PUBLIC_PATHS — the filter is
                        // what actually enforces this, this just tells Spring Security's own
                        // access-decision layer to permit the same paths rather than 403 them
                        // before the filter even runs. NOTE: requestMatchers() here match against
                        // the path with the server.servlet.context-path ("/sss") already stripped,
                        // unlike JwtAuthenticationFilter's getRequestURI() which includes it.
                        .requestMatchers(
                                "/api/login/user",
                                "/api/login/forgot-password",
                                "/api/login/reset-password",
                                "/api/signup",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
