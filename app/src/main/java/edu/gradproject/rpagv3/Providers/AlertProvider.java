package edu.gradproject.rpagv3.Providers;

import android.net.Uri;
import android.util.Log;

import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gradproject.rpagv3.MainActivity;
import edu.gradproject.rpagv3.Models.AlertData;

public class AlertProvider {
    private CollectionReference mCollection;
    private StorageReference mAlertImageStorage;

    public AlertProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Alerts");
        mAlertImageStorage = FirebaseStorage.getInstance().getReference().child("AlertImages");
        Log.d(MainActivity.LOG_TAG, "Alert Provider initialized");
    }

    public Task<Void> create(AlertData alertData) {
        DocumentReference newDocument = mCollection.document();
        Map<String, Object> alert = new HashMap<>();

        alert.put("userId", alertData.getUserId());
        alert.put("typeId", alertData.getTypeId());
        alert.put("description", alertData.getDescription());
        alert.put("date", alertData.getDate());
        alert.put("latitude", alertData.getLatitude());
        alert.put("longitude", alertData.getLongitude());
        alert.put("geohash", alertData.getGeohash());
        alert.put("deleted", alertData.isDeleted());

        return newDocument.set(alert);
    }

    public Task<Void> create(AlertData alertData, DocumentReference documentReference) {
        Map<String, Object> alert = new HashMap<>();

        alert.put("userId", alertData.getUserId());
        alert.put("typeId", alertData.getTypeId());
        alert.put("description", alertData.getDescription());
        alert.put("date", alertData.getDate());
        alert.put("latitude", alertData.getLatitude());
        alert.put("longitude", alertData.getLongitude());
        alert.put("geohash", alertData.getGeohash());
        alert.put("deleted", alertData.isDeleted());

        return documentReference.set(alert);
    }

    public DocumentReference getAlert(String alertId) {
        return mCollection.document(alertId);
    }

    public static Task<Void> setImagesOnAlertDocument(DocumentReference alertRef, List<String> images) {
        return alertRef.update("images", images);
    }

    public UploadTask uploadFileToStorage(Uri fileUri) {
        return mAlertImageStorage.child(fileUri.getLastPathSegment()).putFile(fileUri);
    }

    public Task<byte[]> getImageFile(String fileName) {
        return mAlertImageStorage.child(fileName).getBytes(2 * 1024 * 1024);
    }

    public DocumentReference getNewDocument() {
        return mCollection.document();
    }

    public Query getAlertsByGeohashBounds(GeoQueryBounds bounds) {
        return mCollection.orderBy("geohash")
                .startAt(bounds.startHash)
                .endAt(bounds.endHash);
    }
}
