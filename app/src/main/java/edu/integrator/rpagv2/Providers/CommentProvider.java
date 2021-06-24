package edu.integrator.rpagv2.Providers;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

import edu.integrator.rpagv2.MainActivity;
import edu.integrator.rpagv2.Models.Comment;

public class CommentProvider {
    private CollectionReference mCollection;

    public CommentProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Comments");
        Log.d(MainActivity.LOG_TAG, "Comment Provider initialized");
    }

    public Task<Void> create(Comment newComment) {
        DocumentReference newDocument = mCollection.document();
        Map<String, Object> comentario = new HashMap<>();

        comentario.put("id", newDocument.getId());
        comentario.put("alertId", newComment.getAlertId());
        comentario.put("userId", newComment.getUserId());
        comentario.put("date", newComment.getDate());
        comentario.put("text", newComment.getText());

        return newDocument.set(comentario);
    }

    public Task<Void> create(Comment newComment, DocumentReference documentReference) {
        Map<String, Object> comentario = new HashMap<>();

        comentario.put("id", newComment.getId());
        comentario.put("alertId", newComment.getAlertId());
        comentario.put("userId", newComment.getUserId());
        comentario.put("date", newComment.getDate());
        comentario.put("text", newComment.getText());

        return documentReference.set(comentario);
    }

    public DocumentReference getNewDocument() {
        return mCollection.document();
    }

    public Query getCommentsByAlert(String alertId) {
        return mCollection.whereEqualTo("alertId", alertId);
    }
}
