package com.security.project.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        	.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/public/**",
                    "/error",
                    "/login/**",
                    "/oauth2/**"          // allow OAuth endpoints
                ).permitAll()
                .anyRequest().permitAll() // keep other APIs public (change to .authenticated() if you want protection)
            )
            .oauth2Login(oauth -> oauth
                // Point the login page directly to OAuth authorization endpoints
                .loginPage("/oauth2/authorization/google")
                .defaultSuccessUrl("http://localhost:5173/success-login", true) // customize your post-login redirect
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(new OidcUserService())
                    .userService(new DefaultOAuth2UserService())
                )
            )
             .logout(logout -> logout
             .logoutUrl("/logout")
             .logoutSuccessUrl("http://localhost:5173/logout") // Redirect to home after logout
             .invalidateHttpSession(true)
             .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
