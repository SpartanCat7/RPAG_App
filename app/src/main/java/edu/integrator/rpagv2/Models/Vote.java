package edu.integrator.rpagv2.Models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Vote implements java.io.Serializable {
    private String id;
    private String alertId;
    private String userId;
    private Date date;
    private boolean voteTrue;

    public Vote(String id, String alertId, String userId, Date date, boolean voteTrue) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date;
        this.voteTrue = voteTrue;
    }

    public Vote(String id, String alertId, String userId, Timestamp date, boolean voteTrue) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date.toDate();
        this.voteTrue = voteTrue;
    }

    public Vote() {
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

    public boolean isVoteTrue() {
        return voteTrue;
    }

    public void setVoteTrue(boolean voteTrue) {
        this.voteTrue = voteTrue;
    }
}