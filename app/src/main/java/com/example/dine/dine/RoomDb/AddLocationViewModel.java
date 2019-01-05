package com.example.dine.dine.RoomDb;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

public class AddLocationViewModel extends ViewModel {

    private LiveData<LocationEntry> location;

    public AddLocationViewModel(AppDatabase database, int locationId) {

        location = database.LocationDao().loadLocationById(locationId);
    }

    public LiveData<LocationEntry> getLocation() { return location; }
}
