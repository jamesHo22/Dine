package com.example.dine.dine.RoomDb;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface LocationDao {

    @Query("SELECT * FROM LocationTable ORDER BY id")
    LiveData<List<LocationEntry>> loadAllLocations();

    @Insert
    void insertLocation(LocationEntry locationEntry);

    @Update
    void updateLocation(LocationEntry locationEntry);

    @Delete
    void deleteLocation(LocationEntry locationEntry);

    @Query("DELETE FROM LocationTable")
    public void nukeTable();

    @Query("SELECT * FROM LocationTable WHERE id = :id")
    LiveData<LocationEntry> loadLocationById(int id);
}
