package edu.integrator.rpagv2.Models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class ImageData implements java.io.Serializable {
    private String id;
    private String fileName;
    private String alertId;
    private String userId;
    private Date date;
    private byte[] bitmap;

    public ImageData(String id, String fileName, String alertId, String userId, Date date, byte[] bitmap) {
        this.id = id;
        this.fileName = fileName;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date;
        this.bitmap = bitmap;
    }

    public ImageData(String id, String fileName, String alertId, String userId, Timestamp date, byte[] bitmap) {
        this.id = id;
        this.fileName = fileName;
        this.alertId = alertId;
        this.userId = userId;
        this.date = date.toDate();
        this.bitmap = bitmap;
    }

    public ImageData() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public byte[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }
}
