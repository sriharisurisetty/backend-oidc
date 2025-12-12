package com.security.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * GitHub OAuth2 specific configuration
 * Handles GitHub-specific OAuth2 attributes and behavior
 */
@Configuration
public class GitHubOAuth2Config {

    /**
     * Configure logout handler for OAuth2 providers
     * Note: GitHub does not have a logout endpoint like OIDC providers,
     * but we configure it for consistency
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:5173/logout");
        return logoutSuccessHandler;
    }

    /**
     * GitHub OAuth2 attributes mapping:
     * 
     * Standard Attributes:
     * - login: GitHub username (unique identifier)
     * - name: User's full name
     * - email: User's email address (if public or requested via scope)
     * - avatar_url: URL to user's profile picture
     * - bio: User's bio/description
     * - company: User's company name
     * - location: User's location
     * - blog: User's blog/website URL
     * - public_repos: Number of public repositories
     * 
     * Requested Scopes:
     * - user:email: Read access to user's email (includes public and private)
     * - read:user: Read access to user profile data
     */
}
