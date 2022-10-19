package edu.gradproject.rpagv3.Providers;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import edu.gradproject.rpagv3.MainActivity;

public class CountryProvider {
    private CollectionReference mCollection;

    public CountryProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Countries");
        Log.d(MainActivity.LOG_TAG, "Countries Provider initialized");
    }

    public Query getCountryNumbers(String countryCode) {
        return mCollection.document(countryCode).collection("numbers");
    }
}
