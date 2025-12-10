package com.security.project.controller;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
	
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
		user.setAccountLocked(false);
		user.setFailedLoginAttempts(0);
		userRepository.save(user);
		return ResponseEntity.ok(Map.of("message", "Password reset successful. Your account is now unlocked."));
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

	    userService.createCustomer(user.getEmail(), user.getFirstName(), user.getLastName(), null,false, user.getPassword());
	    return ResponseEntity.ok("User created successfully");
	}
	
	@PostMapping("/login/authentication")
	public ResponseEntity<Object> validateUser(@RequestBody LoginDetails login) {
		 String email = login.getEmail();
		 String userEnteredPassword = login.getPassword();
		 Optional<User> userVo = userRepository.findByEmail(email);
		 if (userVo.isEmpty()) {
			 return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
		 }
		 User user = userVo.get();
		 if (user.isAccountLocked()) {
			 return ResponseEntity.badRequest().body(Map.of("error", "Account locked. Please use 'Forgot Password' to unlock."));
		 }
		 String passwordDatabase = user.getPassword();
		 if (passwordEncoder.matches(userEnteredPassword, passwordDatabase)) {
			 user.setFailedLoginAttempts(0);
			 userRepository.save(user);
			 return ResponseEntity.ok("Password is correct");
		 } else {
			 int attempts = user.getFailedLoginAttempts() + 1;
			 user.setFailedLoginAttempts(attempts);
			 if (attempts >= 3) {
				 user.setAccountLocked(true);
			 }
			 userRepository.save(user);
			 if (user.isAccountLocked()) {
				 return ResponseEntity.badRequest().body(Map.of("error", "Account locked. Please use 'Forgot Password' to unlock."));
			 }
			 return ResponseEntity.badRequest().body(Map.of("error", "Password is not valid"));
		 }
	}
	
	@PostMapping("/address")
	public AddressDTO addAddress(@RequestBody AddressDTO addressDTO) {
		AddressDTO address = new AddressDTO();
		address.setStreetAddress(addressDTO.getStreetAddress());
		address.setState(addressDTO.getState());
		address.setCity(addressDTO.getCity());
		address.setZipCode(addressDTO.getZipCode());
		address.setCountry(addressDTO.getCountry());
		address.setLatitude(addressDTO.getLatitude());
		address.setLongitude(addressDTO.getLongitude());
		address.setUserId(addressDTO.getUserId()); // Map address to user
		userService.saveUserAddress(address);
		return address;
	}
	
	@PostMapping("/send-otp")
	public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Email not registered."));
		}
		User user = userOpt.get();
		String otp = String.valueOf((int)(Math.random() * 900000) + 100000); 
		long expiry = Instant.now().plusSeconds(600).toEpochMilli();
		user.setOtp(otp);
		user.setOtpExpiry(expiry);
		userRepository.save(user);
		userService.storeOtpInRedis(user.getId(), otp);
		userService.sendResetEmail(email, "Your OTP is: " + otp); 
		return ResponseEntity.ok(Map.of("message", "OTP sent to your email address."));
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String otp = body.get("otp");
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Email not registered."));
		}
		User user = userOpt.get();
		String cachedOtp = userService.getOtpFromRedis(user.getId());
		String otpToCheck = (cachedOtp != null) ? cachedOtp : user.getOtp();
		if (otpToCheck == null || user.getOtpExpiry() < Instant.now().toEpochMilli()) {
			return ResponseEntity.badRequest().body(Map.of("error", "OTP expired. Please request a new one."));
		}
		if (!otpToCheck.equals(otp)) {
			return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP."));
		}
		user.setOtp(otp + "_Used");
		user.setOtpExpiry(0);
		user.setEmail_verified(true);
		String familyNumber = "FAM" + UUID.randomUUID().toString();
		user.setFamilyNumber(familyNumber);
		userRepository.save(user);
		userService.deleteOtpFromRedis(user.getId());
		userService.sendFamilyNumberEmail(email, familyNumber);
		return ResponseEntity.ok(Map.of("message", "OTP verified successfully."));
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
        User savedUser = userService.createCustomer(email, firstName, lastName, provider,emailVerified, null);
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
