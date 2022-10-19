package edu.gradproject.rpagv3.Providers;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gradproject.rpagv3.MainActivity;
import edu.gradproject.rpagv3.Models.Comment;
import edu.gradproject.rpagv3.Models.Vote;

public class CommentProvider {
    private CollectionReference mCollection;

    public CommentProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Comments");
        Log.d(MainActivity.LOG_TAG, "Comment Provider initialized");
    }

//    public Task<Void> create(Comment newComment) {
//        DocumentReference newDocument = mCollection.document();
//        Map<String, Object> comentario = new HashMap<>();
//
//        comentario.put("id", newDocument.getId());
//        comentario.put("alertId", newComment.getAlertId());
//        comentario.put("userId", newComment.getUserId());
//        comentario.put("date", newComment.getDate());
//        comentario.put("text", newComment.getText());
//
//        return newDocument.set(comentario);
//    }
//
//    public Task<Void> create(Comment newComment, DocumentReference documentReference) {
//        Map<String, Object> comentario = new HashMap<>();
//
//        comentario.put("id", newComment.getId());
//        comentario.put("alertId", newComment.getAlertId());
//        comentario.put("userId", newComment.getUserId());
//        comentario.put("date", newComment.getDate());
//        comentario.put("text", newComment.getText());
//
//        return documentReference.set(comentario);
//    }
//
//    public DocumentReference getNewDocument() {
//        return mCollection.document();
//    }
//
//    public Query getCommentsByAlert(String alertId) {
//        return mCollection.whereEqualTo("alertId", alertId);
//    }

    public static Query getComments(DocumentReference alertRef) {
        return alertRef.collection("Comments");
    }

    public static Task<Void> addComment(Comment newComment, DocumentReference alertRef) {
        DocumentReference newDocument = alertRef.collection("Comments").document();
        Map<String, Object> comment = new HashMap<>();

        comment.put("userId", newComment.getUserId());
        comment.put("date", newComment.getDate());
        comment.put("text", newComment.getText());
        comment.put("active", newComment.isActive());

        return newDocument.set(comment);
    }

    public static Task<Void> updateCommentActive(DocumentReference alertRef, String commentId, boolean active) {
        return alertRef.collection("Comments").document(commentId).update("active", active);
    }

    public static ArrayList<Comment> DocSnapListToCommentArrayList(List<DocumentSnapshot> snapshots) {
        ArrayList<Comment> newList = new ArrayList<>();
        for (DocumentSnapshot snap : snapshots)
            newList.add(new Comment(snap));
        return newList;
    }
}
