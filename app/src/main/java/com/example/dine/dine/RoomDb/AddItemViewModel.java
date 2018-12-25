package com.example.dine.dine.RoomDb;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

/**
 * This class gets a reference to the LiveData database and returns a item by ID
 */
public class AddItemViewModel extends ViewModel {
    private LiveData<ItemEntry> item;
    public AddItemViewModel(AppDatabase database, int itemId) {
        item = database.ItemDao().loadItemById(itemId);
    }
    public LiveData<ItemEntry> getItem() {
        return item;
    }
}
