package com.wordsprint.model;

import java.time.LocalDateTime;

public class User {
    private long user_id;
    private String username;
    private String passwordHash;
    private String role;
    private LocalDateTime createdAt;

    public void setUserId (Long user_id) {
        this.user_id = user_id;
    }

    public void setUserName (String uname) {
        this.username = uname;
    }

    public void setPasswordHash(String pwdHash) {
        this.passwordHash = pwdHash;
    }

    public void setUserRole (String role) {
        this.role = role;
    }

    public void setCreatedAt (LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId () {
        return this.user_id;
    }

    public String getUserName () {
        return this.username;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public String getUserRole () {
        return this.role;
    }

    public LocalDateTime getCreatedAt () {
        return this.createdAt;
    }
}