package com.example.dine.dine.RoomDb;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * This class is a POJO. Each one of the member variables represents a column in the table that ROOM
 * generates. This class is an entity associated with a table called "LocationTable"
 */

@Entity(tableName = "LocationTable")
public class LocationEntry implements BaseModel {

    public int VIEW_TYPE_LOCATIONS = ViewType.VIEW_TYPE_LOCATIONS;

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String location_id;
    public String name;
    public String address;
    public float latitude;
    public float longitude;
    public float distance;

    @Ignore
    public LocationEntry(String location_id, String name, String address, float latitude, float longitude, float distance) {
        this.location_id = location_id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public LocationEntry(int id, String location_id, String name, String address, float latitude, float longitude, float distance) {
        this.id = id;
        this.location_id = location_id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getLocation_id() {
        return location_id;
    }

    public String getName() {
        return name;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE_LOCATIONS;
    }
}
