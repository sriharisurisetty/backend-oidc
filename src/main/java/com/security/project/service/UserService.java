package com.security.project.service;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.security.project.model.AddressDTO;
import com.security.project.model.User;
import com.security.project.repository.AddressRepository;
import com.security.project.repository.UserRepository;
import org.slf4j.Logger;

@Service
public class UserService {
	
	Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
	
    @Value("${otp.redis.expiry:600}")
    private long otpRedisExpirySeconds;
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private JavaMailSender mailSender;
    
    /**
     * Create a new user
     */
    public User createCustomer(String email, String firstName, String lastName, String provider, boolean email_verified, String password, String picture) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        long now = System.currentTimeMillis();
        int loginCount24h = 0;
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            long lastSession = user.getLastSessionTimestamp();
            if (lastSession != 0L && now - lastSession < 1000) {
                user.setLastSessionTimestamp(now);
                userRepository.save(user);
                return user;
            }
            if (lastSession == 0L || now - lastSession > 24 * 60 * 60 * 1000) {
                user.setLoginCountLast24Hours(1);
                loginCount24h = 1;
            } else {
                user.setLoginCountLast24Hours(user.getLoginCountLast24Hours() + 1);
                loginCount24h = user.getLoginCountLast24Hours();
            }
            user.setLastSessionTimestamp(now);
            user.setEmail_verified(email_verified);
            user.setProviderId(provider);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setLoginCountLast24Hours(loginCount24h);
            user.setConsent(true);
            user.setPicture(picture);
            userRepository.save(user);
            return user;
        }
        User newUser = new User(firstName, lastName, email, provider,email_verified, password, picture);
        newUser.setId(generateUuId());
        newUser.setCreatedAt(now);
        newUser.setLoginCountLast24Hours(1);
        newUser.setLastSessionTimestamp(now);
        newUser.setConsent(true);
        newUser.setPassword(password);
        newUser.setPicture(picture);
        userRepository.save(newUser);
        LOGGER.info("UserService | createCustomer() | Customer has been created ");
        if(email_verified) {
        	String familyNumber = "FAM" + generateRandomNumber();
        	newUser.setFamilyNumber(familyNumber);
    		sendFamilyNumberEmail(email, familyNumber);
        }
        return newUser;
    }
    
    
    public AddressDTO saveUserAddress(AddressDTO address) {
        AddressDTO saveaddress = new AddressDTO();
        saveaddress.setCity(address.getCity());
        saveaddress.setStreetAddress(address.getStreetAddress());
        saveaddress.setCountry(address.getCountry());
        saveaddress.setZipCode(address.getZipCode());
        saveaddress.setState(address.getState());
        saveaddress.setAddressId(generateUuId());
        saveaddress.setCreatedAt(new Date());
		saveaddress.setUserId(address.getUserId()); // Save userId mapping
		saveaddress.setLatitude(address.getLatitude());
		saveaddress.setLongitude(address.getLongitude());
		saveaddress.setDisplayName(address.getDisplayName());
		addressRepo.save(saveaddress);
		LOGGER.info("UserService | saveUserAddress() | Address has been added to the user ");
    return saveaddress;
    }
    
    public void sendFamilyNumberEmail(String to, String familyNumber) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yourdentity@services.com");
        message.setTo(to);
        message.setSubject("Email Verified - Family Number");
        message.setText("Your email has been successfully verified. \n\nYour Family Number is: " + familyNumber + "\nThank you for registering.");
        mailSender.send(message);
        LOGGER.info("UserService | sendFamilyNumberEmail() | Family Number email sent ");
    }
    
    /**
     * Find a user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get a user by ID
     */
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Update a user
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Delete a user by ID
     */
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    
    public String generateUuId() {

		return UUID.randomUUID().toString();
	}
    
    private static String generateRandomNumber() {
		
		Random random = new Random();
		long number = 100000000000L + (long)(random.nextDouble() * 900000000000L);
		
		return String.valueOf(number);
    }
    
    public void sendResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yourdentity@services.com");
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetLink + "\nIf you did not request this, please ignore this email.");
        mailSender.send(message);
        
    }

    public void storeOtpInRedis(String userId, String otp) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(userId, otp, otpRedisExpirySeconds, java.util.concurrent.TimeUnit.SECONDS);
        LOGGER.info("UserService | storeOtpInRedis() | OTP saved in Redis cache ");
    }

    public String getOtpFromRedis(String userId) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        LOGGER.info("UserService | getOtpFromRedis() | Retrieve OTP from cache ");
        return ops.get(userId);
    }

    public void deleteOtpFromRedis(String userId) {
        redisTemplate.delete(userId);
        LOGGER.info("UserService | deleteOtpFromRedis() | OTP flushed from Redis cache for userId: " + userId);
    }
		
}
