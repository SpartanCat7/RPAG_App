package edu.gradproject.rpagv3.Models;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.Date;
import java.util.List;

public class AlertData implements java.io.Serializable {
    private String id;
    private String userId;
    private String typeId;

    private double latitude;
    private double longitude;
    private String geohash;
    private String address;

    private Date date;

    private String description;
    private List<String> images;
    private boolean deleted;

    public AlertData(DocumentSnapshot snapshot) {
        this.id = snapshot.getId();
        this.userId = snapshot.get("userId", String.class);
        this.latitude = snapshot.get("latitude", Double.class);
        this.longitude = snapshot.get("longitude", Double.class);
        this.geohash = snapshot.get("geohash", String.class);
        this.address = snapshot.get("location", String.class);
        this.typeId = snapshot.get("typeId", String.class);
        this.date = snapshot.getDate("date");
        this.description = snapshot.get("description", String.class);
        Object imageListObj = snapshot.get("images");
        if (imageListObj != null) {
            try {
                this.images = (List<String>) snapshot.get("images");
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        this.deleted = Boolean.TRUE.equals(snapshot.get("deleted", boolean.class));
    }

    public AlertData(String id, String userId, double latitude, double longitude, String typeId, Date date) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.typeId = typeId;
        this.date = date;
        //this.fecha = new Timestamp(fecha);
        this.geohash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
        this.deleted = false;
    }

    public AlertData(String id, String userId, double latitude, double longitude, String typeId, Date date, List<String> images) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.typeId = typeId;
        this.date = date;
        //this.fecha = new Timestamp(fecha);
        this.geohash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
        this.images = images;
        this.deleted = false;
    }

    public AlertData(String id, String userId, String typeId, double latitude, double longitude, String address, Date date, String description, List<String> images, boolean deleted) {
        this.id = id;
        this.userId = userId;
        this.typeId = typeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.description = description;
        this.geohash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
        this.address = address;
        this.images = images;
        this.deleted = deleted;
    }

    /*
    public AlertData(String id, String userId, double latitude, double longitude, String typeId, Timestamp date) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.typeId = typeId;
        this.date = date.toDate();
        //this.fecha = fecha;
        this.geohash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
    }

    public AlertData(String id, String userId, double latitude, double longitude, String typeId, Timestamp date, List<String> images) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.typeId = typeId;
        this.date = date.toDate();
        //this.fecha = fecha;
        this.geohash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
        this.images = images;
    }
    */

    public static String toJsonString(AlertData data) throws JSONException {
        //JSONObject obj = new JSONObject();
        if (data != null) {
            /*obj.put("id", data.getId());
            obj.put("userId", data.getUserId());
            obj.put("typeId", data.getTypeId());
            obj.put("description", data.getDescription());
            obj.put("description", data.getDate());
            obj.put("description", data.getLatitude());
            obj.put("description", data.getLongitude());
            obj.put("description", data.getGeohash());
            obj.put("description", data.get());
            obj.put("description", data.getDescription());*/
            return new Gson().toJson(data);
        }
        return null;
    }

    public static AlertData fromJsonString(String json) {
        return new Gson().fromJson(json, AlertData.class);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}