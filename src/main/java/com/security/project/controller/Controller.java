package com.security.project.controller;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.security.project.model.User;
import com.security.project.repository.UserRepository;
import com.security.project.service.UserService;

@RestController
@RequestMapping("/api")
public class Controller {
	
	@Autowired
    private UserRepository userRepository;

	@Autowired
	private UserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping("/hellouser")
	public String hellouser() {
		return "Hello User";

	}

	@GetMapping("/helloadmin")
	public String helloadmin() {
		return "Hello Admin";

	}
	@PostMapping("/login/createcustomer")
	public ResponseEntity<Object> createUser(@RequestBody User userDTO) {
	    User user = new User();
	    user.setFirstName(userDTO.getFirstName());
	    user.setLastName(userDTO.getLastName());
	    if(userRepository.existsByEmail(userDTO.getEmail())) {
	    	return ResponseEntity.badRequest()
	                .body(Map.of("error", "User exists , please use different email"));
        }
	    user.setEmail(userDTO.getEmail());
	    if(validateStrongPassword(userDTO.getPassword())){
	    	user.setPassword(passwordEncoder.encode(userDTO.getPassword())); 
	    }else {
	    	return ResponseEntity.badRequest()
                .body(Map.of("error", "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."));

	    }
	    // Consider hashing the password before saving
	    user.setConsent(true);

	    userService.saveUserIfNotExists(user.getEmail(), user.getFirstName(), user.getLastName(), null,false, user.getPassword());
	    System.out.println("account created");
	    return ResponseEntity.ok("User created successfully");
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
        User savedUser = userService.saveUserIfNotExists(email, firstName, lastName, provider,emailVerified, null);
        return Map.of(
            "authenticated", true,
            "id", savedUser.getId(),
            "name", savedUser.getFirstName(),
            "email", savedUser.getEmail(),
            "lastSessionTimestamp", savedUser.getLastSessionTimestamp()
        );
    }

	public static boolean validateStrongPassword(String password) {
		boolean check;
		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
		Pattern passwordPattern = Pattern.compile(passwordRegex);
		Matcher passwordValid = passwordPattern.matcher(password);
		check = passwordValid.find();
		return check;
	}
}
