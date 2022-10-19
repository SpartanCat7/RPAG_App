package edu.gradproject.rpagv3.Providers;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gradproject.rpagv3.MainActivity;
import edu.gradproject.rpagv3.Models.Vote;

public class VoteProvider {
    private CollectionReference mCollection;

    public VoteProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Votes");
        Log.d(MainActivity.LOG_TAG, "Vote Provider initialized");
    }

//    public Task<Void> create(Vote newVote) {
//        DocumentReference newDocument = mCollection.document();
//        Map<String, Object> vote = new HashMap<>();
//
//        vote.put("id", newDocument.getId());
//        vote.put("alertId", newVote.getAlertId());
//        vote.put("userId", newVote.getUserId());
//        vote.put("date", newVote.getDate());
//        vote.put("voteTrue", newVote.isVoteTrue());
//
//        return newDocument.set(vote);
//    }

//    public Task<Void> create(Vote newVote, DocumentReference documentReference) {
//        Map<String, Object> vote = new HashMap<>();
//
//        vote.put("id", newVote.getId());
//        vote.put("alertId", newVote.getAlertId());
//        vote.put("userId", newVote.getUserId());
//        vote.put("date", newVote.getDate());
//        vote.put("voteTrue", newVote.isVoteTrue());
//
//        return documentReference.set(vote);
//    }

    public static Query getVotes(DocumentReference alertRef) {
        return alertRef.collection("Votes");
    }

    public static Task<Void> addVote(Vote newVote, DocumentReference alertRef) {
        DocumentReference newDocument = alertRef.collection("Votes").document(newVote.getUserId());
        Map<String, Object> vote = new HashMap<>();

        vote.put("date", newVote.getDate());
        vote.put("voteTrue", newVote.isVoteTrue());
        vote.put("active", newVote.isActive());

        return newDocument.set(vote);
    }

    public static Task<Void> updateVote(DocumentReference alertRef, String voteId, boolean voteTrue, boolean active) {
        return alertRef.collection("Votes").document(voteId).update("voteTrue", voteTrue, "active", active);
    }

    /*public static Task<Void> removeVote(DocumentReference alertRef, String voteId) {
        return alertRef.collection("Votes").document(voteId).delete();
    }*/

    public static ArrayList<Vote> DocSnapListToVoteArrayList(List<DocumentSnapshot> snapshots) {
        ArrayList<Vote> newList = new ArrayList<>();
        for (DocumentSnapshot snap : snapshots)
            newList.add(new Vote(snap));
        return newList;
    }

//    public Task<Void> remove(String voteId) {
//        return mCollection.document(voteId).delete();
//    }

//    public Task<Void> updateVote(String voteId, boolean voteTrue) {
//        return mCollection.document(voteId).update("voteTrue", voteTrue);
//    }

//    public DocumentReference getNewDocument() {
//        return mCollection.document();
//    }

//    public Query getVotesByAlert(String alertId) {
//        return mCollection.whereEqualTo("alertId", alertId);
//    }
}
