package com.example.dine.dine.RoomDb;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * This class is a POJO. Each one of the member variables represents a column in the table that ROOM
 * generates. This class is an entity associated with a table called "ItemsTable"
 */

@Entity(tableName = "ItemsTable")
public class ItemEntry implements BaseModel{

    public int VIEW_TYPE_ITEMS = ViewType.VIEW_TYPE_ITEMS;
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String item_id;
    public String title;
    public String description;
    public int price;
    public int progress;

    @Ignore
    public ItemEntry(String item_id, String title, String description, int price, int progress) {
        this.item_id = item_id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.progress = progress;
    }

    public ItemEntry(int id, String item_id, String title, String description, int price, int progress) {
        this.id = id;
        this.item_id = item_id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.progress = progress;
    }
    public int getId() {
        return this.id;
    }

    public String getItemId() {
        return this.item_id;
    }

    public String getTitle() { return this.title; }

    public String getDescription() { return this.description; }

    public int getPrice() { return this.price; }

    public int getProgress() { return progress; }

    public void setId(int id) {this.id = id;}

    @Override
    public int getViewType() {
        return VIEW_TYPE_ITEMS;
    }
}
