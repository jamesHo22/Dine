package com.example.dine.dine.RoomDb;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * This class is where you overide the database actions. DAO = Data Access Objects
 */
@Dao
public interface ItemDao {

    @Query("SELECT * FROM ItemsTable ORDER BY id")
    LiveData<List<ItemEntry>> loadAllItems();

    @Insert
    void insertItem(ItemEntry itemEntry);

    @Update
    void updateitem(ItemEntry itemEntry);

    @Delete
    void deleteItem(ItemEntry itemEntry);

    @Query("SELECT * FROM ItemsTable WHERE id = :id")
    LiveData<ItemEntry> loadItemById(int id);
}
