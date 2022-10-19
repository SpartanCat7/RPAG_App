package edu.gradproject.rpagv3.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class Comment implements java.io.Serializable {
    private String id;
    private String alertId;
    private String userId;
    private Date date;
    private boolean active;

    private String text;

    public Comment(String id, String alertId, String userId, Date date, String text, boolean active) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date;
        this.text = text;
        this.active = active;
    }

    public Comment(String id, String alertId, String userId, Timestamp date, String text, boolean active) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date.toDate();
        this.text = text;
        this.active = active;
    }

    public Comment(DocumentSnapshot snap) {
        this.id = snap.getId();
        this.alertId = null;
        this.userId = snap.getString("userId");
        this.date = snap.getDate("date");
        this.text = snap.getString("text");
        this.active = Boolean.TRUE.equals(snap.getBoolean("active"));
    }

    public Comment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
