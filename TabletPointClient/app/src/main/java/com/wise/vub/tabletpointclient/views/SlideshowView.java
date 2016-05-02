package com.wise.vub.tabletpointclient.views;

import android.app.ActionBar;
import android.app.Notification;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wise.vub.tabletpointclient.R;
import com.wise.vub.tabletpointclient.TabletPoint;
import com.wise.vub.tabletpointclient.data.MyPath;
import com.wise.vub.tabletpointclient.data.Point;
import com.wise.vub.tabletpointclient.utils.Constants;
import com.wise.vub.tabletpointclient.utils.MyXMLParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Chaun on 4/21/2016.
 */
public class SlideshowView extends View {

    public int getPenStroke() {
        return mPenStroke;
    }

    public void setPenStroke(int mPenStroke) {
        this.mPenStroke = mPenStroke;
    }

    /*
        * 1 is normal pen
        * 2 is eraser
        * 3 is square
        * 4 is circle
        * */
    private int mPenStroke = Constants.PENCIL_MENU_ID;
    private Paint mCustomPaint;
    private MyPath mPath;
    private MyPath mEraserPath;
    private ArrayList<MyPath> mPencilPaths = new ArrayList<>();
    private ArrayList<MyPath> mPenPaths = new ArrayList<>();
    private Handler btHandler;
    private int mColor;
    private float mX, mY;

    public int mCurrentSlide = 1;

    private boolean mStreaming = false;
    // Before image from server comes in. We show this.
    private Drawable mSlideshowDrawable;

    public SlideshowView(Context context) {
        super(context);
        init(null, 0);
    }

    public SlideshowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SlideshowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }
    public void init(AttributeSet attrs, int defStyle) {
        // default image before streaming.
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SlideshowView, defStyle, 0);

        btHandler = ((TabletPoint) getContext()).mBTHandler;

        if (a.hasValue(R.styleable.SlideshowView_slideshowDrawable)) {
            mSlideshowDrawable = a.getDrawable(R.styleable.SlideshowView_slideshowDrawable);
            mSlideshowDrawable.setCallback(this);
        }

        mColor = Color.BLACK;

        // Initiate paint with default size and color
        mCustomPaint = new Paint();
        mCustomPaint.setAntiAlias(true);
        mCustomPaint.setDither(true); // Precise color
        mCustomPaint.setColor(mColor);
        mCustomPaint.setStyle(Paint.Style.STROKE);
        mCustomPaint.setStrokeJoin(Paint.Join.ROUND);
        mCustomPaint.setStrokeCap(Paint.Cap.BUTT);
        mCustomPaint.setStrokeWidth(4f);
    }

    public void setColor(int color) {
        mColor = color;
        mCustomPaint.setColor(mColor);
        if(mStreaming)
            btHandler.sendMessageAtFrontOfQueue(
                    btHandler.obtainMessage(Constants.SEND_SET_COLOR, color)
            );
    }

    public void updateBitmap(Bitmap bitmap) {
        mSlideshowDrawable = new BitmapDrawable(getResources(), bitmap);
        mStreaming = true;
        this.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int contentWidth = getWidth() - 2 * paddingLeft;

        if (mStreaming) {
            // This is shitty because I have to adjust the size manually.
            mSlideshowDrawable.setBounds(0, 0, getWidth(), getHeight());
            mSlideshowDrawable.draw(canvas);

            // Iterate all lines on the image
            for (MyPath path : mPenPaths) {
                if (path.getSlideNumber() == mCurrentSlide) {
                    mCustomPaint.setColor(path.getmColor());
                    canvas.drawPath(path, mCustomPaint);
                }
            }

            for (MyPath path : mPencilPaths) {
                if (path.getSlideNumber() == mCurrentSlide) {
                    mCustomPaint.setColor(path.getmColor());
                    canvas.drawPath(path, mCustomPaint);
                }
            }
        } else {
            mSlideshowDrawable.setBounds(0,0,getWidth(), getHeight());
            mSlideshowDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mStreaming) {
            switch (mPenStroke) {
                case Constants.PENCIL_MENU_ID:
                    handlePencilEvent(event);
                    break;
                // Normal Pen
                case Constants.PEN_MENU_ID:
                    handlePenEvent(event);
                    break;
                // Eraser
                case Constants.ERASER_MENU_ID:
                    handleEraserEvent(event);
                    break;
                // Square
                case Constants.SQUARE_MENU_ID:
                    handleSquareEvent(event);
                    break;
                // Circle
                case Constants.LINE_MENU_ID:
                    handleLineEvent(event);
                    break;
            }
            this.invalidate();
            return true;
        } else {
            return false;
        }
    }

    private void handleLineEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        Log.d("TabletPoint" , "X: " + x + "  Y: " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new MyPath(mCurrentSlide);
                mPath.setmColor(mColor);
                mX = x; mY = y;
                mPenPaths.add(mPath);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_DOWN, new Point(x, y)));
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.rewind();
                mPath.moveTo(mX, mY);
                mPath.lineToEnd(x, y);
                break;
            case MotionEvent.ACTION_UP:
                mPath.rewind();
                mPath.moveTo(mX, mY);
                mPath.lineTo(x, y);
                mPath.endLine();
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_UP, new Point(x, y)));
                break;
        }
        this.invalidate();
    }

    private void handlePencilEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        Log.d("TabletPoint" , "X: " + x + "  Y: " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new MyPath(mCurrentSlide);
                mPath.setmColor(mColor);
                mPath.moveTo(x, y);
                mPencilPaths.add(mPath);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_DOWN, new Point(x, y)));
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_MOVE, new Point(x, y)));
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(x, y);
                mPath.endLine();
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_UP, new Point(x, y)));
                break;
        }
        this.invalidate();
    }

    private void handleSquareEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        Log.d("TabletPoint" , "X: " + x + "  Y: " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new MyPath(mCurrentSlide);
                mPath.setmColor(mColor);
                mX = x; mY = y;
                mPenPaths.add(mPath);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_DOWN, new Point(x, y)));
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.rewind();
                mPath.setmColor(mColor);
                mPath.moveTo(mX, mY);
                mPath.lineToEnd(x, mY);
                mPath.lineTo(x, y);
                mPath.lineTo(mX, y);
                mPath.lineTo(mX, mY);
                break;
            case MotionEvent.ACTION_UP:
                mPath.rewind();
                mPath.setmColor(mColor);
                mPath.moveTo(mX, mY);
                mPath.lineToEnd(x, mY);
                mPath.lineTo(x, y);
                mPath.lineTo(mX, y);
                mPath.lineTo(mX, mY);
                mPath.endLine();
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_MOVE, new Point(x, mY)));
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_MOVE, new Point(x, y)));
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_MOVE, new Point(mX, y)));
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_UP, new Point(mX, mY)));
                break;
        }
        this.invalidate();
    }

    private void handleEraserEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mEraserPath = new MyPath();
                mPath.setmColor(mColor);
                mEraserPath.moveTo(x, y);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_ERASER_DOWN, new Point(x, y)));
                break;
            case MotionEvent.ACTION_MOVE:
                mEraserPath.lineTo(x, y);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_MOVE, new Point(x, y)));
                break;
            case MotionEvent.ACTION_UP:
                mEraserPath.lineTo(x, y);
                mEraserPath.endLine();
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_UP, new Point(x, y)));
                Iterator<MyPath> iterator = mPenPaths.iterator();
                while (iterator.hasNext()) {
                    MyPath path = iterator.next();
                    if (mEraserPath.intersectPath(path)) {
                        iterator.remove();
                        this.invalidate();
                    }
                }
                iterator = mPencilPaths.iterator();
                while (iterator.hasNext()) {
                    MyPath path = iterator.next();
                    if (mEraserPath.intersectPath(path)) {
                        iterator.remove();
                        this.invalidate();
                    }
                }
                break;
        }
    }

    private void handlePenEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        Log.d("TabletPoint" , "X: " + x + "  Y: " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new MyPath(mCurrentSlide);
                mPath.setmColor(mColor);
                mPath.moveTo(x, y);
                mPenPaths.add(mPath);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_DOWN, new Point(x, y)));
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_MOVE, new Point(x, y)));
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(x, y);
                mPath.endLine();
                btHandler.handleMessage(
                        btHandler.obtainMessage(Constants.SEND_PEN_UP, new Point(x, y)));
                break;
        }
        this.invalidate();
    }

    public ArrayList<MyPath> getInkXMl() {
        return mPenPaths;
    }

    // Add annotation to current slide
    public void addAnnotation(ArrayList<MyPath> annotations) {
        for (MyPath path : annotations) {
            path.setSlideNumber(mCurrentSlide);
            mPencilPaths.add(path);
            sendAnnotation(path);
        }
        invalidate();
    }

    // Send the exists annotation towards client;
    public void sendAnnotation(MyPath path) {
        Vector<Point> points = path.getPoints();
        Iterator<Point> iterator = points.iterator();
        Point point = iterator.next();
        btHandler.sendMessageAtFrontOfQueue(
                btHandler.obtainMessage(Constants.SEND_PEN_DOWN, point)
        );
        while (iterator.hasNext()) {
            point = iterator.next();
            if (point.getX() > 0) {
                btHandler.sendMessageAtFrontOfQueue(
                        btHandler.obtainMessage(Constants.SEND_PEN_MOVE, point)
                );
                Log.d("TabletPoint", "AnnotationMovement: " + point.getX() + "," + point.getY());
            }
        }
        btHandler.sendMessageAtFrontOfQueue(
                btHandler.obtainMessage(Constants.SEND_PEN_UP, point)
        );
    }

}
