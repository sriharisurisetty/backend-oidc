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
import com.security.project.service.BankIdService;
import com.security.project.service.UserService;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

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
	
	@Autowired
	private BankIdService bankIdService;
	
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
		Argon2 argon2 = Argon2Factory.create();
		
	    User user = new User();
	    user.setFirstName(userDTO.getFirstName());
	    user.setLastName(userDTO.getLastName());
	    if(userRepository.existsByEmail(userDTO.getEmail())) {
	    	return ResponseEntity.badRequest()
	                .body(Map.of("error", "User exists , please use different email"));
        }
	    user.setEmail(userDTO.getEmail());
	    String password = userDTO.getPassword();
	    String hash = argon2.hash(10, 65536, 1, password);
	    
	    user.setPassword(hash); 
	    user.setConsent(true);

	    userService.createCustomer(user.getEmail(), user.getFirstName(), user.getLastName(), null,false, user.getPassword(), null);
	    return ResponseEntity.ok("User created successfully");
	}
	
	@PostMapping("/login/authentication")
	public ResponseEntity<Object> validateUser(@RequestBody LoginDetails login) {
		Argon2 argon2 = Argon2Factory.create(); 
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
		 if (argon2.verify(passwordDatabase, userEnteredPassword)) {
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
			 System.out.println("User logged in");
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
		address.setDisplayName(addressDTO.getDisplayName());
		address.setUserId(addressDTO.getUserId()); // Map address to user
		userService.saveUserAddress(address);
		return address;
	}
	
	@PostMapping("/send-otp")
	public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Email is not found"));
		}
		User user = userOpt.get();
		String otp = String.valueOf((int)(Math.random() * 900000) + 100000); 
		long expiry = Instant.now().plusSeconds(600).toEpochMilli();
		user.setOtp(otp);
		user.setOtpExpiry(expiry);
		userRepository.save(user);
		userService.storeOtpInRedis(user.getId(), otp);
		userService.sendVerifyEmail(email, "Your OTP is: " + otp); 
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
    public Map<String, Object> getOAuthUser(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return Map.of("authenticated", false);
        }
        
        String provider = null;
        Object issObj = user.getAttributes().get("iss");
        String issuer = issObj != null ? issObj.toString() : null;
        String firstName = null;
        String picture = null;
        String login = null;
        String email = null;
        String lastName = null;
        // Set provider based on issuer or other attributes
        if (issuer != null && issuer.contains("google")) {
        // Extract user information - handle both Google and GitHub OAuth2 providers
        firstName = user.getAttribute("given_name");
        lastName = user.getAttribute("family_name");
        email = user.getAttribute("email");
        picture = user.getAttribute("picture");
        provider = "Google";
        }
        else {
        	login = user.getAttribute("login");
        	provider = "Github";
        	firstName = login;
        	picture = user.getAttribute("avatar_url");
        	email = login + "@github.local";
        	LOGGER.warn("Email not available from OAuth provider, using login-based email: " + email);
        }
        boolean emailVerified = true; // Default to true, as OAuth providers verify emails
        Object emailVerifiedObj = user.getAttribute("email_verified");
        if (emailVerifiedObj != null) {
            emailVerified = (Boolean) emailVerifiedObj;
        }
        
        // If still no email, reject the login
        if (email == null) {
            LOGGER.error("No email available from OAuth provider");
            return Map.of("authenticated", false, "error", "Email not available from OAuth provider");
        }
        // Save or retrieve user from database
        try {
            User savedUser = userService.createCustomer(email, firstName, lastName, provider, emailVerified, null, picture);
            
            return Map.of(
                "authenticated", true,
                "id", savedUser.getId(),
                "name", savedUser.getFirstName(),
                "email", savedUser.getEmail(),
                "provider", provider != null ? provider : "Unknown",
                "picture", picture != null ? picture : "",
                "lastSessionTimestamp", savedUser.getLastSessionTimestamp()
            );
        } catch (Exception e) {
            LOGGER.error("Error creating/retrieving user from database", e);
            return Map.of("authenticated", false, "error", "Failed to create user account");
        }
    }

	// BankID Authentication Endpoint
    @PostMapping("/bankid/authenticate")
    public ResponseEntity<?> authenticateWithBankId(@RequestBody Map<String, String> body) {
        String personalNumber = body.get("personalNumber");
        if (personalNumber == null || personalNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Personal number is required."));
        }
        // Call BankID API
        ResponseEntity<String> bankIdResponse = bankIdService.authenticate(personalNumber);
        return bankIdResponse;
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
