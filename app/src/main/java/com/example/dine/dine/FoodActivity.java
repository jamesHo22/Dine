package com.example.dine.dine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.dine.dine.RoomDb.AddItemViewModel;
import com.example.dine.dine.RoomDb.AddItemViewModelFactory;
import com.example.dine.dine.RoomDb.AppDatabase;
import com.example.dine.dine.RoomDb.ItemEntry;
import com.example.dine.dine.RoomDb.LocationEntry;
import com.example.dine.dine.uiDrawers.FirestoreItemAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class FoodActivity extends AppCompatActivity implements LocationListener,
        BottomSheetDialogue.BottomSheetListener,
        LocationUtils.LocationUpdateListener {
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

    /**
     * The following path is for the updated DB
     */
//    private CollectionReference itemRef = db.collection("restaurants_2")
//            .document("TZs7LD60OiZFHGu6CWz1")
//            .collection("menu_items");

    private FirestoreRecyclerAdapter mFirestoreAdapter;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private android.support.v7.widget.Toolbar myToolbar;
    private AppDatabase roomDb;
    private static String mRestaurantDocumentId;
    public LifecycleOwner lifecycleOwner = this;

    //Location stuff
    private static Location mCurrentLocation;

    private CollectionReference itemRef = db.collection("restaurants")
            .document("aqvUJjyokpta9KyBFz9U")
                        .collection("all_items");

//    /**
//     * This method checks if the document exists and sets the itemRef variable. It also sets up the recyclerview after
//     * itemRef is assigned a value
//     * @param db
//     * @return
//     */
//    public void getItemRef(final FirebaseFirestore db) {
//
//            Log.d(TAG, "getItemRef: method called");
//            SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
//            final String documentId = prefs.getString(DataHandlingUtils.DOCUMENT_ID, "Id Not Set");
//            if (!documentId.equals("Id Not Set")) {
//                // Check if the document exists. If it does, set the itemRef to that document
//                db.collection("restaurants_2")
//                        .document(documentId)
//                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        Boolean exists = task.getResult().exists();
//                        if (exists) {
//                            itemRef = db.collection("restaurants_2")
//                                    .document(documentId)
//                                    .collection("menu_items");
//
//                            Log.d(TAG, "onComplete: document exists: " + itemRef + "documentID: " + documentId);
//                        } else {
//                            //TODO: send a thing that changes the UI to show that the restaurant hasn't signed up
//                            itemRef = db.collection("restaurants")
//                                    .document("aqvUJjyokpta9KyBFz9U")
//                                    .collection("all_items");
//                            Log.d(TAG, "onComplete: document no exist");
//                        }
//                        setUpRecyclerView();
//                        mFirestoreAdapter.startListening();
//                    }
//                });
//            } else {
//                Log.d(TAG, "getItemRef: " + documentId);
//                itemRef = db.collection("restaurants")
//                        .document("aqvUJjyokpta9KyBFz9U")
//                        .collection("all_items");
//            }
//    }

    public void showNoRestaurants() {
        //TODO: modify the UI to display that the selected restaurant is not signed up for Dine
        myToolbar.setTitle("Restaurant Not Available");
    }

    /**
     * This method takes in a integer ID for an object in the database and sets the path of itemRef to that document_id
     * @param roomLocationId
     */
    public void setItemRef(final int roomLocationId) {
        LiveData<LocationEntry> locationEntry = roomDb.LocationDao().loadLocationById(roomLocationId);
        locationEntry.observe(this, new Observer<LocationEntry>() {
            @Override
            public void onChanged(@Nullable final LocationEntry locationEntry) {
                if (locationEntry!= null) {
                    final String documentId = locationEntry.getLocation_id();
                    mRestaurantDocumentId = documentId;
                    //if the documentId exists, check if the firestore document with the same ID exists
                    if (documentId!=null) {
                        // Document ID exists, check Firestore
                        db.collection("restaurants_2")
                                .document(documentId)
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                Boolean exists = task.getResult().exists();
                                if (exists) {
                                    // Firestore doc exists: set itemRef to that document and change the toolbar name
                                    itemRef = db.collection("restaurants_2")
                                            .document(documentId)
                                            .collection("menu_items");
                                    Log.d(TAG, "setItemRef: " + itemRef.getPath() + "documentID: " + documentId);
                                    String name = locationEntry.getName();
                                    myToolbar.setTitle(name);
                                    // Update the RV
                                    setUpRecyclerView();
                                } else {
                                    // Firestore doc does not exist, let the user know
                                    showNoRestaurants();
                                }
                            }
                        });
                    } else {
                        // Document doesn't exist: log it and tell user
                        showNoRestaurants();
                        Log.d(TAG, "onChanged: documentId does not exist");
                    }
                }
            }
        });
    }
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
            case R.id.change_location:
                BottomSheetDialogue bottomSheetDialogue = new BottomSheetDialogue();
                bottomSheetDialogue.show(getSupportFragmentManager(), "example_bottom_sheet");

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Must overide this method for dialog fragment bottom sheet to communicate to this Activity


    /**
     * Implement this method to get information from the bottomSheet
     */
    @Override
    public void onLocationClicked(String locationId, int roomId) {
        //TODO: Change the path to the locationID
        setItemRef(roomId);
        DataHandlingUtils dataHandlingUtils = new DataHandlingUtils();
        dataHandlingUtils.deleteAllItemsRoom(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        roomDb = AppDatabase.getInstance(this);
        DataHandlingUtils.makePrefQuery(this, itemRef);


        // Setup toolbar
        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        FloatingActionButton mFloatingActionButton;
        mFloatingActionButton = findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OrderSummaryActivity.class);
                startActivity(intent);
            }
        });

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
     * Accesses the roomDatabase through a viewModel. Only observes one item.
     */
    private void setupViewModel() {
        roomDb = AppDatabase.getInstance(this);
        AddItemViewModelFactory factory = new AddItemViewModelFactory(roomDb, 1);

        AddItemViewModel viewModel = ViewModelProviders.of(this, factory).get(AddItemViewModel.class);
        viewModel.getItem().observe(this, new Observer<ItemEntry>() {
            @Override
            public void onChanged(@Nullable ItemEntry itemEntry) {
                try {
                    String message = itemEntry.getItemId();
                    Log.d(TAG, "onClick: Updating live from ViewModel " + message);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: nothing in the database");
                }
            }
        });
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

    // essentially a callback from LocationUtils
    @Override
    public void onLocationUpdate(Location location) {
        mCurrentLocation = location;
    }

    /**
     * calls PlacesAPI's getCurrentLocation and returns a list of nearby places. Updates the UI with the more accurate one
     * Temporarily inserts the locations into the ROOM database
     */
    private void getLocation() {

        //TODO: once the nearest place has been determined, set the sharedPreference string to that location's ID
        // This sets up an update that gets a new location every time the user moves more than a specified distance
        LocationUtils locationUtils = new LocationUtils();
        locationUtils.init(getApplicationContext(), this);
        locationUtils.getCoordinates(this);

        @SuppressLint("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {

                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                DataHandlingUtils dataHandlingUtils = new DataHandlingUtils();
                dataHandlingUtils.deleteAllLocationsRoom(getApplicationContext());
                dataHandlingUtils.getLocations(mCurrentLocation, getApplicationContext());

//                for (int i = 0; i<likelyPlaces.getCount()/2; i++) {
//
//                    // Get all the variables from data source
//                    String location_id = likelyPlaces.get(i).getPlace().getId();
//                    String location_name = likelyPlaces.get(i).getPlace().getName().toString();
//                    String address = likelyPlaces.get(i).getPlace().getAddress().toString();
//                    double latitude = likelyPlaces.get(i).getPlace().getLatLng().latitude;
//                    double longitude = likelyPlaces.get(i).getPlace().getLatLng().longitude;
//                    Location POI = new Location("current location");
//                    POI.setLatitude(latitude);
//                    POI.setLongitude(longitude);
//                    double distance = mCurrentLocation.distanceTo(POI);
//
//                    Log.i(TAG, String.format("Location_id '%s' Place '%s' has likelihood: '%g' address: '%s' lat: '%g' long: '%g' distance: '%g'",
//                            likelyPlaces.get(i).getPlace().getId(),
//                            likelyPlaces.get(i).getPlace().getName(),
//                            likelyPlaces.get(i).getLikelihood(),
//                            likelyPlaces.get(i).getPlace().getAddress(),
//                            likelyPlaces.get(i).getPlace().getLatLng().latitude,
//                            likelyPlaces.get(i).getPlace().getLatLng().longitude,
//                            distance
//                            ));
//
//                    LocationEntry locationEntry = new LocationEntry(location_id, location_name, address, latitude, longitude, distance);
//                    dataHandlingUtils.insertLocationRoom(locationEntry, getApplicationContext());
//                }
                likelyPlaces.release();

                LiveData<LocationEntry> location = roomDb.LocationDao().loadNearestLocation();
                location.observe(lifecycleOwner, new Observer<LocationEntry>() {
                    @Override
                    public void onChanged(@Nullable LocationEntry locationEntry) {
                        if (locationEntry!=null) {
                            int roomRestaurantId = locationEntry.getId();
                            Log.d(TAG, "onChanged: " + roomRestaurantId);
                            setItemRef(roomRestaurantId);
                        }
                    }
                });
            }
        });
    }

    /**
     * Does something when the users location changes
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: location has changed");
        Log.d(TAG, "onLocationChanged: Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
    }

    /**
     * sets up the recyclerview.
     */
    private void setUpRecyclerView() {
        // Create a query when requesting data from firestore
        Query query = DataHandlingUtils.makePrefQuery(this, itemRef);
        Log.d(TAG, "setUpRecyclerView: " + itemRef.getPath());
        FirestoreRecyclerOptions<Item> options =  new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();
        mFirestoreAdapter = new FirestoreItemAdapter(options, FirestoreItemAdapter.MENU_ORDERING_STYLE);
        RecyclerView recyclerView = findViewById(R.id.rv_show_menu_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mFirestoreAdapter);
        mFirestoreAdapter.startListening();


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

                // Get values of objects in document snapshot
                String document_id = documentSnapshot.getId();
                String title = documentSnapshot.get("title").toString();
                String description = documentSnapshot.get("description").toString();
                int price = Integer.valueOf(documentSnapshot.get("price").toString());

                //Make a new intent and pass this document ID into it as a string Extra
                Intent detailIntent = new Intent(getApplicationContext(), ItemDetailsActivity.class);
                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_DOCUMENT_ID, document_id);
                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_DETAILED_TITLE, title);
                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_DETAILED_DESCRIPTION, description);
                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_DETAILED_PRICE, price);
                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_LOCATION_ID, mRestaurantDocumentId);
                Log.d(TAG, "onItemClick: " + mRestaurantDocumentId);
                startActivity(detailIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for changes in shared preferences
        // FIXME: only check preference if they changed
        DataHandlingUtils.makePrefQuery(this, itemRef);
        setUpRecyclerView();
        //mFirestoreAdapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirestoreAdapter.stopListening();
    }
}
