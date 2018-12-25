package com.example.dine.dine.RoomDb;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

/**
 * Creates the database connection
 */
@Database(entities = {ItemEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "MenuItems";
    private static AppDatabase sInstance; // Is null when declared

    /**
     * Singleton pattern for ensuring that only one AppDatabase is created.
     * @param context
     * @return a Room database or a connection to an existing room database if one exists.
     */
    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        //Move all database operations to background thread.
                        .allowMainThreadQueries()
                        //FIXME: Disable main thread queries once everything works
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract ItemDao ItemDao();
}
