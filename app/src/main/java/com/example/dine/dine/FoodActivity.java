package com.example.dine.dine;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class FoodActivity extends AppCompatActivity implements ItemAdapter.ItemAdapterOnClickHandler {

    // TODO(1): Display Firestore data in recyclerviews
    // TODO(2): Add Location services and use that to switch to different Firestore collections/documents
    // TODO(3): Add Firebase Cloud Messaging to update the web-client based on location

    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Toast mToast;

    // Add Firestore Reference
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = db.collection("items");
    private FirestoreRecyclerAdapter mFirestoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        setUpRecyclerView();
//        mRecyclerView = findViewById(R.id.rv_show_menu_items);
//
//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        mRecyclerView.setHasFixedSize(true);
//
//        // use a linear layout manager
//        mLayoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//
//        // specify an adapter (see also next example)
//        //FIXME: make an adapter
//        mAdapter = new ItemAdapter(this);
//        mRecyclerView.setAdapter(mAdapter);
    }

    private void setUpRecyclerView() {
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
                }

                Toast.makeText(getApplicationContext(), "You swiped " + mDirection + "on card " + String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
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

    /**
     * Is an implementation of the onClick method in ItemAdapterOnClickHandler
     * Method will display a toast
     * @param testItem
     */
    @Override
    public void onClick(String testItem) {
        Context context = this;
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, testItem, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
