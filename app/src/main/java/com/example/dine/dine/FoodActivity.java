package com.example.dine.dine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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


public class FoodActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 25;

    // TODO(1): (Completed) Display Firestore data in recyclerviews
    // TODO(1.1): (Completed) Add click handlers
    // TODO(2): Add Location services and use that to switch to different Firestore collections/documents
    // TODO(3, postponed): Add Firebase Cloud Messaging to update the web-client based on location
    // TODO(4, postponed): check for google play services in onCreate and onResume (https://firebase.google.com/docs/cloud-messaging/android/client#sample-play)
    // TODO(5, postponed): Make a class that handles the firebase tokens. Sending to server/when they reset.
    // TODO(6, postponed): Make a way for the client to subscribe to the firebase topic.
    // TODO (7): Show a screen that tells user the if restaurant does not have anything they can eat
    // FIXME: Make sure to follow the permissions best practices

    private String TAG = this.getClass().getName();
    private FirebaseAuth mAuth;
    // Add Firestore Reference
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = db.collection("restaurants")
            .document("aqvUJjyokpta9KyBFz9U")
            .collection("all_items");
    //change the query when preferences are checked
    private Query query;
    private FirestoreRecyclerAdapter mFirestoreAdapter;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private android.support.v7.widget.Toolbar myToolbar;

    // inflates the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Does something when the an item in the overflow menu is touched.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:

                // Open settings activity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        // check user preferences before loading anything
        checkPreferences();
        setUpRecyclerView();

        // Setup toolbar
        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Check for or request for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            Log.d(TAG, "onCreate: permission not granted");

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            Log.d(TAG, "onCreate: permission requested");

        } else {
            // Permission already granted, get location
            Log.d(TAG, "onCreate: permission already granted");
            getLocation();
        }
    }

    /**
     * In check preferences,
     */
    //FIXME: Make a function that generates a query based on location and preferences and have it call this method and getLocation method.
    private void checkPreferences() {

        // ensure settings are initialized with their default values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Read values from shared preferences
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);
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
        query = buildQuery(itemRef, preferences);
    }

    /**
     * this builds a query that satisfies the user preferences
     * @param itemRef is the initial path to the collection
     * @param preferences is a List of booleans that represent the user set preferences
     * @return a Query object that is used to call Firestore.
     */
    public Query buildQuery(Query itemRef, List<Boolean> preferences) {
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
    private static boolean areAllFalse(List<Boolean> array)
    {
        for(boolean b : array) if(b) return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    /**
     * calls PlacesAPI's getCurrentLocation and returns a list of nearby places. Updates the UI with the more accurate one
     */
    private void getLocation() {
        // Temporary location tag
        //final TextView location_tv = findViewById(R.id.current_location);

        @SuppressLint("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                String name = likelyPlaces.get(0).getPlace().getName().toString();
                float probability = likelyPlaces.get(0).getLikelihood();
                com.google.android.gms.maps.model.LatLng coordinates = likelyPlaces.get(0).getPlace().getLatLng();
                //Toast.makeText(getApplicationContext(), String.valueOf(coordinates), Toast.LENGTH_LONG).show();
//                location_tv.setText(name);
//                location_tv.setVisibility(View.GONE);
                myToolbar.setTitle(name);
                Log.d(TAG, "onComplete: ");
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    Log.i(TAG, String.format("Place '%s' has likelihood: %g coordinates: %s",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood(),
                            String.valueOf(coordinates)
                    ));
                }
                likelyPlaces.release();
            }
        });
    }

    /**
     * sets up the recyclerview.
     */
    private void setUpRecyclerView() {
        // Create a query when requesting data from firestore
        // FIXME: figure out compound orders orderBy("price", Query.Direction.DESCENDING)
        // TODO: use preferences to determine what items to show whereArrayContains("info", "vegan");
        FirestoreRecyclerOptions<Item> options =  new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();
        mFirestoreAdapter = new FirestoreItemAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.rv_show_menu_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mFirestoreAdapter);


        // Set itemtouch helper to recycler view
        //Add the swiping cards functionality using the simple callback
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
                // do not allow moving
            }

            /**
             * When swiped left, it will delete the item from the database
             * FIXME: instead of deleting from the database, it will move the item to another collection where all the disliked items are held
             * @param viewHolder
             * @param direction
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Do something when the item is swiped
                // Right now it will show a toast saying which direction you swiped.
                String mDirection;
                int position = viewHolder.getAdapterPosition();
                if (direction==ItemTouchHelper.RIGHT) {
                    mDirection = "right";
                } else {
                    mDirection = "left";
                    // Do not delete the item.
                    //((FirestoreItemAdapter) mFirestoreAdapter).deleteItem(position);
                }

                Toast.makeText(getApplicationContext(), "You swiped " + mDirection + "on card " + String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);


        /**
         * The onItemClick function does something when it is clicked. It is an interface from FirestoreItemAdapter.onItemClickListener()
         * that must be overridden.
         * The one below hides and shows the order item button.
         */
        ((FirestoreItemAdapter) mFirestoreAdapter).setOnItemClickListener(new FirestoreItemAdapter.onItemClickListener() {
            @Override
            public void onItemClick(final DocumentSnapshot documentSnapshot, int position, View itemView) {
                // TODO: watch firestore tutorials to see what you can do with document snapshots
                String id = documentSnapshot.getId();
                //Toast.makeText(FoodActivity.this, "Position " + String.valueOf(position) + " ID: " + id, Toast.LENGTH_LONG).show();

//                // Add ability to hide and show order button depending on if the user has clicked the particular item.
//                final Button orderButton = itemView.findViewById(R.id.order_dish);
//                // Remove ordering icon
//                //final ImageView dropDown = itemView.findViewById(R.id.expand_card);
//                int visibility = orderButton.getVisibility();
//                if (visibility == View.GONE) {
//                    orderButton.setVisibility(View.VISIBLE);
//                    //dropDown.setImageResource(R.drawable.ic_arrow_drop_up_pink_24dp);
//                } else {
//                    orderButton.setVisibility(View.GONE);
//                    //dropDown.setImageResource(R.drawable.ic_arrow_drop_down_circle_black_24dp);
//                }

                /**
                 * When the button is clicked, call the function to move the data that was clicked on to the "current_orders" part of the
                 * database.
                 */
//                orderButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        moveItemToCurrentOrders(documentSnapshot, getApplicationContext());
//                        orderButton.setVisibility(View.GONE);
//                        //dropDown.setImageResource(R.drawable.ic_arrow_drop_down_circle_black_24dp);
//                    }
//                });

                //Make a new intent and pass this document ID into it as a string Extra
                Intent detailIntent = new Intent(getApplicationContext(), ItemDetailsActivity.class);
                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_DOCUMENT_ID, documentSnapshot.getId());
                startActivity(detailIntent);

            }
        });
    }


    /**
     * Makes a copy of the menu items that was clicked. Attaches timestamp and name of person who ordered it.
     * Sets this new HashMap to the "current_orders" path.
     * FIXME: instead of using Firestore RTDB, learn how to use FCM.
     * @param documentSnapshot is the Firestore document snapshot passed from the order.
     */
    public void moveItemToCurrentOrders(DocumentSnapshot documentSnapshot, Context context) {

        Toast.makeText(context, "You ordered " + String.valueOf(documentSnapshot.get("title")), Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
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

    @Override
    protected void onStart() {
        super.onStart();
        // Check for changes in shared preferences
        // FIXME: only check preference if they changed
        checkPreferences();
        setUpRecyclerView();
        mFirestoreAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirestoreAdapter.stopListening();
    }
}
