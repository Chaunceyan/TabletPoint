package com.wise.vub.tabletpoint;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Chaun on 3/26/2016.
 */
public class SlidesPreviewAdapter extends RecyclerView.Adapter<SlidesPreviewAdapter.ViewHolder>{
    private Bitmap[] mDataset;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public ViewHolder(ImageView imageView) {
            super(imageView);
            mImageView = imageView;
        }
    }
    public SlidesPreviewAdapter(Bitmap[] dataSet) {
        mDataset = dataSet;
    }

    @Override
    public SlidesPreviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view_slides, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SlidesPreviewAdapter.ViewHolder holder, int position) {
        holder.mImageView.setImageBitmap(mDataset[position]);
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
