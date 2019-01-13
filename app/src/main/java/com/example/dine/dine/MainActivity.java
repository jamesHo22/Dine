package com.example.dine.dine;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dine.dine.uiFragments.MenuFragment;
import com.example.dine.dine.uiFragments.RecommendationFragment;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements BottomSheetDialogue.BottomSheetListener, RecommendationFragment.MainActivityInterface {

    //
    /**
     * Variables
     */
    private final String TAG = this.getClass().getSimpleName();
    private Toolbar myToolbar;
    private static android.support.v4.app.FragmentManager fragmentManager;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 25;
    private static final String TAG_RECOMMENDATION_FRAGMENT= "tag_recommendation_fragment";
    private static final String TAG_MENU_FRAGMENT= "tag_menu_fragment";
    private static boolean menuIconClicked = true;
    private FragmentTransaction fragmentTransaction;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    /**
     * Interfaces
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setup toolbar
        myToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

//        TODO: Instantiate a ViewPager and a PagerAdapter.
//        mPager = (ViewPager) findViewById(R.id.view_pager);
//        mPagerAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
//        mPager.setAdapter(mPagerAdapter);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        // Create fragment
        RecommendationFragment recommendationFragment = new RecommendationFragment();
        MenuFragment menuFragment = new MenuFragment();

        // create fragment
        if (savedInstanceState==null) {
            // Activity created for first time
            // Create both fragments.
            Log.d(TAG, "onCreate: savedInstanceState is null");
            fragmentTransaction.add(R.id.test_container, recommendationFragment, TAG_RECOMMENDATION_FRAGMENT);
            fragmentTransaction.add(R.id.test_container, menuFragment, TAG_MENU_FRAGMENT);
            fragmentTransaction.commit();

//            // Check for or request for permissions
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // Permission is not granted
//                // Should we show an explanation?
//                Log.d(TAG, "onCreate: permission not granted");
//
//                // No explanation needed; request the permission
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
//                Log.d(TAG, "onCreate: permission requested");
//
//            } else {
//                // Permission already granted, get location
//                Log.d(TAG, "onCreate: permission already granted");
//                Bundle bundle = new Bundle();
//                bundle.putBoolean(Constants.TAG_ACCESS_FINE_LOCATION_PERMISSION_GRANTED, true);
//                recommendationFragment.setArguments(bundle);
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: woot " + menuIconClicked);
        if (menuIconClicked) {
            // No press
           switchToRecommendationFrag();

        } else {
            // Odd number press
            switchToMenuFrag();
        }
    }

    private void switchToRecommendationFrag() {
//        RecommendationFragment recommendationFragment = (RecommendationFragment) getSupportFragmentManager().findFragmentByTag(TAG_RECOMMENDATION_FRAGMENT);
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.detach(getSupportFragmentManager().findFragmentByTag(TAG_MENU_FRAGMENT));
//        fragmentTransaction.attach(recommendationFragment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commitAllowingStateLoss();
//        getSupportFragmentManager().executePendingTransactions();

        MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentByTag(TAG_MENU_FRAGMENT);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(menuFragment);
        fragmentTransaction.show(getSupportFragmentManager().findFragmentByTag(TAG_RECOMMENDATION_FRAGMENT));
        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void switchToMenuFrag() {

//        MenuFragment menuFragment = (MenuFragment) getSupportFragmentManager().findFragmentByTag(TAG_MENU_FRAGMENT);
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.detach(getSupportFragmentManager().findFragmentByTag(TAG_RECOMMENDATION_FRAGMENT));
//        fragmentTransaction.attach(menuFragment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commitAllowingStateLoss();
//        getSupportFragmentManager().executePendingTransactions();

        RecommendationFragment recommendationFragment = (RecommendationFragment) getSupportFragmentManager().findFragmentByTag(TAG_RECOMMENDATION_FRAGMENT);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(recommendationFragment);
        fragmentTransaction.show(getSupportFragmentManager().findFragmentByTag(TAG_MENU_FRAGMENT));
        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "onRequestPermissionResult: permission already granted");
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.TAG_ACCESS_FINE_LOCATION_PERMISSION_GRANTED, true);
                    RecommendationFragment recommendationFragment = new RecommendationFragment();
                    //recommendationFragment.setArguments(bundle);
                    // Refresh the fragment
                    getSupportFragmentManager().beginTransaction().add(R.id.test_container, recommendationFragment, TAG_RECOMMENDATION_FRAGMENT).commit();
                    switchToRecommendationFrag();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(TAG, "onRequestPermissionResult: permission declined");
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationClicked(String locationId, int roomId) {
        // Make a bundle for the data
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ON_LOCATION_CLICKED_LOCATION_ID, locationId);
        bundle.putInt(Constants.ON_LOCATION_CLICKED_ROOM_ID, roomId);
        // set Fragmentclass Arguments
//        RecommendationFragment recommendationFragment = new RecommendationFragment();
//        recommendationFragment.setArguments(bundle);
        // Refresh the fragment
        //fragmentManager.beginTransaction().replace(R.id.test_container, recommendationFragment, TAG_RECOMMENDATION_FRAGMENT).commit();
        RecommendationFragment recommendationFragment1 = (RecommendationFragment) fragmentManager.findFragmentByTag(TAG_RECOMMENDATION_FRAGMENT);
        recommendationFragment1.setArguments(bundle);
        recommendationFragment1.setItemRef(roomId);
    }

    /**
     * Get the information from the fragment to set up the toolbar
     * @param toolbarTitle
     */
    @Override
    public void toolBarSetup(String toolbarTitle) {
        myToolbar.setTitle(toolbarTitle);
    }

    /**
     * Does something when the an item in the overflow menu is touched.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //TODO: instead of launching intents to different activities, set new fragments

            case R.id.change_location:
                BottomSheetDialogue bottomSheetDialogue = new BottomSheetDialogue();
                bottomSheetDialogue.show(getSupportFragmentManager(), "example_bottom_sheet");
                return true;

            case R.id.action_preferences:
                Intent preferenceIntent = new Intent(this, SettingsActivity.class);
                startActivity(preferenceIntent);
                return true;

            case R.id.action_rewards:
                Intent rewardsIntent = new Intent(this, RewardsActivity.class);
                startActivity(rewardsIntent);
                return true;

            case R.id.action_sign_out:
                signOut();
                Intent signOutIntent = new Intent(this, SignInActivity.class);
                startActivity(signOutIntent);
                finish();
                return true;

            case R.id.action_menu:
                if (menuIconClicked) {
                    // Odd number press
                    menuIconClicked = false;
                    switchToMenuFrag();

                } else {
                    // Even number press
                    menuIconClicked = true;
                    switchToRecommendationFrag();
                }
                Log.d(TAG, "onOptionsItemSelected: button state" + menuIconClicked);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Inflates the menu in the toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        // TODO: use the following to set menu based on what fragment is being displayed.
//        MenuItem menuItem = menu.findItem(R.id.action_menu);
//        menuItem.setVisible(false);
        return true;
    }

    /**
     * Sign out
     */
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Signed Out", Toast.LENGTH_LONG).show();
    }
}
