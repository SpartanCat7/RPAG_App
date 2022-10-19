package edu.gradproject.rpagv3.Models;

import android.graphics.Bitmap;

import com.google.firebase.Timestamp;

import java.io.File;
import java.util.Date;

public class ImageData implements java.io.Serializable {
    private String fileName;
    private Date date;
    private Bitmap bitmap;
    private File file;

    public ImageData(String fileName, Date date, Bitmap bitmap, File file) {
        this.fileName = fileName;
        this.date = date;
        this.bitmap = bitmap;
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
