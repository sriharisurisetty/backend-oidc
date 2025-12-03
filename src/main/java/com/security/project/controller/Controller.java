package com.security.project.controller;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api")
public class Controller {

	@GetMapping("/hellouser")
	public String hellouser() {
		return "Hello User";

	}

	@GetMapping("/helloadmin")
	public String helloadmin() {
		return "Hello Admin";

	}

	@GetMapping("/hello")
	public Map<String, String> hello() {
		return Map.of("message", "Hello from Spring Boot");
	}
	

	@GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return Map.of("authenticated", false);
        }
        return Map.of(
            "authenticated", true,
            "name", user.getAttribute("name"),
            "email", user.getAttribute("email"),
            "picture", user.getAttribute("picture")
        );
    }


}
