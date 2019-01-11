package com.example.dine.dine.uiFragments;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dine.dine.Constants;
import com.example.dine.dine.DataHandlingUtils;
import com.example.dine.dine.Item;
import com.example.dine.dine.ItemDetailsActivity;
import com.example.dine.dine.LocationUtils;
import com.example.dine.dine.OrderSummaryActivity;
import com.example.dine.dine.R;
import com.example.dine.dine.RoomDb.AppDatabase;
import com.example.dine.dine.RoomDb.LocationEntry;
import com.example.dine.dine.uiDrawers.FirestoreItemAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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

public class RecommendationFragment extends android.support.v4.app.Fragment implements LocationUtils.LocationUpdateListener {
    public RecommendationFragment(){}

    /**
     * Variables
     */
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 25;
    private String TAG = this.getClass().getName();
    private FirebaseAuth mAuth;
    // Add Firestore Reference
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter mFirestoreAdapter;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private AppDatabase roomDb;
    private static String mRestaurantDocumentId;
    public LifecycleOwner lifecycleOwner = this;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    //Location stuff
    private static Location mCurrentLocation;
    // This is just the default path. Will show nothing on the RV
    private CollectionReference itemRef = db.collection(" ");
    private View rootView;
    // Interface
    MainActivityInterface mCallBack;

    /**
     * Interfaces
     */

    public interface MainActivityInterface{
        void toolBarSetup(String toolbarTitle);
    }

    /**
     * Lifecycle Callbacks
     */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure the host activity has implemented the callback interface. If not, it throws an exception
        try {
            mCallBack = (MainActivityInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement toolBarSetup");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_food, container, false);
        roomDb = AppDatabase.getInstance(getContext());
        DataHandlingUtils.makePrefQuery(getContext(), itemRef);
        mAuth = FirebaseAuth.getInstance();

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getContext(), null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getContext(), null);

        // Get bundle data. Contains new locationId. Change location
        if (getArguments()!=null){
            // if the locations permission was granted, proceed to get the current location.
            // If not, use the location specified by the database.
            if (getArguments().getInt(Constants.TAG_ACCESS_FINE_LOCATION_PERMISSION_GRANTED)
                    == Constants.CODE_ACCESS_FINE_LOCATION_PERMISSION_GRANTED) {
                getLocation();
            } else {
                int roomId = getArguments().getInt(Constants.ON_LOCATION_CLICKED_ROOM_ID);
                setItemRef(roomId);
                Log.d(TAG, "onCreateView: " + itemRef.getPath());
            }
        }

//        // Check for or request for permissions
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            // Should we show an explanation?
//            Log.d(TAG, "onCreate: permission not granted");
//
//            // No explanation needed; request the permission
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
//            Log.d(TAG, "onCreate: permission requested");
//
//        } else {
//            // Permission already granted, get location
//            Log.d(TAG, "onCreate: permission already granted");
//            getLocation();
//        }

        FloatingActionButton mFloatingActionButton;
        mFloatingActionButton = rootView.findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OrderSummaryActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
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
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: woot");
        DataHandlingUtils.makePrefQuery(getContext(), itemRef);
        //setUpRecyclerView(rootView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFirestoreAdapter.stopListening();
    }

    /**
     * Non-Android Callbacks
     */

    // essentially a callback from LocationUtils
    @Override
    public void onLocationUpdate(Location location) {
        mCurrentLocation = location;
    }

    /**
     * Methods
     */

    public void showNoRestaurants() {
        //TODO: modify the UI to display that the selected restaurant is not signed up for Dine
        mCallBack.toolBarSetup("Restaurant Not Available");
    }

    /**
     * todo: FIRESTORE COST: R = 1, W = 0, D = 0
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
                                    mCallBack.toolBarSetup(name);
                                    // Update the RV
                                    setUpRecyclerView(rootView);
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

    /**
     * todo: FIRESTORE COST: R = 0:n, W = 0, D = 0
     * sets up the recyclerview.
     */
    private void setUpRecyclerView(View rootView) {
        // Create a query when requesting data from firestore
        Log.d(TAG, "setupRv: " + itemRef.getPath());
        Query query = DataHandlingUtils.makePrefQuery(getContext(), itemRef);
        Log.d(TAG, "setUpRecyclerView: " + itemRef.getPath());
        FirestoreRecyclerOptions<Item> options =  new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();
        mFirestoreAdapter = new FirestoreItemAdapter(options);
        RecyclerView recyclerView = rootView.findViewById(R.id.rv_show_menu_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
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

                Toast.makeText(getContext(), "You swiped " + mDirection + "on card " + String.valueOf(position), Toast.LENGTH_SHORT).show();
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
                Intent detailIntent = new Intent(getContext(), ItemDetailsActivity.class);
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

    /**
     * calls PlacesAPI's getCurrentLocation and returns a list of nearby places. Updates the UI with the more accurate one
     * Temporarily inserts the locations into the ROOM database
     * todo: FIRESTORE COST: R = 1, W = , D =
     */
    private void getLocation() {

        //TODO: once the nearest place has been determined, set the sharedPreference string to that location's ID
        // This sets up an update that gets a new location every time the user moves more than a specified distance
        LocationUtils locationUtils = new LocationUtils();
        locationUtils.init(getContext(), this);
        locationUtils.getCoordinates(getContext());

        @SuppressLint("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {

                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                DataHandlingUtils dataHandlingUtils = new DataHandlingUtils();
                dataHandlingUtils.deleteAllLocationsRoom(getContext());
                dataHandlingUtils.getLocations(mCurrentLocation, getContext());
                dataHandlingUtils.checkMyReward(mAuth.getUid(), db);


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
}

