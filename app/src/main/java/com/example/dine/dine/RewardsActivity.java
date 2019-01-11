package com.example.dine.dine;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RewardsActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();
    private android.support.v7.widget.Toolbar myToolbar;
    private TextView numPointsTv;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private DataHandlingUtils mDataHandlingUtils = new DataHandlingUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        // Setup toolbar
        myToolbar = findViewById(R.id.toolbar_rewards);
        myToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                onBackPressed();
            }
        });

        numPointsTv = findViewById(R.id.num_points_tv);
        // Get the number of points from FireStore then set that to TextView
        setNumPointsTv(mAuth.getUid(), mDb);

    }

    private void setNumPointsTv(final String user_id, FirebaseFirestore db) {
        DocumentReference userReference = mDataHandlingUtils.buildFirestoreUserReference(user_id, db);
        db.document(userReference.getPath()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()) {
                    try{
                        // get the number of points and use getMyReward() method to set appropriate award in user's collection.
                        long numPoints = task.getResult().getLong("points");
                        numPointsTv.setText(getString(R.string.num_points, numPoints));
                        Log.d(TAG, "onComplete: num points: " + numPoints);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onComplete: could not retrieve the number of points", e);
                    }
                } else {
                    Log.d(TAG, "onComplete: user with " + user_id + " does not exist.");
                }
            }
        });
    }
}
