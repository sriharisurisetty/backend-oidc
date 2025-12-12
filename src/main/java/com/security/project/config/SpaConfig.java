package com.security.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Configuration to serve React SPA (Single Page Application)
 * Routes all non-API requests to index.html so React Router can handle them
 */
@Configuration
public class SpaConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            // Serve static assets (JS, CSS, images, fonts, etc.)
            .addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            // Custom resolver to fallback to index.html for SPA routing
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location) throws IOException {
                    // First, try to get the actual resource
                    Resource resource = location.createRelative(resourcePath);
                    if (resource.exists() && resource.isReadable()) {
                        return resource;
                    }
                    
                    // If resource not found and it's not an API call, serve index.html
                    if (!resourcePath.startsWith("api/")) {
                        return location.createRelative("index.html");
                    }
                    
                    return null;
                }
            });
    }
}
