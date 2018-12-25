package com.example.dine.dine.RoomDb;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * This class is a POJO. Each one of the member variables represents a column in the table that ROOM
 * generates. This class is an entity associated with a table called "ItemsTable"
 */

@Entity(tableName = "ItemsTable")
public class ItemEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String item_id;

    @Ignore
    public ItemEntry(String item_id) {
        this.item_id = item_id;
    }

    public ItemEntry(int id, String item_id) {
        this.id = id;
        this.item_id = item_id;
    }
    public int getId() {
        return id;
    }

    public String getItemId() {
        return this.item_id;
    }

}
