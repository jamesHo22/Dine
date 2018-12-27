package com.example.dine.dine;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dine.dine.RoomDb.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains methods that handles data manipulation
 */
public class DataHandlingUtils {

    private final static String TAG = DataHandlingUtils.class.getSimpleName();
    private AppDatabase roomDb;
    public static void DataHandlingUtils() {

    }

    /**
     * this builds a query for FireStore that satisfies the user preferences
     * @param itemRef is the initial path to the collection
     * @param preferences is a List of booleans that represent the user set preferences
     * @return a Query object that is used to call Firestore.
     */
    public static Query buildQuery(Query itemRef, List<Boolean> preferences) {
        boolean first_count = true;
        boolean gluten_free_count = true;
        boolean vegan_count = true;
        boolean vegetarian_count = true;

        Query newQuery = null;

        for (int i = 0; i <= 3; i ++) {
            Log.d(TAG, "buildQuery: " + String.valueOf(i));

            if (first_count) {
                // will only run once by setting count to false
                first_count = false;
                // sets newQuery with "where" clause if it is gluten free. Begin with default itemRef
                newQuery = itemRef;
                Log.d(TAG, "buildQuery: count is " + String.valueOf(first_count));

            } else if (gluten_free_count && preferences.get(0)) {
                gluten_free_count = false;
                newQuery = newQuery.whereEqualTo("gluten_free", true);
                Log.d(TAG, "buildQuery: gluten_count is " + String.valueOf(gluten_free_count));

            } else if (vegan_count && preferences.get(1)) {
                vegan_count = false;
                newQuery = newQuery.whereEqualTo("vegan", true);
                Log.d(TAG, "buildQuery: vegan_count is " + String.valueOf(vegan_count));

            } else if (vegetarian_count && preferences.get(2)) {
                vegetarian_count = false;
                newQuery = newQuery.whereEqualTo("vegetarian", true);
                Log.d(TAG, "buildQuery: vegetarian_count is " + String.valueOf(vegetarian_count));
            } else if (areAllFalse(preferences)) {
                newQuery = itemRef;
            }
        }

        return newQuery;
    }

    /**
     * Checks if all of the values in the List are false
     * @param array
     * @return boolean value. True if all are false
     */
    private static boolean areAllFalse(List<Boolean> array) {
        for(boolean b : array) if(b) return false;
        return true;
    }

    /**
     * Makes a copy of the menu items that was clicked. Attaches timestamp and name of person who ordered it.
     * Sets this new HashMap to the "current_orders" path.
     * FIXME: instead of using Firestore RTDB, learn how to use FCM.
     * @param mAuth is a firestore Auth that is used to retrieve the display name
     * @param db is the database reference
     * @param documentSnapshot is the Firestore document snapshot passed from the order.
     * @param context is the context of the activity.
     */
    public void moveItemToCurrentOrders(FirebaseAuth mAuth, FirebaseFirestore db, DocumentSnapshot documentSnapshot, Context context) {

        Toast.makeText(context, "You ordered " + String.valueOf(documentSnapshot.get("title")), Toast.LENGTH_SHORT).show();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String orderer = currentUser.getDisplayName();

        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("ordered_by", orderer);
        orderInfo.put("order_time", FieldValue.serverTimestamp());
        // Combine documentSnapshot and orderInfo Maps
        orderInfo.putAll(documentSnapshot.getData());

        db.collection("restaurants")
                .document("aqvUJjyokpta9KyBFz9U")
                .collection("current_orders").document()
                .set(orderInfo);
    }

    /**
     * Checks preferences then makes a query that used to call firestore
     */
    //FIXME: Make a function that generates a query based on location and preferences and have it call this method and getLocation method.
    public static Query makePrefQuery(Context context, CollectionReference itemRef) {

        // ensure settings are initialized with their default values
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        // Read values from shared preferences
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context);
        Boolean glutenFreeSwitchPref = sharedPref.getBoolean
                (SettingsActivity.KEY_GLUTEN_FREE_SWITCH, false);
        Boolean veganSwitchPref = sharedPref.getBoolean
                (SettingsActivity.KEY_VEGAN_SWITCH, false);
        Boolean vegetarianSwitchPref = sharedPref.getBoolean
                (SettingsActivity.KEY_VEGETARIAN_SWITCH, false);

        // Make a arraylist
        List<Boolean> preferences = new ArrayList<>();
        preferences.add(glutenFreeSwitchPref);
        preferences.add(veganSwitchPref);
        preferences.add(vegetarianSwitchPref);

        // Make query that satisfies user preferences.
        Query mQuery = buildQuery(itemRef, preferences);
        return mQuery;
    }
}
