package com.security.project.service;

import com.security.project.model.User;
import com.security.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
	
    @Autowired
    private UserRepository userRepository;
   
    /**
     * Save a new user if they don't already exist by email
     */
    public User saveUserIfNotExists(String email, String firstName, String lastName, String provider, boolean email_verified) {
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
            userRepository.save(user);
             // ensure up-to-date
            return user;
        }
        User newUser = new User(firstName, lastName, email, provider,email_verified);
        newUser.setLoginCountLast24Hours(1);
        newUser.setLastSessionTimestamp(now);
        userRepository.save(newUser);
        return newUser;
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
}
