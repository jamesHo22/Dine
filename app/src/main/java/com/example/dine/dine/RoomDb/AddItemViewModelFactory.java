package com.example.dine.dine.RoomDb;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

/**
 * This class creates a view model
 */
public class AddItemViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final int mItemId;

    public AddItemViewModelFactory(AppDatabase db, int itemId) {
        mDb = db;
        mItemId = itemId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AddItemViewModel(mDb, mItemId);
    }
}
