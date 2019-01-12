package com.example.dine.dine;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.text.DateFormat;
import java.util.Date;

public class LocationUtils {

    private final String TAG = LocationUtils.class.getSimpleName();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private static Location mCurrentLocation;

    private static String mLastUpdateTime;
    private LocationUpdateListener locationUpdateListener;

    public interface LocationUpdateListener {
        void onLocationUpdate(Location location);
    }

    /**
     * initializes variables that are used to determine the current location of the client
     * @param context of the activity that calls it.
     */
    public void init(final Context context, LocationUpdateListener mLocationUpdateListener) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);
        locationUpdateListener = mLocationUpdateListener;

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                locationUpdateListener.onLocationUpdate(mCurrentLocation);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                Toast.makeText(context, "Your current location is: Lat: " + mCurrentLocation.getLatitude() + " Long: " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "onLocationResult: Lat: " + mCurrentLocation.getLatitude() + " Long: " + mCurrentLocation.getLongitude());
            }
        };

        // Request a new location when user moves 10 meters
        mLocationRequest = new LocationRequest();
        mLocationRequest
                .setSmallestDisplacement(10)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void getCoordinates(Context context) {
        if (ActivityCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }
}
