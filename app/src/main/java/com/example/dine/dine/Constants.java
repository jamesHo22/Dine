package com.example.dine.dine;

public class Constants {
    /**
     * For Order Summary recyclerView to identify what to display. Sets constant int in roomDatabase
     */
    public static final int ITEM_ENTRY_PROGRESS_COOKING = 100;
    public static final int ITEM_ENTRY_PROGRESS_NOT_ORDERED = 300;

    /**
     * For MainActivity BottomSheet OnLocationClicked method overide
     */
    public static final String ON_LOCATION_CLICKED_LOCATION_ID = "location_id";
    public static final String ON_LOCATION_CLICKED_ROOM_ID = "location_id";
    public static final String TAG_ACCESS_FINE_LOCATION_PERMISSION_GRANTED= "fine_location_granted_code";
    public static final int CODE_ACCESS_FINE_LOCATION_PERMISSION_GRANTED = 935;

    /**
     * Firestore Database Paths and Fields variables
     */
    public static final String PATH_EMPTY = " ";
    public static final String PATH_RESTAURANT = "restaurants_2";
    public static final String PATH_MENU_ITEMS = "menuItems";
    public static final String FIELD_RESTAURANT_NAME = "restaurantName";
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_LOCATION_LAT_LONG = "location.lat_long";
    public static final String FIELD_CHIPS_DIET_RESTRICTION = "chipsDietRestriction";
    public static final String VALUE_GLUTEN_FREE = "glutenFree";
    public static final String VALUE_VEGAN = "vegan";
    public static final String VALUE_VEGETARIAN = "vegetarian";
    public static final String FIELD_FILE = "file";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_ORDERED_BY = "orderedBy";
    public static final String FIELD_ORDER_TIME = "orderTime";
    public static final String FIELD_MENU_ITEM_IDS = "menuItemIds";
    public static final String PATH_USERS = "users_2";
    public static final String PATH_ORDER_INFO = "order_info";

    /**
     * Shared Preferences Variables/Keys
     */
    final public static String INTENT_EXTRA_DOCUMENT_ID = "document_id";
    final public static String INTENT_EXTRA_KEY_DETAILED_TITLE = "detailed_title";
    final public static String INTENT_EXTRA_KEY_DETAILED_DESCRIPTION = "detailed_description";
    final public static String INTENT_EXTRA_KEY_DETAILED_IMAGE_URI = "detailed_image_uri";
    final public static String INTENT_EXTRA_KEY_DETAILED_PRICE = "detailed_price";
    final public static String INTENT_EXTRA_KEY_LOCATION_ID = "location_id";

    /**
     * Code Format Below ---------------------------------------------------------------------------
     */

    /**
     * Variables
     */

    /**
     * Interfaces
     */

    /**
     * Android Methods/Callbacks
     */

    /**
     * Non-Android Callbacks
     */

    /**
     * Methods
     */
}
