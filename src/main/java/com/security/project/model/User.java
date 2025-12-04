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
    
    public User(String id, String firstName, String email, String lastName, long createdAt, int loginCountLast24Hours,
			long lastSessionTimestamp, String providerId, boolean email_verified) {
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
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
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

    public User(String firstName, String lastName, String email, String providerId, boolean email_verified) {
		super();
		this.lastName = lastName;
		this.firstName = firstName;
		this.email = email;
		this.providerId = providerId;
		this.email_verified = email_verified;
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
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", email=" + email + ", createdAt=" + createdAt
				+ ", loginCountLast24Hours=" + loginCountLast24Hours + ", lastSessionTimestamp=" + lastSessionTimestamp
				+ ", providerId=" + providerId + ", email_verified=" + email_verified + "]";
	}
}
