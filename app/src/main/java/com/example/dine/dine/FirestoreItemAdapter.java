package com.example.dine.dine;

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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

public class FirestoreItemAdapter extends FirestoreRecyclerAdapter<Item, FirestoreItemAdapter.FirestoreItemHolder> {

    public FirestoreItemAdapter(@NonNull FirestoreRecyclerOptions<Item> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FirestoreItemHolder holder, int position, @NonNull Item model) {

        holder.titleTv.setText(model.getTitle());
        holder.descriptionTv.setText(model.getDescription());
        holder.priceTv.setText(String.valueOf(model.getPrice()));
        // Check if a image url exists for the item
        if (model.getImageUri() != null) {

            Log.v("Get Image", String.valueOf(model.getImageUri()));
            // Get context for picasso
            Context context = holder.mainIV.getContext();
            Uri imageUri = Uri.parse(model.getImageUri());
            Picasso.with(context).load(imageUri).fit().centerCrop().into(holder.mainIV);
        }
    }

    @NonNull
    @Override
    public FirestoreItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new FirestoreItemHolder(v);
    }

    class FirestoreItemHolder extends RecyclerView.ViewHolder {

        //public TextView mTextView;
        TextView titleTv;
        TextView cardNumberTv;
        ImageView mainIV;
        TextView priceTv;
        TextView descriptionTv;

        public FirestoreItemHolder(View itemView) {
            super(itemView);

            //This references the textView on the card
            titleTv = itemView.findViewById(R.id.info_text);
            mainIV = itemView.findViewById(R.id.main_image_view);
            descriptionTv = itemView.findViewById(R.id.description);
            priceTv = itemView.findViewById(R.id.price);
            //FIXME: redo onclicklistener
            //itemView.setOnClickListener(this);
        }
    }
}
