package com.wise.vub.tabletpoint.RecyclerView;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.vub.tabletpoint.ConnectionService;
import com.wise.vub.tabletpoint.PresentationFragment;
import com.wise.vub.tabletpoint.R;
import com.wise.vub.tabletpoint.ScribbleView;
import com.wise.vub.tabletpoint.util.Constants;

/**
 * Created by Chaun on 3/26/2016.
 */
public class SlidesPreviewAdapter extends RecyclerView.Adapter<SlidesPreviewAdapter.ViewHolder>{
    private Bitmap[] mDataset;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mCompoundView;
        public ViewHolder(LinearLayout compoundView) {
            super(compoundView);
             compoundView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     ConnectionService service = ((PresentationFragment) ((Activity) mContext).getFragmentManager().findFragmentById(R.id.fragment_presentation_image)).mConnectionService;
                     service.write(Constants.GOTO_SLIDE + "," + (getAdapterPosition()+1) + "\r\n");
                     ((ScribbleView)((Activity)mContext).findViewById(R.id.customized_view_scribble)).mSlideNumber = getAdapterPosition() + 1;
                 }
             });
            mCompoundView = compoundView;
        }
    }
    public SlidesPreviewAdapter(Context context, Bitmap[] dataSet) {
        mContext = context;
        mDataset = dataSet;
    }

    @Override
    public SlidesPreviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout compoundView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.compound_view_slides, parent, false);
        return new ViewHolder(compoundView);
    }

    @Override
    public void onBindViewHolder(SlidesPreviewAdapter.ViewHolder holder, int position) {
        ImageView imageView = (ImageView) holder.mCompoundView.findViewById(R.id.image_view_preview);
        imageView.setImageBitmap(mDataset[position]);
        imageView.setSelected(true);
        TextView textView = (TextView) holder.mCompoundView.findViewById(R.id.text_view_number);
        textView.setText("Slide " + (position+1));
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

//    private class MyViewHolder extends ViewHolder implements View.OnClickListener {
//        public ImageView mImageView;
//
//        public MyViewHolder(LinearLayout compoundView) {
//            super(compoundView);
//            mImageView = (ImageView) compoundView.findViewById(R.id.image_view_preview);
//            mImageView.setOnClickListener(this);
//        }
//
//
//        @Override
//        public void onClick(View v) {
//        }
//    }
}
