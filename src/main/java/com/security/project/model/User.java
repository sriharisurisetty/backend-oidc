package com.security.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

	@Id
	private String id;
	private String firstName;
	private String lastName;
	private String email;
	private long createdAt;
	private int loginCountLast24Hours = 0;
	private long lastSessionTimestamp = 0L;
	private String providerId;
	private boolean email_verified;
	private boolean consent;
	private String password;
	private String resetToken;
	private long resetTokenExpiry;
	private int failedLoginAttempts = 0;
	private boolean accountLocked = false;
	private String otp;
	private long otpExpiry;
	private String familyNumber;

	public User(String id, String firstName, String lastName, String email, long createdAt, int loginCountLast24Hours,
			long lastSessionTimestamp, String providerId, boolean email_verified, boolean consent, String password) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.createdAt = createdAt;
		this.loginCountLast24Hours = loginCountLast24Hours;
		this.lastSessionTimestamp = lastSessionTimestamp;
		this.providerId = providerId;
		this.email_verified = email_verified;
		this.consent = consent;
		this.password = password;
	}

	// Constructors
	public User() {
	}

	public User(String firstName, String email) {
		this.firstName = firstName;
		this.email = email;
		this.createdAt = System.currentTimeMillis();
		this.loginCountLast24Hours = 0;
		this.lastSessionTimestamp = 0L;
	}

	public User(String firstName, String lastName, String email, String providerId, boolean email_verified,
			String password) {
		super();
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.providerId = providerId;
		this.email_verified = email_verified;
		this.password = password;
	}

	public boolean isEmail_verified() {
		return email_verified;
	}

	public void setEmail_verified(boolean email_verified) {
		this.email_verified = email_verified;
	}

	public int getLoginCountLast24Hours() {
		return loginCountLast24Hours;
	}

	public void setLoginCountLast24Hours(int loginCountLast24Hours) {
		this.loginCountLast24Hours = loginCountLast24Hours;
	}

	public long getLastSessionTimestamp() {
		return lastSessionTimestamp;
	}

	public void setLastSessionTimestamp(long lastSessionTimestamp) {
		this.lastSessionTimestamp = lastSessionTimestamp;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public long getOtpExpiry() {
		return otpExpiry;
	}

	public void setOtpExpiry(long otpExpiry) {
		this.otpExpiry = otpExpiry;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// Getters and Setters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isConsent() {
		return consent;
	}

	public void setConsent(boolean consent) {
		this.consent = consent;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", createdAt=" + createdAt + ", loginCountLast24Hours=" + loginCountLast24Hours
				+ ", lastSessionTimestamp=" + lastSessionTimestamp + ", providerId=" + providerId + ", email_verified="
				+ email_verified + ", consent=" + consent + ", password=" + password + "]";
	}

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public long getResetTokenExpiry() {
		return resetTokenExpiry;
	}

	public void setResetTokenExpiry(long resetTokenExpiry) {
		this.resetTokenExpiry = resetTokenExpiry;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public boolean isAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getFamilyNumber() {
		return familyNumber;
	}

	public void setFamilyNumber(String familyNumber) {
		this.familyNumber = familyNumber;
	}
}
