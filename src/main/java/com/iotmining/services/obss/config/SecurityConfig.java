package com.iotmining.services.obss.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. DISABLE CSRF (Required for React/Axios POST requests)
                .csrf(csrf -> csrf.disable())

                // 2. DISABLE CORS (Required because Nginx handles this)
                .cors(cors -> cors.disable())

                // 3. ALLOW ALL (Nginx handles the firewall/routing security)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                );

        return http.build();
    }
}