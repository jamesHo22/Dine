package com.example.dine.dine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    // Key value to hold key for the value.
    public static final String KEY_GLUTEN_FREE_SWITCH = "gluten_free_switch";
    public static final String KEY_VEGAN_SWITCH = "vegan_switch";
    public static final String KEY_VEGETARIAN_SWITCH = "vegetarian_switch";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // display fragment as main view
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
