package com.example.dine.dine;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dine.dine.RoomDb.BaseModel;
import com.example.dine.dine.RoomDb.ItemEntry;
import com.example.dine.dine.RoomDb.MainViewModel;
import com.example.dine.dine.uiDrawers.RoomRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderSummaryActivity extends AppCompatActivity implements RoomRecyclerViewAdapter.ClickHandler {

    //TODO (1 Complete): get the document IDs from the MainViewModel
    //TODO (2): make a query to FireStore to get only those documents
    //TODO (3): display those items in a recyclerView

    private final static String TAG = OrderSummaryActivity.class.getSimpleName();

    //RecyclerView variables
    private RoomRecyclerViewAdapter mAdapter;
    private MainViewModel mMainViewModel;
    private Toolbar mToolbar;
    private TextView mSumTv;
    private DataHandlingUtils dataHandlingUtils = new DataHandlingUtils();
    private TextView mPlaceOrderButton;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        setUpRecyclerView();
        mToolbar = findViewById(R.id.order_summary_toolbar);
        mPlaceOrderButton = findViewById(R.id.place_order);
        mToolbar.setTitle("Your Order");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                onBackPressed();
            }
        });

        mPlaceOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderItems();
            }
        });


    }

    /**
     * Orders the items in the cart
     */
    private void orderItems() {
        final ArrayList<String> item_ids = new ArrayList<>();
        mMainViewModel.getItems().observe(this, new Observer<List<ItemEntry>>() {
            @Override
            public void onChanged(@Nullable List<ItemEntry> itemEntries) {
                item_ids.clear();
                for(int i = 0; i < itemEntries.size(); i++) {
                    item_ids.add(itemEntries.get(i).getItemId());
                    Log.d(TAG, "onChanged: " + item_ids.get(i));
                }
            }
        });

        dataHandlingUtils.orderItems(mAuth, db, this, item_ids);
        dataHandlingUtils.deleteAllItemsRoom(this);
        finish();
    }

    /**
     * sets up the recyclerView.
     */
    private void setUpRecyclerView() {

        mAdapter = new RoomRecyclerViewAdapter(new ArrayList<ItemEntry>(), this, this);
        RecyclerView recyclerView = findViewById(R.id.rv_show_ordered_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        mSumTv = findViewById(R.id.total_price);
        Log.d(TAG, "setUpRecyclerView: setup recycler view" );

        mMainViewModel.getItems().observe(this, new Observer<List<ItemEntry>>() {
            @Override
            public void onChanged(@Nullable List<ItemEntry> itemEntries) {
                Log.d(TAG, "onChanged: called");
                mAdapter.addItems(itemEntries);
                Log.d(TAG, "onChanged: total number of items " + itemEntries.size());

                // Get sum of price
                //FIXME: probably put this in another method
                float sum = 0;
                for(int i = 0; i<itemEntries.size(); i++) {
                    sum += (float) itemEntries.get(i).getPrice();
                    Log.d(TAG, "onChanged: sum of items " + sum);
                    mSumTv.setText("$" + String.format("%.02f", sum/100));
                }
            }
        });




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
                List<? extends BaseModel> mItemEntries;
                int position = viewHolder.getAdapterPosition();
                if (direction==ItemTouchHelper.RIGHT) {
                    mDirection = "right";

                    int adapterPosition = viewHolder.getAdapterPosition();
                    mItemEntries = mAdapter.getItemEntries();
                    //dataHandlingUtils.deleteItemRoom(mItemEntries.get(adapterPosition), getApplicationContext());
                } else {
                    mDirection = "left";
                    // Do not delete the item.
                    //((FirestoreItemAdapter) mFirestoreAdapter).deleteItem(position);
                }

                Toast.makeText(getApplicationContext(), "You swiped " + mDirection + " on card " + String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        /**
         * The onItemClick function does something when it is clicked. It is an interface from FirestoreItemAdapter.onItemClickListener()
         * that must be overridden.
         * The one below hides and shows the order item button.
         */
//        mAdapter.(new FirestoreItemAdapter.onItemClickListener() {
//            @Override
//            public void onItemClick(final DocumentSnapshot documentSnapshot, int position, View itemView) {
//
//                //Make a new intent and pass this document ID into it as a string Extra
//                Intent detailIntent = new Intent(getApplicationContext(), ItemDetailsActivity.class);
//                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_DOCUMENT_ID, documentSnapshot.getId());
//                startActivity(detailIntent);
//
//            }
//        });
    }

    // OnClick for RV
    @Override
    public void onClick(String locationID) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
