package com.example.dine.dine.RoomDb;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

/**
 * This class is responsible for holding data through Activity configuration changes (rotation)
 * Also prevents memory leaks. Allows for asynchronous calls. Since viewmodel isn't restarted, you can make asynchronous calls here.
 * Returns are stored here
 */
public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<ItemEntry>> items;

    /**
     * Cache LiveData objects
     * @param application
     */
    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase mDb = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "MainViewModel: Actively retrieving from the Database");
        items = mDb.ItemDao().loadAllItems();
    }

    public LiveData<List<ItemEntry>> getItems() {
        return items;
    }
}
