package com.example.dine.dine.uiFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.example.dine.dine.R;
import com.example.dine.dine.uiDrawers.FirestoreItemAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MenuFragment extends android.support.v4.app.Fragment {

    /**
     * Variables
     */
    private final String TAG = this.getClass().getName();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private DataHandlingUtils mDataHandlingUtils = new DataHandlingUtils();
    private View rootView;

    //Firestore recyclerview
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter mFirestoreAdapter;
    private static String mRestaurantDocumentId;
    private CollectionReference itemRef;

    /**
     * Interfaces
     */

    /**
     * Android Methods/Callbacks
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemRef = db.collection(Constants.PATH_RESTAURANT).document("gsruIzBRG2AZBGbbv7Uv").collection(Constants.PATH_MENU_ITEMS);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        setUpRecyclerView(rootView);
        return rootView;
    }



    /**
     * Non-Android Callbacks
     */

    /**
     * Methods
     */

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
        RecyclerView recyclerView = rootView.findViewById(R.id.rv_show_all_menu_items);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(mFirestoreAdapter);
        mFirestoreAdapter.startListening();

        int firstItemPosition = layoutManager.findFirstVisibleItemPosition();
        Log.d(TAG, "setUpRecyclerView: " + firstItemPosition);


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

//        /**
//         * The onItemClick function does something when it is clicked. It is an interface from FirestoreItemAdapter.onItemClickListener()
//         * that must be overridden.
//         * The one below hides and shows the order item button.
//         */
//        ((FirestoreItemAdapter) mFirestoreAdapter).setOnItemClickListener(new FirestoreItemAdapter.onItemClickListener() {
//            @Override
//            public void onItemClick(final DocumentSnapshot documentSnapshot, int position, View itemView) {
//
//                // Get values of objects in document snapshot
//                String document_id = documentSnapshot.getId();
//                String title = documentSnapshot.get("title").toString();
//                String description = documentSnapshot.get("description").toString();
//                int price = Integer.valueOf(documentSnapshot.get("price").toString());
//
//                //Make a new intent and pass this document ID into it as a string Extra
//                Intent detailIntent = new Intent(getContext(), ItemDetailsActivity.class);
//                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_DOCUMENT_ID, document_id);
//                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_DETAILED_TITLE, title);
//                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_DETAILED_DESCRIPTION, description);
//                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_DETAILED_PRICE, price);
//                detailIntent.putExtra(ItemDetailsActivity.INTENT_EXTRA_KEY_LOCATION_ID, mRestaurantDocumentId);
//                Log.d(TAG, "onItemClick: " + mRestaurantDocumentId);
//                startActivity(detailIntent);
//            }
//        });
    }
}
