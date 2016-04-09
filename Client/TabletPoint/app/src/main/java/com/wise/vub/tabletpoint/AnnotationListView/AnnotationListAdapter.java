package com.wise.vub.tabletpoint.AnnotationListView;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.wise.vub.tabletpoint.MyPath;
import com.wise.vub.tabletpoint.ScribbleView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaun on 4/6/2016.
 */
public class AnnotationListAdapter extends ArrayAdapter<MyPath>{

    private float mRatio;
    private float mHeight;

    public AnnotationListAdapter(Context context, int resource, float ratio, float height) {
        super(context, resource);
        mRatio = ratio;
        mHeight = height;
    }

    @Override
    public View getView(int position, View resultView, ViewGroup parent) {
        MyPath path = getItem(position);
        Bitmap tempBitmap = Bitmap.createBitmap(Math.round(mHeight / mRatio), Math.round(mHeight), Bitmap.Config.ARGB_4444);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawARGB(0xff, 0xff, 0xff, 0xff);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);

        tempCanvas.drawPath(path, paint);
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setImageDrawable(new BitmapDrawable(parent.getResources(), tempBitmap));
        imageView.setLayoutParams(new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Log.e("Test", "So far so good");
        return imageView;
    }
}
