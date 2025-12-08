package com.security.project.controller;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

import com.security.project.model.AddressDTO;
import com.security.project.model.LoginDetails;
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

	// Forgot Password Endpoint
	@PostMapping("/login/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isPresent()) {
			User user = userOpt.get();
			String token = UUID.randomUUID().toString();
			long expiry = Instant.now().plusSeconds(60 * 30).toEpochMilli(); // 30 min expiry
			user.setResetToken(token);
			user.setResetTokenExpiry(expiry);
			userRepository.save(user);
			String resetLink = "http://localhost:5173/reset-password?token=" + token;
			userService.sendResetEmail(email, resetLink);
		}
		// Always return success to avoid email enumeration
		return ResponseEntity.ok(Map.of("message", "If your email is registered, you will receive password reset instructions."));
	}

	// Reset Password Endpoint
	@PostMapping("/login/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
		String token = body.get("token");
		String newPassword = body.get("newPassword");
		Optional<User> userOpt = userRepository.findAll().stream().filter(u -> token.equals(u.getResetToken())).findFirst();
		if (userOpt.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token."));
		}
		User user = userOpt.get();
		if (user.getResetTokenExpiry() < Instant.now().toEpochMilli()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Token expired."));
		}
		if (!validateStrongPassword(newPassword)) {
			return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."));
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setResetToken(null);
		user.setResetTokenExpiry(0);
		userRepository.save(user);
		return ResponseEntity.ok(Map.of("message", "Password reset successful."));
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
	    return ResponseEntity.ok("User created successfully");
	}
	
	@PostMapping("/login/authentication")
	public ResponseEntity<Object> validateUser(@RequestBody LoginDetails login) {
		 Optional<User> userVo = Optional.of(new User());
		String email = login.getEmail();
		String userEnteredPassword = login.getPassword();
		userVo = userRepository.findByEmail(email);
		String passwordDatabase = userVo.get().getPassword();
		if(passwordEncoder.matches(userEnteredPassword, passwordDatabase)) {
			return ResponseEntity.ok("Password is correct");
		}
		else {
			return ResponseEntity.badRequest()
	                .body(Map.of("error", "Password is not valid"));
		}
	}
	
	@PostMapping("/address")
	public AddressDTO addAddress(@RequestBody AddressDTO addressDTO) {
	    AddressDTO address = new AddressDTO();
	    address.setStreetAddress(addressDTO.getStreetAddress());
	    address.setState(addressDTO.getStreetAddress());
	    address.setCity(addressDTO.getCity());
	    address.setZipCode(addressDTO.getZipCode());
	    address.setCountry(addressDTO.getCountry());
	    userService.saveUserAddress(address);
	    return address;
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
