package com.security.project.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.project.model.User;
import com.security.project.service.UserService;

@RestController
@RequestMapping("/api")
public class Controller {

	@Autowired
	private UserService userService;

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
        // Extract user information
        String firstName = user.getAttribute("given_name");
        String lastName = user.getAttribute("family_name");
        String email = user.getAttribute("email");

		Object issObj = user.getAttributes().get("iss");
		String issuer = issObj != null ? issObj.toString() : null;
		String provider = null;
		if(issuer.contains("google")) {
			provider = "Google";
		}
		boolean emailVerified = (Boolean) user.getAttribute("email_verified");
        
        
        
        // Save or retrieve user from database
        User savedUser = userService.saveUserIfNotExists(email, firstName, lastName, provider,emailVerified);
        return Map.of(
            "authenticated", true,
            "id", savedUser.getId(),
            "name", savedUser.getFirstName(),
            "email", savedUser.getEmail(),
            "lastSessionTimestamp", savedUser.getLastSessionTimestamp()
        );
    }


}
