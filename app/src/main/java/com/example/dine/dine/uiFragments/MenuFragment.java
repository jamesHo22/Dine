package com.example.dine.dine.uiFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dine.dine.DataHandlingUtils;
import com.example.dine.dine.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuFragment extends android.support.v4.app.Fragment {

    /**
     * Variables
     */
    private final String TAG = this.getClass().getName();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private DataHandlingUtils mDataHandlingUtils = new DataHandlingUtils();
    private View rootView;

    /**
     * Interfaces
     */

    /**
     * Android Methods/Callbacks
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        return rootView;
    }



    /**
     * Non-Android Callbacks
     */

    /**
     * Methods
     */
}
