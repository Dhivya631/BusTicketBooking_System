package com.application.userservice.entity;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String sessionId;

    public AuthResponse(String token, String id) {
        this.token=token;
        this.sessionId=id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}