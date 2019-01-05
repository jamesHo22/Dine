package com.example.dine.dine.RoomDb;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class AddLocationViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final int mLocationId;

    public AddLocationViewModelFactory(AppDatabase db, int locationId) {
        mDb = db;
        mLocationId = locationId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AddLocationViewModel(mDb, mLocationId);
    }
}
