package com.security.project.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

 @Bean
 public CorsFilter corsFilter() {
     CorsConfiguration config = new CorsConfiguration();
     config.setAllowCredentials(true);
     // Change to your React dev origin: http://localhost:5173 (Vite) or http://localhost:3000 (CRA)
     config.setAllowedOrigins(List.of("http://localhost:5173"));
     config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
     config.setAllowedHeaders(List.of("*"));

     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
     source.registerCorsConfiguration("/**", config);
     return new CorsFilter(source);
 }
}

