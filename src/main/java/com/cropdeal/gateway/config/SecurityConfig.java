package com.cropdeal.gateway.config;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 
import java.util.List;
 
@Configuration
public class SecurityConfig {
 
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
 
        CorsConfiguration configuration = new CorsConfiguration();
 
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:4201",
                "http://127.0.0.1:4201"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
 
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
 
        source.registerCorsConfiguration("/**", configuration);
 
        return source;
    }
 
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {
 
        return http
                .csrf(csrf -> csrf.disable())
 
                .cors(cors -> {})
 
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
 
                .authorizeHttpRequests(auth -> auth
 
                        // =====================================================
                        // SWAGGER / OPENAPI
                        // =====================================================
 
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/openapi/**",
 
                                // Gateway Swagger routes
                                "/authentication/v3/api-docs",
                                "/profile/v3/api-docs",
                                "/crop/v3/api-docs",
                                "/order/v3/api-docs",
                                "/payment/v3/api-docs",
                                "/notification/v3/api-docs",
                                "/subscription/v3/api-docs",
                                "/subscription/v3/api-docs/**"
                        ).permitAll()
 
                        // =====================================================
                        // AUTHENTICATION
                        // =====================================================
 
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                        .requestMatchers("/admin/health").hasRole("ADMIN")
 
                        // =====================================================
                        // PUBLIC CROP GET APIs
                        // =====================================================
 
                        .requestMatchers(
                                HttpMethod.GET,
                                "/crops",
                                "/crops/search/**",
                                "/crops/filter/**",
                                "/crops/page"
                        ).permitAll()
 
                        // =====================================================
                        // EVERYTHING ELSE REQUIRES AUTHENTICATION
                        // =====================================================
 
                        .anyRequest().authenticated()
                )
 
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
 
                .build();
    }
}