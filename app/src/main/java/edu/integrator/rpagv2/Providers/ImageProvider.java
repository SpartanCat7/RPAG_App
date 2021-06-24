package edu.integrator.rpagv2.Providers;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import edu.integrator.rpagv2.MainActivity;
import edu.integrator.rpagv2.Models.ImageData;

public class ImageProvider {
    private CollectionReference mCollection;
    private StorageReference mStorageRef;

    public ImageProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Images");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("images");
        Log.d(MainActivity.LOG_TAG, "Image Provider initialized");
    }

    public Task<Void> create(ImageData newImageData) {
        DocumentReference newDocument = mCollection.document();
        Map<String, Object> image = new HashMap<>();

        image.put("id", newDocument.getId());
        image.put("fileName", newImageData.getFileName());
        image.put("alertId", newImageData.getAlertId());
        image.put("userId", newImageData.getUserId());
        image.put("date", newImageData.getDate());

        return newDocument.set(image);
    }

    public Task<Void> create(ImageData newImageData, DocumentReference documentReference) {
        Map<String, Object> image = new HashMap<>();

        image.put("id", newImageData.getId());
        image.put("fileName", newImageData.getFileName());
        image.put("alertId", newImageData.getAlertId());
        image.put("userId", newImageData.getUserId());
        image.put("date", newImageData.getDate());

        return documentReference.set(image);
    }

    public DocumentReference getNewDocument() {
        return mCollection.document();
    }

    public Query getImagesByAlert(String alertId) {
        return mCollection.whereEqualTo("alertId", alertId);
    }

    public Task<byte[]> getFile(String name) {
        return mStorageRef.child(name).getBytes(2 * 1024 * 1024);
    }

    public UploadTask uploadStorageFile(Uri fileUri) {
        return mStorageRef.child(fileUri.getLastPathSegment()).putFile(fileUri);
    }
}
