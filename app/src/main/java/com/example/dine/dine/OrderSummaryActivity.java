package com.example.dine.dine;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.dine.dine.RoomDb.ItemEntry;
import com.example.dine.dine.RoomDb.MainViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OrderSummaryActivity extends AppCompatActivity {

    //TODO (1 Complete): get the document IDs from the MainViewModel
    //TODO (2): make a query to FireStore to get only those documents
    //TODO (3): display those items in a recyclerView

    private final static String TAG = OrderSummaryActivity.class.getSimpleName();
    private FirestoreRecyclerAdapter mFirestoreAdapter;
    // Add Firestore Reference
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemRef = db.collection("restaurants")
            .document("aqvUJjyokpta9KyBFz9U")
            .collection("all_items");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);
        //final TextView mTestTv = findViewById(R.id.test_tv);
        final ArrayList<String> docIDs = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();

        setUpRecyclerView();

        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getItems().observe(this, new Observer<List<ItemEntry>>() {
            @Override
            public void onChanged(@Nullable List<ItemEntry> itemEntries) {
                String testString = "";
                for (int i = 0; i < itemEntries.size(); i ++) {
                    docIDs.add(itemEntries.get(i).getItemId());
                    Log.d(TAG, "onChanged: " + docIDs.get(i));

                    testString = sb.append("Item " + i + " is " + itemEntries.get(i).getItemId() + "\n").toString();
                }
                Log.d(TAG, "onChanged: " + docIDs.size());
                //mTestTv.setText(testString);
            }
        });
    }

    /**
     * sets up the recyclerView.
     */
    private void setUpRecyclerView() {
        // Create a query when requesting data from firestore
        Query query = itemRef; //DataHandlingUtils.makePrefQuery(this, itemRef);
        FirestoreRecyclerOptions<Item> options =  new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();
        mFirestoreAdapter = new FirestoreItemAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.rv_show_ordered_items);
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

                //Make a new intent and pass this document ID into it as a string Extra
                Intent detailIntent = new Intent(getApplicationContext(), ItemDetailsActivity.class);
                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_DOCUMENT_ID, documentSnapshot.getId());
                startActivity(detailIntent);

            }
        });
    }
}
