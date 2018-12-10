package com.example.dine.dine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;


public class FoodActivity extends AppCompatActivity {

    // TODO(1): (Completed) Display Firestore data in recyclerviews
    // TODO(1.1): (Completed) Add click handlers
    // TODO(2): Add Location services and use that to switch to different Firestore collections/documents
    // TODO(3, postponed): Add Firebase Cloud Messaging to update the web-client based on location
    // TODO(4, postponed): check for google play services in onCreate and onResume (https://firebase.google.com/docs/cloud-messaging/android/client#sample-play)
    // TODO(5, postponed): Make a class that handles the firebase tokens. Sending to server/when they reset.
    // TODO(6, postponed): Make a way for the client to subscribe to the firebase topic.

    private String TAG = this.getClass().getName();
    private FirebaseAuth mAuth;
    // Add Firestore Reference
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = db.collection("restaurants")
            .document("aqvUJjyokpta9KyBFz9U")
            .collection("all_items");
    private FirestoreRecyclerAdapter mFirestoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        setUpRecyclerView();

        /**
         * Comment out FCM test code
         */
//        // Get Firebase token
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w("Token", "getInstanceId failed", task.getException());
//                            return;
//                        }
//                        // Get new Instance ID token
//                        String token = task.getResult().getToken();
//                        // Log token
//                        Log.d("Token", token);
//                    }
//                });
//
//        // Subscribe to a test topic
//        /**
//         * Currently, the following FirebaseMessaging function subscribes to the topic "weather".
//         * It logs "subcribed" if it does so and "failed to subscribe" if it doesn't.
//         * If the topic does not exist, it will create a new topic on the firebase project
//         * Call unsubscribeFromTopic() with the topic name to unsubscribe
//         */
//        FirebaseMessaging.getInstance().subscribeToTopic("weather")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        String msg = "subscribed";
//                        if (!task.isSuccessful()) {
//                            msg = "failed to subscribe";
//                        }
//                        Log.d(TAG, msg);
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    /**
     * sets up the recyclerview.
     */
    private void setUpRecyclerView() {
        // Create a query when requesting data from firestore
        Query query = itemRef.orderBy("price", Query.Direction.DESCENDING);
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

                // Add ability to hide and show order button depending on if the user has clicked the particular item.
                final Button orderButton = itemView.findViewById(R.id.order_dish);
                final ImageView dropDown = itemView.findViewById(R.id.expand_card);
                int visibility = orderButton.getVisibility();
                if (visibility == View.GONE) {
                    orderButton.setVisibility(View.VISIBLE);
                    dropDown.setImageResource(R.drawable.ic_arrow_drop_up_pink_24dp);
                } else {
                    orderButton.setVisibility(View.GONE);
                    dropDown.setImageResource(R.drawable.ic_arrow_drop_down_circle_black_24dp);
                }

                /**
                 * When the button is clicked, call the function to move the data that was clicked on to the "current_orders" part of the
                 * database.
                 */
                orderButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moveItemToCurrentOrders(documentSnapshot);
                        orderButton.setVisibility(View.GONE);
                        dropDown.setImageResource(R.drawable.ic_arrow_drop_down_circle_black_24dp);
                        Toast.makeText(getApplicationContext(), "You ordered " + String.valueOf(documentSnapshot.get("title")), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }


    /**
     * Makes a copy of the menu items that was clicked. Attaches timestamp and name of person who ordered it.
     * Sets this new HashMap to the "current_orders" path.
     * FIXME: instead of using Firestore RTDB, learn how to use FCM.
     * @param documentSnapshot is the Firestore document snapshot passed from the order.
     */
    private void moveItemToCurrentOrders(DocumentSnapshot documentSnapshot) {

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
        mFirestoreAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirestoreAdapter.stopListening();
    }
}
