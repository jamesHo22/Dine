package com.example.dine.dine;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dine.dine.RoomDb.AppDatabase;
import com.example.dine.dine.RoomDb.ItemEntry;
import com.example.dine.dine.RoomDb.LocationEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains methods that handles data manipulation
 */
public class DataHandlingUtils {

    private final static String TAG = DataHandlingUtils.class.getSimpleName();
    private AppDatabase roomDb;
    public static final String DOCUMENT_ID = "document_id";

    public static void DataHandlingUtils() {
    }

    private static Boolean itemRefIsSet;
    public static Boolean setItemRef(final String documentId, FirebaseFirestore db, final Context context){
        itemRefIsSet = false;
        // check if document with that user exists
        db.collection("restaurants_2")
                .document(documentId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Boolean exists = task.getResult().exists();
                if (exists) {
                    SharedPreferences.Editor sharedPrefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    sharedPrefEditor.putString(DOCUMENT_ID, documentId);
                    sharedPrefEditor.apply();
                    Log.d(TAG, "onComplete: document exists and shared preferences changed");
                    itemRefIsSet = true;
                } else {
                    //TODO: send a thing that changes the UI to show that the restaurant hasn't signed up
                }
            }
        });

        return itemRefIsSet;
    }

    /**
     * This method downloads all the restaurants on Firestore to a local DB and finds the distances to all of them.
     * @param mCurrentLocation is the Location object to compare the distance to.
     * @param context is the application context
     */
    public void getLocations(final Location mCurrentLocation, final Context context){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference itemRef = db.collection("restaurants_2");
        itemRef.limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(i);
                    String location_id = documentSnapshot.getId();
                    String name = documentSnapshot.getString("name");
                    String address = documentSnapshot.getString("address");
                    double latitude = documentSnapshot.getGeoPoint("lat_long").getLatitude();
                    double longitude = documentSnapshot.getGeoPoint("lat_long").getLongitude();
                    // Use current location to calculate distance
                    Location POI = new Location("current location");
                    POI.setLatitude(latitude);
                    POI.setLongitude(longitude);
                    double distance = mCurrentLocation.distanceTo(POI);

                    DataHandlingUtils dataHandlingUtils = new DataHandlingUtils();
                    LocationEntry locationEntry = new LocationEntry(location_id, name, address, latitude, longitude, distance);
                    dataHandlingUtils.insertLocationRoom(locationEntry, context);

                    Log.d(TAG, "Doc Id: " + documentSnapshot.getId() + " onEvent: " + name + " lat: " + latitude + " long: " + longitude + " distance " + distance);
                }
            }
        });
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
    public void orderOneItem(FirebaseAuth mAuth, FirebaseFirestore db, DocumentSnapshot documentSnapshot, Context context) {

        Toast.makeText(context, "You ordered " + String.valueOf(documentSnapshot.get("title")), Toast.LENGTH_SHORT).show();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String orderer = currentUser.getDisplayName();

        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("ordered_by", orderer);
        orderInfo.put("order_time", FieldValue.serverTimestamp());
        orderInfo.put("menu_item_ids", Arrays.asList("hello", "you", "are", "gey"));
        // Combine documentSnapshot and orderInfo Maps
        orderInfo.putAll(documentSnapshot.getData());

        db.collection("restaurants")
                .document("aqvUJjyokpta9KyBFz9U")
                .collection("current_orders").document()
                .set(orderInfo);
    }

    /**
     * Orders an Array of items and puts them in a firestore order document
     * @param mAuth
     * @param db
     * @param context
     * @param idArray
     */
    public void orderItems(FirebaseAuth mAuth,
                           final FirebaseFirestore db,
                           Context context,
                           ArrayList idArray) {
        // if there is something in the order array, begin fireStore transaction
        if (!idArray.isEmpty()) {
            Toast.makeText(context, "You ordered " + String.valueOf(idArray.size()) + " items", Toast.LENGTH_LONG).show();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String orderer = currentUser.getDisplayName();

            final Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("ordered_by", orderer);
            orderInfo.put("order_time", FieldValue.serverTimestamp());
            orderInfo.put("menu_item_ids", idArray);
            Log.d(TAG, "orderItems: " + orderInfo.get("menu_item_ids"));

            final String user_id = mAuth.getUid();

            Log.d(TAG, "orderItems: ");
            boolean mExists = checkIfUserExists(user_id, db);

            if (mExists) {
                Log.d(TAG, "orderItems: user exists. Set order info in their path");
                CollectionReference itemRef = buildFirestoreUserReference(user_id, db);
                db.collection(itemRef.getPath()).document()
                        .set(orderInfo);
            } else {
                Log.d(TAG, "orderItems: user did not exist. Create user document and set order info to it.");
                final Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("user_created_at", FieldValue.serverTimestamp());
                db.collection("users_2")
                        .document(user_id)
                        .set(userInfo);
                CollectionReference itemRef = buildFirestoreUserReference(user_id, db);
                db.collection(itemRef.getPath()).document()
                        .set(orderInfo);
            }
        } else {
            Toast.makeText(context, "Your Order Is Empty", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if a user document exists
     */
    private static boolean userExists;
    public boolean checkIfUserExists(String user_id, FirebaseFirestore db) {
        //FIXME: Learn about callback methods to change the userExists variable from within the onComplete method
        // check if document with that user exists
        db.collection("users_2")
                .document(user_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Boolean exists = task.getResult().exists();
                userExists = exists;
            }
        });
        return userExists;
    }

    private static boolean RestaurantExists;
    public boolean checkIfRestaurantExists(String restaurant_id, FirebaseFirestore db) {
        //FIXME: Learn about callback methods to change the userExists variable from within the onComplete method
        // check if document with that user exists
        db.collection("restaurants_2")
                .document(restaurant_id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Boolean exists = task.getResult().exists();
                RestaurantExists = exists;
            }
        });
        return RestaurantExists;
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

        Query mQuery = buildQuery(itemRef, preferences);
        // Make query that satisfies user preferences.
        return mQuery;
    }

    //FIXME: should put all this in a viewmodel
    public void insertItemRoom(final ItemEntry itemEntry, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                roomDb = AppDatabase.getInstance(context);
                roomDb.ItemDao().insertItem(itemEntry);
                return null;
            }
        }.execute();
    }

    public void insertLocationRoom(final LocationEntry locationEntry, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                roomDb = AppDatabase.getInstance(context);
                roomDb.LocationDao().insertLocation(locationEntry);
                return null;
            }
        }.execute();
    }

    public void deleteItemRoom(final ItemEntry itemEntry, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                roomDb = AppDatabase.getInstance(context);
                roomDb.ItemDao().deleteItem(itemEntry);
                return null;
            }
        }.execute();
    }

    public void deleteAllLocationsRoom(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                roomDb = AppDatabase.getInstance(context);
                roomDb.LocationDao().nukeTable();
                return null;
            }
        }.execute();
    }

    public void deleteAllItemsRoom(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                roomDb = AppDatabase.getInstance(context);
                roomDb.ItemDao().nukeTable();
                return null;
            }
        }.execute();
    }

    /**
     * Make a method that takes in the google userId that directs the user to their account to write data.
     */
    public CollectionReference buildFirestoreUserReference(String user_id, FirebaseFirestore db){
        CollectionReference itemRef = db.collection("users_2")
                .document(user_id)
                .collection("order_info");
        return itemRef;
    }
    /**
     * Write another method that takes in the location (lat, long) to make the path to the right restaurant
     */

}
