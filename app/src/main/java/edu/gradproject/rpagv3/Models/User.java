package edu.gradproject.rpagv3.Models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class User {
    private String userId;
    private String username;
    private String type;
    private String telfNumber;
    private String fullName;
    private String email;
    private boolean active;

    public User(String userId, String phoneNumber) {
        this.userId = userId;
        this.telfNumber = phoneNumber;
        this.type = "USER";
    }

    public User(String userId, String phoneNumber, String type) {
        this.userId = userId;
        this.telfNumber = phoneNumber;
        this.type = type;
    }

    public User(DocumentSnapshot snap) {
        this.userId = snap.getId();
        this.username = snap.getString("username");
        this.type = snap.getString("type");
        this.telfNumber = snap.getString("telf_number");
        this.telfNumber = snap.getString("full_name");
        this.email = snap.getString("email");
        this.active = Boolean.TRUE.equals(snap.getBoolean("active"));
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTelfNumber() {
        return telfNumber;
    }

    public void setTelfNumber(String telfNumber) {
        this.telfNumber = telfNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
