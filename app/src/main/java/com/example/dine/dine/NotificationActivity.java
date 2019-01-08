package com.example.dine.dine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        RelativeLayout relativeLayout = findViewById(R.id.notification_rl);
        relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorNotification));
    }
}
