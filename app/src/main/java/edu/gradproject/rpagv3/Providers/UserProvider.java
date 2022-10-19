package edu.gradproject.rpagv3.Providers;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import edu.gradproject.rpagv3.MainActivity;
import edu.gradproject.rpagv3.Models.User;

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

    public Task<Void> createEmptyProfileWithTelf(User user) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("type", user.getType());
        newUser.put("telf_number", user.getTelfNumber());
        return mCollection.document(user.getUserId()).set(newUser);
    }

    public Task<Void> createFullProfile(User user) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", user.getUsername());
        newUser.put("type", user.getType());
        newUser.put("telf_number", user.getTelfNumber());
        newUser.put("full_name", user.getFullName());
        newUser.put("email", user.getEmail());
        newUser.put("active", user.isActive());
        return mCollection.document(user.getUserId()).set(newUser);
    }

    public Task<Void> updateUserUsername(String userId, String username) {
        return mCollection.document(userId).update("username", username);
    }
    public Task<Void> updateUserPhone(String userId, String phone) {
        return mCollection.document(userId).update("telf_number", phone);
    }
    public Task<Void> updateUserFullname(String userId, String fullname) {
        return mCollection.document(userId).update("full_name", fullname);
    }
    public Task<Void> updateUserEmail(String userId, String email) {
        return mCollection.document(userId).update("email", email);
    }

    public Task<DocumentSnapshot> getUserById(String userId) {
        return mCollection.document(userId).get();
    }
}
