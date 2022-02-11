package edu.integrator.rpagv2.Providers;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.integrator.rpagv2.MainActivity;

public class UserProvider {
    private CollectionReference mCollection;

    public UserProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Users");
        Log.d(MainActivity.LOG_TAG, "Users Provider initialized");
    }

    public Task<Void> create(String userId, String username) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        return mCollection.document(userId).set(user);
    }

    public DocumentReference getUserById(String userId) {
        return mCollection.document(userId);
    }
}
