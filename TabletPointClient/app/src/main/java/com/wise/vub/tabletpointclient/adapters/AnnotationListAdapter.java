package com.wise.vub.tabletpointclient.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.wise.vub.tabletpointclient.R;
import com.wise.vub.tabletpointclient.data.MyPath;
import com.wise.vub.tabletpointclient.views.SlideshowView;

import java.util.ArrayList;

/**
 * Created by Chaun on 4/27/2016.
 */
public class AnnotationListAdapter extends ArrayAdapter<ArrayList<MyPath>> {

    private float width, height;

    public AnnotationListAdapter(Context context, int resource, float width, float height) {
        super(context, resource);
        this.width = width;
        this.height = height;
    }

    @Override
    public View getView(final int position, View resultView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) getContext())
                .getLayoutInflater();
        SwipeLayout viewGroup = (SwipeLayout) inflater.inflate(R.layout.item_annotation_list, null);
        ArrayList<MyPath> paths = getItem(position);
        Bitmap tempBitmap = Bitmap.createBitmap(Math.round(width), Math.round(height), Bitmap.Config.ARGB_4444);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawARGB(0xff, 0xff, 0xff, 0xff);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);

        for (MyPath path : paths) {
            tempCanvas.drawPath(path, paint);
        }

        ImageView imageView = (ImageView) viewGroup.findViewById(R.id.image_view_annotation);
        imageView.setImageDrawable(new BitmapDrawable(parent.getResources(), tempBitmap));

        ImageView trash = (ImageView) viewGroup.findViewById(R.id.image_view_trash);
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(getItem(position));
            }
        });

        viewGroup.setShowMode(SwipeLayout.ShowMode.LayDown);
        viewGroup.addDrag(SwipeLayout.DragEdge.Left, viewGroup.findViewById(R.id.linear_layout_annotation_bottom));

        viewGroup.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

        return viewGroup;
    }



}

