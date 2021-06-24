package edu.integrator.rpagv2.Models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Comment implements java.io.Serializable {
    private String id;
    private String alertId;
    private String userId;
    private Date date;

    private String text;

    public Comment(String id, String alertId, String userId, Date date, String text) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date;
        this.text = text;
    }

    public Comment(String id, String alertId, String userId, Timestamp date, String text) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date.toDate();
        this.text = text;
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
}
