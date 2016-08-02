package com.crypta.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crypta.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by D064343 on 14.07.2016.
 */
public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder> {
    private final Callback mCallback;
    private List<Provider> mFiles;

    public ProviderAdapter(Picasso picasso, Callback callback) {
        mCallback = callback;
    }

    public void setFiles(List<Provider> files) {
        mFiles = Collections.unmodifiableList(new ArrayList<>(files));
        notifyDataSetChanged();
    }

    @Override
    public ProviderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.provider_item, viewGroup, false);
        return new ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProviderViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(mFiles.get(i));
    }

    @Override
    public long getItemId(int position) {
        return mFiles.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    public interface Callback {
        void onItemClicked(Provider provider);
    }

    public class ProviderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final ImageView mImageView;
        private Provider mItem;

        public ProviderViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.providerImage);
            mTextView = (TextView) itemView.findViewById(R.id.providerText);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


            mCallback.onItemClicked(mItem);

        }

        public void bind(Provider item) {
            mItem = item;
            mTextView.setText(mItem.getTitle());

            if (mItem.getTitle().equals("Dropbox")) {
                mImageView.setImageResource(R.drawable.dropbox_logo);
            }

        }
    }
}
