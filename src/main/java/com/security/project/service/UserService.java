package com.security.project.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.security.project.model.AddressDTO;
import com.security.project.model.User;
import com.security.project.repository.AddressRepository;
import com.security.project.repository.UserRepository;

@Service
public class UserService {
	
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private JavaMailSender mailSender;
    public void sendResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetLink + "\nIf you did not request this, please ignore this email.");
        mailSender.send(message);
    }
    
    /**
     * Save a new user if they don't already exist by email
     */
    public User saveUserIfNotExists(String email, String firstName, String lastName, String provider, boolean email_verified, String password) {
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
            user.setId(generateUuId());
            userRepository.save(user);
             // ensure up-to-date
            return user;
        }
        User newUser = new User(firstName, lastName, email, provider,email_verified, password);
        newUser.setId(generateUuId());
        newUser.setCreatedAt(now);
        newUser.setLoginCountLast24Hours(1);
        newUser.setLastSessionTimestamp(now);
        newUser.setConsent(true);
        newUser.setPassword(password);
        userRepository.save(newUser);
        return newUser;
    }
    
    
    public AddressDTO saveUserAddress(AddressDTO address) {
    	AddressDTO saveaddress = new AddressDTO();
    	saveaddress.setCity(address.getCity());
    	saveaddress.setStreetAddress(address.getStreetAddress());
    	saveaddress.setCountry(address.getCountry());
    	saveaddress.setZipCode(address.getZipCode());
    	saveaddress.setAddressId(generateUuId());
    	saveaddress.setCreatedAt(new Date());
        addressRepo.save(saveaddress);
            return saveaddress;
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
}
