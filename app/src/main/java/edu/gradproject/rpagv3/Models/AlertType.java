package edu.gradproject.rpagv3.Models;

import android.graphics.Bitmap;

import com.google.firebase.firestore.DocumentSnapshot;

public class AlertType {
    private String id;
    private String name;
    private String description;
    private String icon;
    private Integer lifetime;
    private Boolean active;

    //private byte[] iconFile;
    private Bitmap iconBitmap;

    public AlertType(String id, String name, String description, String icon, int lifetime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.lifetime = lifetime;
    }

    public AlertType(DocumentSnapshot snapshot) {
        this.id = snapshot.getId();
        this.name = snapshot.get("name", String.class);
        this.description = snapshot.get("description", String.class);
        this.icon = snapshot.get("icon", String.class);
        this.lifetime = snapshot.get("lifetime", int.class);
        this.active = snapshot.get("active", Boolean.class);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }
}
