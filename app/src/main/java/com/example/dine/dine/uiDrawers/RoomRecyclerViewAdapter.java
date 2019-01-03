package com.example.dine.dine.uiDrawers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dine.dine.R;
import com.example.dine.dine.RoomDb.BaseModel;
import com.example.dine.dine.RoomDb.ItemEntry;
import com.example.dine.dine.RoomDb.LocationEntry;
import com.example.dine.dine.RoomDb.ViewType;

import java.text.DecimalFormat;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.BaseViewHolder> {

//    private List<ItemEntry> mItemEntries;
//
//    public RoomRecyclerViewAdapter(List<ItemEntry> itemEntries) {
//        mItemEntries = itemEntries;
//        //mClickListener = ClickListener;
//    }
//
//    /**
//     * Inflates the recycled views
//     * @param parent
//     * @param viewType
//     * @return
//     */
//    @NonNull
//    @Override
//    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//        // This method is in charge of creating new views
//        Context context = parent.getContext();
//        // The following gets a reference to the layout to inflate when more views are needed
//        int layoutIdForMenuItem = R.layout.order_summary_view;
//        LayoutInflater inflater = LayoutInflater.from(context);
//        boolean shouldAttachToParentImmediately = false;
//        View view = inflater.inflate(layoutIdForMenuItem, parent, shouldAttachToParentImmediately);
//        RoomViewHolder viewHolder = new RoomViewHolder(view);
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
//        ItemEntry item = mItemEntries.get(position);
//        float price = (float)item.getPrice()/100;
//        Log.d(TAG, "onBindViewHolder: " + price);
//        holder.orderSummaryTitleTv.setText(item.getTitle());
//        DecimalFormat df = new DecimalFormat();
//        df.setMaximumFractionDigits(2);
//        holder.orderSummaryPriceTv.setText("$" + String.valueOf(df.format(price)));
//    }
//
//    @Override
//    public int getItemCount() {
//        if (mItemEntries == null) {
//            Log.v("RecyclerView", "mItemEntries is null");
//            return 0;
//        }
//        return mItemEntries.size();
//    }
//
//    public class RoomViewHolder extends RecyclerView.ViewHolder {
//
//        private TextView orderSummaryTitleTv;
//        private TextView orderSummaryDescriptionTv;
//        private TextView orderSummaryPriceTv;
//
//        public RoomViewHolder(View itemView) {
//            super(itemView);
//            orderSummaryTitleTv = itemView.findViewById(R.id.order_summary_title);
//            orderSummaryDescriptionTv = itemView.findViewById(R.id.order_summary_description);
//            orderSummaryPriceTv = itemView.findViewById(R.id.order_summary_price);
//        }
//    }
//
//    public void addItems(List<ItemEntry> itemEntries) {
//        this.mItemEntries = itemEntries;
//        notifyDataSetChanged();
//    }
//
//    public void addLocations(List<LocationEntry> locationEntries) {
//        notifyDataSetChanged();
//    }
//
//    public List<ItemEntry> getItemEntries() {
//        return mItemEntries;
//    }

    /**
     * Learning to use one adapter for different datatypes. REMEMBER to notifyDataSetChanged to update views
     */
    private List<? extends BaseModel> mList;
    private LayoutInflater mInflator;
    // A variable to holds a reference to the UI that displays the views
    private ClickHandler mClickHandler;

    // Create an interface that the UI activity implements to pass the click events from the RV to the activity
    public interface ClickHandler {
        void onClick(String locationID);
    }

    /**
     *
     * @param list is a list of objects that extends BaseModel
     * @param context is the context of the activity or fragment that shows the RV
     * @param clickHandler is what is handling the clicks in the UI (ex. the BottomSheetFragment)
     */
    public RoomRecyclerViewAdapter(List<? extends BaseModel> list, Context context, ClickHandler clickHandler) {
        //this.mList = list;
        this.mInflator = LayoutInflater.from(context);

        try {
            // Set the click handler to the UI component
            this.mClickHandler = clickHandler;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onClick()");
        }

    }

    /**
     * Adds objects to the mList variable
     * @param list is any object that implements BaseModel
     */
    public void addItems(List<? extends BaseModel> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder class extends this one. Each one can be used given the itemDataType
     * @param <T> represents the data type
     */
    public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(T object);
    }

    /**
     * This class is responsible for referencing views that show ItemEntries
     */
    public class ItemHolder extends BaseViewHolder<ItemEntry> {

        private TextView orderSummaryTitleTv;
        private TextView orderSummaryDescriptionTv;
        private TextView orderSummaryPriceTv;

        public ItemHolder(View itemView) {
            super(itemView);
            orderSummaryTitleTv = itemView.findViewById(R.id.order_summary_title);
            orderSummaryDescriptionTv = itemView.findViewById(R.id.order_summary_description);
            orderSummaryPriceTv = itemView.findViewById(R.id.order_summary_price);
        }

        @Override
        public void bind(ItemEntry object) {
            float price = (float)object.getPrice()/100;
            Log.d(TAG, "onBindViewHolder: " + price);
            orderSummaryTitleTv.setText(object.getTitle());
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            orderSummaryPriceTv.setText("$" + String.valueOf(df.format(price)));
        }
    }

    /**
     * This class is responsible for referencing views that show LocationEntries
     */
    public class LocationHolder extends BaseViewHolder<LocationEntry> implements RecyclerView.OnClickListener {

        private TextView nameTv;
        private TextView addressTv;
        private String locationId;

        public LocationHolder(View locationView) {
            super(locationView);
            nameTv = locationView.findViewById(R.id.location_name);
            addressTv = locationView.findViewById(R.id.location_address);
            // Remember to set the click listener
            locationView.setOnClickListener(this);
        }
        @Override
        public void bind(LocationEntry object) {
            nameTv.setText(object.getName());
            addressTv.setText(object.getAddress());
            locationId = object.getLocation_id();
        }

        /**
         * This method overrides the RecyclerView.OnClickListener onClick method.
         * You then pass information to the activity/fragment that implements ClickHandler by using the onClick method
         * @param v is the view that was clicked on
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String testItem = "Clicked on item ";
            Log.d(TAG, "onClick: " + testItem + adapterPosition);
            mClickHandler.onClick(locationId);
        }
    }

    /**
     * Binds the information to the views.
     * @param holder
     * @param position
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bind(mList.get(position));
    }

    /**
     * Determines which ViewHolder to use given the itemViewType
     * @param parent
     * @param viewType is the type of view. Value is given by getItemViewType()
     * @return a view holder
     */
    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ViewType.VIEW_TYPE_ITEMS:
                return new ItemHolder(mInflator.inflate(R.layout.order_summary_view, parent, false));
            case ViewType.VIEW_TYPE_LOCATIONS:
                return new LocationHolder(mInflator.inflate(R.layout.location_view, parent, false));
        }
        Log.d(TAG, "onCreateViewHolder: return null");
        return null;
    }

    @Override
    public int getItemCount() {

        if (mList!=null) {
            Log.d(TAG, "getItemCount: roomRV " + mList.size());
            return mList.size();
        } else {
            return 0;
        }
    }

    /**
     * Determines which viewType the list item is
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: " + mList.get(position).getViewType());
        return mList.get(position).getViewType();
    }

    /**
     * @return the list of objects
     */
    public List<? extends BaseModel> getItemEntries() {
        return mList;
    }

}
