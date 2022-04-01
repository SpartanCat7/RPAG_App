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
import edu.integrator.rpagv2.Models.Vote;

public class VoteProvider {
    private CollectionReference mCollection;

    public VoteProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Votes");
        Log.d(MainActivity.LOG_TAG, "Vote Provider initialized");
    }

    public Task<Void> create(Vote newVote) {
        DocumentReference newDocument = mCollection.document();
        Map<String, Object> vote = new HashMap<>();

        vote.put("id", newDocument.getId());
        vote.put("alertId", newVote.getAlertId());
        vote.put("userId", newVote.getUserId());
        vote.put("date", newVote.getDate());
        vote.put("voteTrue", newVote.isVoteTrue());

        return newDocument.set(vote);
    }

    public Task<Void> create(Vote newVote, DocumentReference documentReference) {
        Map<String, Object> vote = new HashMap<>();

        vote.put("id", newVote.getId());
        vote.put("alertId", newVote.getAlertId());
        vote.put("userId", newVote.getUserId());
        vote.put("date", newVote.getDate());
        vote.put("voteTrue", newVote.isVoteTrue());

        return documentReference.set(vote);
    }

    public Task<Void> remove(String voteId) {
        return mCollection.document(voteId).delete();
    }

    public Task<Void> updateVote(String voteId, boolean voteTrue) {
        return mCollection.document(voteId).update("voteTrue", voteTrue);
    }

    public DocumentReference getNewDocument() {
        return mCollection.document();
    }

    public Query getVotesByAlert(String alertId) {
        return mCollection.whereEqualTo("alertId", alertId);
    }
}
