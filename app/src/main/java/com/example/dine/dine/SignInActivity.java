package com.example.dine.dine;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

//TODO: Add Google FoodActivity

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private final String TAG = this.getClass().getName();
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private Button signOutButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button makeDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Get a reference to the sign in button
        signInButton = findViewById(R.id.sign_in);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        // Get reference to signout button
        signOutButton = findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        // Set an OnClickListener to start the sign in flow
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        // Set an OnClickListener for making firebase data
        makeDataButton = findViewById(R.id.make_data);
        makeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeData();
            }
        });

        // Create google sign in object
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Request the ID Token (needed for firebase auth)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void makeData() {
        // Access a cloud firestore instance
        db = FirebaseFirestore.getInstance();
        // Create a collection and a document
        Map<String, Object> user = new HashMap<>();
        user.put("first", "James");
        user.put("last", "Ho");
        user.put("born", 2000);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if there is a firebase currentUser.
        FirebaseUser currentUser = mAuth.getCurrentUser();

//        // Check if an existing user is signed in
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (currentUser != null) {
            //TODO: Display the main page
            Log.v(TAG, "User siged in " + currentUser.toString());
        } else {
            Log.v(TAG, "No user signed in");
            // TODO: display the sign in page
        }
    }

    private void signIn() {
        /**
         * Initiate the sign in intent that prompts the user to select a google account.
         * Starts the intent with a startActivityForResult
         */
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Signed Out", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * If the request code is equal to RC_SIGN_IN, create a Task and pass it to handleSignInResult()
         */
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                updateUI(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            Intent intent = new Intent(getApplicationContext(), FoodActivity.class);
            intent.putExtra("accountName", account.getDisplayName());
            startActivity(intent);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        /**
         * After a user successfully signs in, get an ID token from the GoogleSignInAccount object,
         * exchange it for a Firebase credential, and authenticate with Firebase using the Firebase credential:
         */
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), "Signed in as " + user.getDisplayName(), Toast.LENGTH_LONG).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //TODO: figure out how to use android snackbars
                            //Snackbar.make(, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
