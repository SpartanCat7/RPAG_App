package edu.gradproject.rpagv3.Providers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import edu.gradproject.rpagv3.MainActivity;
import edu.gradproject.rpagv3.Models.AlertType;

public class AlertTypeProvider {
    private final CollectionReference mCollection;
    private final StorageReference mStorageRef;

    public AlertTypeProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("AlertTypes");
        //mStorageRef = FirebaseStorage.getInstance().getReference("AlertTypeIcons");
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public Task<QuerySnapshot> getAllAlertTypes() {
        return mCollection.get();
    }

    public void getTypeIconFile(Context context, String filename, getTypeIconFileCallback callback) {
        File file = getTypeIconFileFromCache(context, filename);
        if (file.exists()) {
            try {
                byte[] bytes = FileUtils.readFileToByteArray(file);
                callback.onSuccess(bytes);
                callback.Finally();
            } catch (IOException e) {
                Log.e(MainActivity.LOG_TAG, "Could not recover file" + filename + " from cache");
                e.printStackTrace();
                callback.onFailure();
                callback.Finally();
            }
        } else {
            getTypeIconFileFromStorage(filename).addOnCompleteListener((Task<byte[]> getFileFromStorageTask) -> {
                if (getFileFromStorageTask.isSuccessful()) {
                    byte[] bytes = getFileFromStorageTask.getResult();
                    saveTypeIconFileToCache(file, bytes);
                    callback.onSuccess(bytes);
                    callback.Finally();
                } else {
                    Log.e(MainActivity.LOG_TAG, "Could not recover file" + filename + " from Firebase Storage");
                    callback.onFailure();
                    callback.Finally();
                }
            });
        }
    }

    public interface getTypeIconFileCallback {
        void onSuccess(byte[] bytes);
        void onFailure();
        void Finally();
    }

    public Task<byte[]> getTypeIconFileFromStorage(String filename) {
         return mStorageRef.child(filename).getBytes(25 * 1024);
    }

    public void saveTypeIconFileToCache(File file, byte[] bytes) {
        try {
            FileUtils.writeByteArrayToFile(file, bytes);
        } catch (IOException e) {
            Log.e(MainActivity.LOG_TAG, "Could not save AlertType icon file " + file.getName() + " to cache");
            e.printStackTrace();
        }
    }
    public File getTypeIconFileFromCache(Context context, String filename) {
        return new File(getTypeIconFileDirectory(context), filename);
    }

    public static File getTypeIconFileDirectory(Context context) {
        File dir = new File(context.getCacheDir(), "TypeIconFiles");
        if (!dir.exists() && !dir.mkdir()) {
            Log.e(MainActivity.LOG_TAG, "TypeIconFiles cache directory doesn't exist and couldn't be created");
        }
        return new File(dir, "TypeIconFiles");
    }

    public static AlertType getAlertType(String typeId, ArrayList<AlertType> alertTypeList) {
        for (AlertType alertType : alertTypeList) {
            if (Objects.equals(typeId, alertType.getId())) {
                return alertType;
            }
        }
        return null;
    }

    public static ArrayList<AlertType> updateAlertTypeList(AlertType alertType, ArrayList<AlertType> list) {
        AlertType existingType = AlertTypeProvider.getAlertType(alertType.getId(), list);
        if (existingType == null) {
            list.add(alertType);
        } else {
            list.set(list.indexOf(existingType), alertType);
        }
        return list;
    }
}
