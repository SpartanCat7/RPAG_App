package edu.integrator.rpagv2.Providers;

import android.util.Log;

import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

import edu.integrator.rpagv2.MainActivity;
import edu.integrator.rpagv2.Models.AlertData;

public class AlertProvider {
    private CollectionReference mCollection;

    public AlertProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Alerts");
        Log.d(MainActivity.LOG_TAG, "Alert Provider initialized");
    }

    public Task<Void> create(AlertData alertData) {
        DocumentReference newDocument = mCollection.document();
        Map<String, Object> alert = new HashMap<>();

        alert.put("id", newDocument.getId());
        alert.put("userId", alertData.getUserId());
        alert.put("classId", alertData.getClassId());
        alert.put("date", alertData.getDate());
        alert.put("latitude", alertData.getLatitude());
        alert.put("longitude", alertData.getLongitude());
        alert.put("geohash", alertData.getGeohash());
        alert.put("customName", alertData.getCustomName());

        return newDocument.set(alert);
    }

    public Task<Void> create(AlertData alertData, DocumentReference documentReference) {
        Map<String, Object> alert = new HashMap<>();

        alert.put("id", alertData.getId());
        alert.put("userId", alertData.getUserId());
        alert.put("classId", alertData.getClassId());
        alert.put("date", alertData.getDate());
        alert.put("latitude", alertData.getLatitude());
        alert.put("longitude", alertData.getLongitude());
        alert.put("geohash", alertData.getGeohash());
        alert.put("customName", alertData.getCustomName());

        return documentReference.set(alert);
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
