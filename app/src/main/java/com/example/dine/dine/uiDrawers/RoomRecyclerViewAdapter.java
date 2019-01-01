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
import com.example.dine.dine.RoomDb.ItemEntry;

import java.text.DecimalFormat;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.RoomViewHolder> {

    private List<ItemEntry> mItemEntries;
    private View.OnClickListener mClickListener;

    public RoomRecyclerViewAdapter(List<ItemEntry> itemEntries) {
        mItemEntries = itemEntries;
        //mClickListener = ClickListener;
    }

    /**
     * Inflates the recycled views
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // This method is in charge of creating new views
        Context context = parent.getContext();
        // The following gets a reference to the layout to inflate when more views are needed
        int layoutIdForMenuItem = R.layout.order_summary_view;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForMenuItem, parent, shouldAttachToParentImmediately);
        RoomViewHolder viewHolder = new RoomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        ItemEntry item = mItemEntries.get(position);
        float price = (float)item.getPrice()/100;
        Log.d(TAG, "onBindViewHolder: " + price);
        holder.orderSummaryTitleTv.setText(item.getTitle());
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        holder.orderSummaryPriceTv.setText("$" + String.valueOf(df.format(price)));
    }

    @Override
    public int getItemCount() {
        if (mItemEntries == null) {
            Log.v("RecyclerView", "mItemEntries is null");
            return 0;
        }
        return mItemEntries.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder {

        private TextView orderSummaryTitleTv;
        private TextView orderSummaryDescriptionTv;
        private TextView orderSummaryPriceTv;

        public RoomViewHolder(View itemView) {
            super(itemView);
            orderSummaryTitleTv = itemView.findViewById(R.id.order_summary_title);
            orderSummaryDescriptionTv = itemView.findViewById(R.id.order_summary_description);
            orderSummaryPriceTv = itemView.findViewById(R.id.order_summary_price);
        }
    }

    public void addItems(List<ItemEntry> itemEntries) {
        this.mItemEntries = itemEntries;
        notifyDataSetChanged();
    }

    public List<ItemEntry> getItemEntries() {
        return mItemEntries;
    }
}
