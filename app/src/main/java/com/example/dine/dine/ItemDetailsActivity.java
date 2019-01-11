package com.example.dine.dine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dine.dine.RoomDb.AppDatabase;
import com.example.dine.dine.RoomDb.ItemEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.squareup.picasso.Picasso;

public class ItemDetailsActivity extends AppCompatActivity {

    private final static String TAG = ItemDetailsActivity.class.getName();
    final public static String INTENT_EXTRA_DOCUMENT_ID = "document_id";
    final public static String INTENT_EXTRA_KEY_DETAILED_TITLE = "detailed_title";
    final public static String INTENT_EXTRA_KEY_DETAILED_DESCRIPTION = "detailed_description";
    final public static String INTENT_EXTRA_KEY_DETAILED_IMAGE_URI = "detailed_image_uri";
    final public static String INTENT_EXTRA_KEY_DETAILED_PRICE = "detailed_price";
    final public static String INTENT_EXTRA_KEY_LOCATION_ID = "location_id";

    // create member variables
    private String mTitle;
    private String mDescription;
    private String mImageUri;
    private float mPrice;
    private DataHandlingUtils dataHandlingUtils = new DataHandlingUtils();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // get instance of firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Set source to local to save database calls. Load document from local cache
    Source source = Source.CACHE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        //Receive firestore doc id from the card that launched this activity.
        Intent launchIntent = getIntent();
        final String document_id = launchIntent.getStringExtra(INTENT_EXTRA_DOCUMENT_ID);
        final String title = launchIntent.getStringExtra(INTENT_EXTRA_KEY_DETAILED_TITLE);
        final String description = launchIntent.getStringExtra(INTENT_EXTRA_KEY_DETAILED_DESCRIPTION);
        final String restaurantDocumentId = launchIntent.getStringExtra(INTENT_EXTRA_KEY_LOCATION_ID);
        final int price = launchIntent.getIntExtra(INTENT_EXTRA_KEY_DETAILED_PRICE, 1234);
        CollectionReference itemRef = db.collection("restaurants_2")
                .document(restaurantDocumentId)
                .collection("menu_items");
        Log.d(TAG, "onCreate: " + document_id);

        //Find the views.
        final TextView detailed_description_tv = findViewById(R.id.detailed_description_tv);
        final CollapsingToolbarLayout detailed_toolbar = findViewById(R.id.collapsingToolbarLayout);
        final ImageView detailed_iv = findViewById(R.id.detailed_iv);
        final FloatingActionButton floatingActionButton = findViewById(R.id.fab_detailed);
        final AppDatabase mDb = AppDatabase.getInstance(this);

        // Store item information in variables
        // Get the document, forcing the SDK to use the offline cache
        /**
         * Set the correct information to the views
         */
        itemRef.document(document_id).get(source).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // Document found in the offline cache
                    final DocumentSnapshot document = task.getResult();
                    Log.d(TAG, "Cached document data: " + document.getData());
                    detailed_description_tv.setText(document.get("description").toString());
                    detailed_toolbar.setTitle(document.get("title").toString());
                    // Get context for picasso
                    final Context context = getApplicationContext();
                    Uri imageUri = Uri.parse(document.get("imageUri").toString());
                    // Loads the image URI from that document into the imageView.
                    Picasso.with(context).load(imageUri).fit().centerCrop().into(detailed_iv);

                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Sends the order to the current orders collection
                            dataHandlingUtils.orderOneItem(mAuth, db, document, getApplicationContext());
                            // Insert document ID into local database
                            ItemEntry itemEntry = new ItemEntry(document_id, title, description, price, Constants.ITEM_ENTRY_PROGRESS_NOT_ORDERED);
                            dataHandlingUtils.insertItemRoom(itemEntry, context);
                            finish();
                        }
                    });
                } else {
                    Log.d(TAG, "Cached get failed: ", task.getException());
                }
            }
        });
    }
}
