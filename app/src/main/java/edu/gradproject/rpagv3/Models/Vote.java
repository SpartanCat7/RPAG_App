package edu.gradproject.rpagv3.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class Vote implements java.io.Serializable {
    private String id;
    private String alertId;
    private String userId;
    private Date date;
    private boolean voteTrue;
    private boolean active;

    public Vote(String id, String alertId, String userId, Date date, boolean voteTrue, boolean active) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date;
        this.voteTrue = voteTrue;
        this.active = active;
    }

    public Vote(String id, String alertId, String userId, Timestamp date, boolean voteTrue, boolean active) {
        this.id = id;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date.toDate();
        this.voteTrue = voteTrue;
        this.active = active;
    }

    public Vote(DocumentSnapshot snap) {
        this.id = snap.getId();
        this.alertId = null;
        this.userId = snap.getId();
        this.date = snap.getDate("date");
        this.voteTrue = Boolean.TRUE.equals(snap.getBoolean("voteTrue"));
        this.active = Boolean.TRUE.equals(snap.getBoolean("active"));
    }

    public Vote(DocumentSnapshot snap, String alertId) {
        this.id = snap.getId();
        this.alertId = alertId;
        this.userId = snap.getId();
        this.date = snap.getDate("date");
        this.voteTrue = Boolean.TRUE.equals(snap.getBoolean("voteTrue"));
        this.active = Boolean.TRUE.equals(snap.getBoolean("active"));
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}