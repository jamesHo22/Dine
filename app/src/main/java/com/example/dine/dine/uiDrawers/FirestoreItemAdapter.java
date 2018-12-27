package com.example.dine.dine.uiDrawers;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dine.dine.Item;
import com.example.dine.dine.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

public class FirestoreItemAdapter extends FirestoreRecyclerAdapter<Item, FirestoreItemAdapter.FirestoreItemHolder> {

    private onItemClickListener mListener;
    private int mViewStyle;
    public static final int MENU_ORDERING_STYLE = 1;
    public static final int ORDER_SUMMARY_STYLE = 2;
    private final static String TAG = FirestoreItemAdapter.class.getSimpleName();

    public FirestoreItemAdapter(@NonNull FirestoreRecyclerOptions<Item> options, int viewStyle) {
        super(options);
        mViewStyle = viewStyle;
    }

    /**
     * Binds the data from the database to the views in the card
     * @param holder
     * @param position
     * @param model
     */
    @Override
    protected void onBindViewHolder(@NonNull FirestoreItemHolder holder, int position, @NonNull Item model) {
        Log.d(TAG, "onBindViewHolder: " + mViewStyle);

        switch (mViewStyle){
            case MENU_ORDERING_STYLE:
                Log.d(TAG, "onBindViewHolder: MENU_ORDERING_STYLE");
                holder.titleTv.setText(model.getTitle());
                holder.descriptionTv.setText(model.getDescription());
                float price = model.getPrice();
                holder.priceTv.setText("$ " + String.valueOf(price/100));
                // Check if a image url exists for the item
                if (model.getImageUri() != null) {

                    Log.v("Get Image", String.valueOf(model.getImageUri()));
                    // Get context for picasso
                    Context context = holder.mainIV.getContext();
                    Uri imageUri = Uri.parse(model.getImageUri());
                    // Loads the image URI from that document into the imageView.
                    Picasso.with(context).load(imageUri).fit().centerCrop().into(holder.mainIV);
                }
                break;

            case ORDER_SUMMARY_STYLE:
                Log.d(TAG, "onBindViewHolder: ORDER_SUMMARY_STYLE");
                holder.titleTv.setText(model.getTitle());
                break;

            default:
                Log.d(TAG, "onBindViewHolder: DEFAULT_STYLE");
                holder.titleTv.setText(model.getTitle());
                holder.descriptionTv.setText(model.getDescription());
                float priceDefault = model.getPrice();
                holder.priceTv.setText("$ " + String.valueOf(priceDefault/100));
                // Check if a image url exists for the item
                if (model.getImageUri() != null) {

                    Log.v("Get Image", String.valueOf(model.getImageUri()));
                    // Get context for picasso
                    Context context = holder.mainIV.getContext();
                    Uri imageUri = Uri.parse(model.getImageUri());
                    // Loads the image URI from that document into the imageView.
                    Picasso.with(context).load(imageUri).fit().centerCrop().into(holder.mainIV);
                }
                break;
        }
    }

    /**
     * Inflates different views depending on the ViewStyle
     */
    @NonNull
    @Override
    public FirestoreItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (mViewStyle) {
            case MENU_ORDERING_STYLE:
                View menuOrderingStyle = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
                Log.d(TAG, "onCreateViewHolder: MENU_ORDERING_STYLE");
                return new FirestoreItemHolder(menuOrderingStyle);

            case ORDER_SUMMARY_STYLE:
                View summaryStyle = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_summary_view, parent, false);
                Log.d(TAG, "onCreateViewHolder: ORDER_SUMMARY_STYLE");
                return new FirestoreItemHolder(summaryStyle);

            default:
                View defaultstyle= LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
                Log.d(TAG, "onCreateViewHolder: DEFAULT_STYLE");
                return new FirestoreItemHolder(defaultstyle);
        }
    }

    /**
     * obtains references to the views in the card
     */
    class FirestoreItemHolder extends RecyclerView.ViewHolder {

        //public TextView mTextView;
        TextView titleTv;
        ImageView mainIV;
        TextView priceTv;
        TextView descriptionTv;

        public FirestoreItemHolder(final View itemView) {
            super(itemView);

            switch (mViewStyle) {
                case MENU_ORDERING_STYLE:
                    //This references the textView on the card
                    titleTv = itemView.findViewById(R.id.info_text);
                    mainIV = itemView.findViewById(R.id.main_image_view);
                    descriptionTv = itemView.findViewById(R.id.description);
                    priceTv = itemView.findViewById(R.id.price);
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && mListener != null) {
                                mListener.onItemClick(getSnapshots().getSnapshot(position), position, itemView);
                            }
                        }
                    });
                    break;


                case ORDER_SUMMARY_STYLE:
                    //This references the textView on the card
                    titleTv = itemView.findViewById(R.id.order_summary_title);
//                    mainIV = itemView.findViewById(R.id.main_image_view);
//                    descriptionTv = itemView.findViewById(R.id.description);
//                    priceTv = itemView.findViewById(R.id.price);
//                    itemView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            int position = getAdapterPosition();
//                            if (position != RecyclerView.NO_POSITION && mListener != null) {
//                                mListener.onItemClick(getSnapshots().getSnapshot(position), position, itemView);
//                            }
//                        }
//                    });
                    break;

                default:
                    //This references the textView on the card
                    titleTv = itemView.findViewById(R.id.info_text);
                    mainIV = itemView.findViewById(R.id.main_image_view);
                    descriptionTv = itemView.findViewById(R.id.description);
                    priceTv = itemView.findViewById(R.id.price);
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && mListener != null) {
                                mListener.onItemClick(getSnapshots().getSnapshot(position), position, itemView);
                            }
                        }
                    });
                    break;

            }
        }
    }

    /**
     * send data from the adapter to the underlying activity that implements the interface below
     */
    public interface onItemClickListener {
        // Change this to change kind of data you want to send to activity
        void onItemClick(DocumentSnapshot documentSnapshot, int position, View itemView);
    }

    public void setOnItemClickListener(onItemClickListener listener) {

        this.mListener = listener;

    }

    /**
     * Deletes document from fireStore given position
     * @param position pass in recyclerView position of the document that is to be deleted
     */
    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
        // Firestore recyclerview automatically detects data changes. No need to call notifyDataChanged().
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
