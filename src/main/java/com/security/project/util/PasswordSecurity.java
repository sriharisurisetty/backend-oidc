package com.security.project.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordSecurity {

 @Bean
 public BCryptPasswordEncoder passwordEncoder() {
     // Strength defaults to 10; you can pass a higher value (e.g., new BCryptPasswordEncoder(12))
     return new BCryptPasswordEncoder();
 }
 


public boolean passwordDecoder(String rawPassword, String encodedPassword) {
	BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
	boolean isPasswordMatch = bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
	if (isPasswordMatch) {
		return true;
	} else {
		return false;
	}
}

 
}

