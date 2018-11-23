package com.example.dine.dine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<String> mItemEntries;

    public ItemAdapter(ItemAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    // Used for onClick for recyclerview
    private final ItemAdapterOnClickHandler mClickHandler;
    // define an interface for handling onClickViews

    // Used for onClick for recyclerview
    public interface ItemAdapterOnClickHandler {
        // Must implement this method
        void onClick(String testItem);
    }

    // Provide a reference to the views for each item. Implements the OnClickListener
    public class ItemViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener {
        //public TextView mTextView;
        TextView titleTv;
        TextView cardNumberTv;
        ImageView mainIV;
        TextView descriptionTv;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //This references the textView on the card
            titleTv = itemView.findViewById(R.id.info_text);
            cardNumberTv = itemView.findViewById(R.id.card_number);
            mainIV = itemView.findViewById(R.id.main_image_view);
            descriptionTv = itemView.findViewById(R.id.description);
            itemView.setOnClickListener(this);
        }

        // Override OnClick for OnClickListener
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String testItem = "Clicked on item ";
            // Call the onClick method in the ClickHandler
            mClickHandler.onClick(testItem + String.valueOf(adapterPosition));
        }
    }
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creates new views (invoked by the layout manager)
        Context context = parent.getContext();
        // this references the layout of the card view
        int layoutIdForMenuItem = R.layout.card_view;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForMenuItem, parent, shouldAttachToParentImmediately);
        ItemViewHolder viewHolder = new ItemViewHolder(view);
        return viewHolder;
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position) {
        // FIXME: get the names of the items from FireStore
        holder.titleTv.setText("Get title from firestore");
        holder.cardNumberTv.setText(String.valueOf(position));
        holder.mainIV.setImageResource(R.mipmap.big_mac);
        holder.descriptionTv.setText("Get description from firestore");
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mItemEntries == null) {
            Log.v("RecyclerView", "mMenuItems is null");
            return 5;
        }
        return mItemEntries.size();
    }
}
