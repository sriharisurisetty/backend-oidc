package com.security.project.password;

public class PasswordResponse {
    private final String password;

    public PasswordResponse(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}