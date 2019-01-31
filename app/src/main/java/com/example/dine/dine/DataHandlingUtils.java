package com.example.dine.dine;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.dine.dine.RoomDb.AppDatabase;
import com.example.dine.dine.RoomDb.ItemEntry;
import com.example.dine.dine.RoomDb.LocationEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
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

    /**
     * todo: FIRESTORE COST: R = 0:10, W = 0, D = 0
     * FIXME: Make a method that calculates the max and min lat, longs and queries Firestore using that to return only restaurants within a predetermined radius.
     * This method downloads all the restaurants on Firestore to a local DB and finds the distances to all of them.
     * @param mCurrentLocation is the Location object to compare the distance to.
     * @param context is the application context
     */
    public void getLocations(final Location mCurrentLocation, final Context context){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference itemRef = db.collection(context.getString(R.string.PATH_RESTAURANT));
        itemRef.limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(i);
                    String location_id = documentSnapshot.getId();
                    String name = documentSnapshot.getString(Constants.FIELD_RESTAURANT_NAME);
                    String address = documentSnapshot.getString(Constants.FIELD_ADDRESS);
                    GeoPoint mGeoPoint = (GeoPoint) documentSnapshot.get(Constants.FIELD_LOCATION_LAT_LONG);
                    double latitude = mGeoPoint.getLatitude();
                    double longitude = mGeoPoint.getLongitude();
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
     * FIXME: change the query to work with the updated chipsDietRestriction data
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
                newQuery = newQuery.whereArrayContains(Constants.FIELD_CHIPS_DIET_RESTRICTION, Constants.VALUE_GLUTEN_FREE);
                Log.d(TAG, "buildQuery: gluten_count is " + String.valueOf(gluten_free_count));

            } else if (vegan_count && preferences.get(1)) {
                vegan_count = false;
                newQuery = newQuery.whereArrayContains(Constants.FIELD_CHIPS_DIET_RESTRICTION, Constants.VALUE_VEGAN);
                Log.d(TAG, "buildQuery: vegan_count is " + String.valueOf(vegan_count));

            } else if (vegetarian_count && preferences.get(2)) {
                vegetarian_count = false;
                newQuery = newQuery.whereArrayContains(Constants.FIELD_CHIPS_DIET_RESTRICTION, Constants.VALUE_VEGETARIAN);
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
     * todo: FIRESTORE COST: R = 0, W = 1, D = 0
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
     * todo: FIRESTORE COST: R = 1, W = 1:2, D = 0
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
        final String user_id = mAuth.getUid();
        if (!idArray.isEmpty()) {
            Toast.makeText(context, "You ordered " + String.valueOf(idArray.size()) + " items", Toast.LENGTH_LONG).show();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String orderer = currentUser.getDisplayName();

            final Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put(Constants.FIELD_ORDERED_BY, orderer);
            orderInfo.put(Constants.FIELD_ORDER_TIME, FieldValue.serverTimestamp());
            orderInfo.put(Constants.FIELD_MENU_ITEM_IDS, Arrays.asList(idArray.toArray()));

            db.collection(Constants.PATH_USERS)
                    .document(user_id)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Boolean exists = task.getResult().exists();
                    if (exists) {
                        Log.d(TAG, "orderItems: user exists. Set order info in their path");
                        CollectionReference itemRef = buildFirestoreUserReference(user_id, db).collection(Constants.PATH_ORDER_INFO);
                        db.collection(itemRef.getPath()).document()
                                .set(orderInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Yay! successfully added order information to the users path!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Failed to save order information to path :(");
                                e.printStackTrace();
                            }
                        });
                        Log.d(TAG, "onComplete: orderitems" + orderInfo.toString());
                    } else {
                        Log.d(TAG, "orderItems: user did not exist. Create user document and set order info to it.");
                        final Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("user_created_at", FieldValue.serverTimestamp());
                        db.collection(Constants.PATH_USERS)
                                .document(user_id)
                                .set(userInfo);
                        CollectionReference itemRef = buildFirestoreUserReference(user_id, db).collection(Constants.PATH_ORDER_INFO);
                        db.collection(itemRef.getPath()).document()
                                .set(orderInfo);
                    }
                }
            });

            //TODO: add points to the points field
            // check if document with that user exists
            DocumentReference userReference = buildFirestoreUserReference(user_id, db);
            db.document(userReference.getPath()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.getResult().exists()) {
                        try{
                            // get the number of points and use getMyReward() method to set appropriate award in user's collection.
                            long numPoints = task.getResult().getLong("points");
                            long newPoints = numPoints + 100;
                            DocumentReference itemRef = buildFirestoreUserReference(user_id, db);
                            db.document((itemRef.getPath())).update("points", newPoints);
                            Log.d(TAG, "onComplete: 100 points added");
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onComplete: could not update the number of points", e);
                        }
                    } else {
                        Log.d(TAG, "onComplete: user with " + user_id + " does not exist.");
                    }
                }
            });

        } else {
            Toast.makeText(context, "Your Order Is Empty", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Checks the user_id then passes the number of points into the getMyReward() method.
     * todo: FIRESTORE COST: R = 2, W = 1, D = 0
     * @param user_id
     * @param db
     */
    public void checkMyReward(final String user_id, final FirebaseFirestore db) {
        //FIXME: Learn about callback methods to change the userExists variable from within the onComplete method
        //TODO: get the threshold from rewards field
        // check if document with that user exists
        DocumentReference userReference = buildFirestoreUserReference(user_id, db);
        db.document(userReference.getPath()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()) {
                    try{
                        // get the number of points and use getMyReward() method to set appropriate award in user's collection.
                        long numPoints = task.getResult().getLong("points");
                        getMyReward(numPoints, user_id, db);
                        Log.d(TAG, "onComplete: num points: " + numPoints);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onComplete: could not retrieve the number of points", e);
                    }
                } else {
                    Log.d(TAG, "onComplete: user with " + user_id + " does not exist.");
                }
            }
        });
    }

    /**
     * getMyReward calls Firestore to move a reward from the "reward" collection to the "rewards" collection under a user document
     * method evaluates the number of points to determine which rank the user is in and what collection to read from.
     * todo: FIRESTORE COST: R = 1, W = 1, D = 0
     * @param user_id the google ID of the user
     * @param db instance of a Firestore Database
     */
    public static DocumentSnapshot discountDocumentSnapshot;
    public void getMyReward(final long mNumPoints, final String user_id, final FirebaseFirestore db) {
        // 1. Check how many points the person has.
        Log.d(TAG, "getMyReward: " + mNumPoints);
        // 2. for a n number of points, retrieve the reward from the right rank
        // 3. delete n number of points
        if (mNumPoints==1000) {
            // Retrieve a random discount from Firestore
            final CollectionReference rewardsRef = db.collection("rewards");
            rewardsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    int Min = 0;
                    int Max = task.getResult().getDocuments().size();
                    int randomNum = Min + (int)(Math.random() * (Max - Min));
                    discountDocumentSnapshot = task.getResult().getDocuments().get(randomNum);
                    String reward_id = discountDocumentSnapshot.getId();
                    if (discountDocumentSnapshot != null) {

                        Log.d(TAG, "onComplete: " + discountDocumentSnapshot.get("title") + " randomNum: " + randomNum);
                        // create a reference to the "rewards" collection under the user's path
                        CollectionReference itemRef = buildFirestoreUserReference(user_id, db).collection("rewards");
                        // get a DocumentReference to the reward document
                        DocumentReference rewardReference = rewardsRef.document(reward_id);
                        // save the DocumentReference to the user's "rewards" collection as a new document.
                        // set the validity of that reward to true.
                        Map<String, Object> discountInfo = new HashMap<>();
                        discountInfo.put("validity", true);
                        discountInfo.put("reward_ref", rewardReference);
                        db.collection(itemRef.getPath()).document().set(discountInfo);
                    }
                }
            });
            Log.d(TAG, "onComplete: you have a discount");
        } else {
            double i = 10 - mNumPoints%10;
            Log.d(TAG, "onComplete: you have " + i + " more orders until your next discount");
        }
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

        Query mQuery = buildQuery(itemRef, preferences).orderBy("promo", Query.Direction.DESCENDING);
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

    public void updateOneItemsRoom(final Context context, final ItemEntry itemEntry) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                roomDb = AppDatabase.getInstance(context);
                roomDb.ItemDao().updateitem(itemEntry);
                return null;
            }
        }.execute();
    }

    public void updateOrderedItemsRoom(final Context context) {
        // TODO: update the progress field to indicate that the restaurant is preparing the dish.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                roomDb = AppDatabase.getInstance(context);
                LiveData<List<ItemEntry>> itemEntries = roomDb.ItemDao().loadAllItems();
                itemEntries.observe((LifecycleOwner) context, new Observer<List<ItemEntry>>() {
                    @Override
                    public void onChanged(@Nullable List<ItemEntry> itemEntries) {
                        if (!itemEntries.isEmpty()) {
                            for (int i = 0; i<itemEntries.size(); i++) {
                                int progress = itemEntries.get(i).getProgress();
                                if (progress == Constants.ITEM_ENTRY_PROGRESS_NOT_ORDERED) {
                                    int id = itemEntries.get(i).getId();
                                    String item_id = itemEntries.get(i).getItemId();
                                    String title = itemEntries.get(i).getTitle();
                                    String description = itemEntries.get(i).getDescription();
                                    int price = itemEntries.get(i).getPrice();
                                    int newProgress = Constants.ITEM_ENTRY_PROGRESS_COOKING;
                                    ItemEntry itemEntry = new ItemEntry(id, item_id, title, description, price, newProgress);
                                    updateOneItemsRoom(context, itemEntry);
                                    Log.d(TAG, "onChanged: " + title + " progress: " + newProgress);
                                }
                            }
                        }
                    }
                });
                return null;
            }
        }.execute();
    }

    /**
     * Make a method that takes in the google userId that directs the user to their account to write data.
     */
    public DocumentReference buildFirestoreUserReference(String user_id, FirebaseFirestore db){
        DocumentReference itemRef = db.collection("users_2")
                .document(user_id);
        return itemRef;
    }
    /**
     * Write another method that takes in the location (lat, long) to make the path to the right restaurant
     */

}
