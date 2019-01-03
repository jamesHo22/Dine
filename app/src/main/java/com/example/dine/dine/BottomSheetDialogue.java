package com.example.dine.dine;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dine.dine.RoomDb.LocationEntry;
import com.example.dine.dine.RoomDb.MainViewModel;
import com.example.dine.dine.uiDrawers.RoomRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetDialogue extends BottomSheetDialogFragment {

    private static final String TAG = BottomSheetListener.class.getSimpleName();

    // Assign this variable with the activity we want the information to be sent to.
    // Do this by overiding onAttach.
    private BottomSheetListener mListener;
    private RoomRecyclerViewAdapter mAdapter;
    private MainViewModel mMainViewModel;

    /**
     * Inflate the layout that will become the bottom sheet fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return a view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet, container, false);

//        // You can access the views within the bottom sheet as if it were an Activity
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mAdapter = new RoomRecyclerViewAdapter(new ArrayList<LocationEntry>(), getContext());
        RecyclerView locationRv = v.findViewById(R.id.location_rv);
        locationRv.setHasFixedSize(true);
        locationRv.setLayoutManager(new LinearLayoutManager(v.getContext()));
        locationRv.setAdapter(mAdapter);

        mMainViewModel.getLocations().observe(this, new Observer<List<LocationEntry>>() {
            @Override
            public void onChanged(@Nullable List<LocationEntry> locationEntries) {
                mAdapter.addItems(locationEntries);
            }
        });

        return v;
    }

    /**
     * Create an interface that is implemented in the activity that uses this bottom sheet.
     * It allows the bottom sheet fragment to pass information to the activity.
     */
    public interface BottomSheetListener {
        /**
         * parameters can be set to anything you want to pass from the fragment to the activity.
         * @param text
         */
        void onButtonClicked(String text);
    }

    /**
     * When dialog is opened, this method assigns activity to the mListener variable.
     * Assigns the context to it.
     * This allows us to send information from the fragment to the activity
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // use a try catch to make sure that bottom sheet listener is implemented
        try {
            // cast context into BottomSheetListener
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }

    }
}
