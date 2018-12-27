package com.example.dine.dine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.example.dine.dine.uiDrawers.FirestoreItemAdapter;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


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
    private FirestoreRecyclerAdapter mFirestoreAdapter;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private android.support.v7.widget.Toolbar myToolbar;
    private AppDatabase roomDb;

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
        DataHandlingUtils.makePrefQuery(this, itemRef);
        setUpRecyclerView();

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
                //setupViewModel();
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
        Query query = DataHandlingUtils.makePrefQuery(this, itemRef);
        FirestoreRecyclerOptions<Item> options =  new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();
        mFirestoreAdapter = new FirestoreItemAdapter(options, FirestoreItemAdapter.MENU_ORDERING_STYLE);
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
        mFirestoreAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirestoreAdapter.stopListening();
    }
}
