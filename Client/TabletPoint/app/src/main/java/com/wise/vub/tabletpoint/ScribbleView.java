package com.wise.vub.tabletpoint;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.wise.vub.tabletpoint.AnnotationListView.AnnotationListAdapter;
import com.wise.vub.tabletpoint.util.ClientConstants;
import com.wise.vub.tabletpoint.util.Constants;
import com.wise.vub.tabletpoint.util.PenStrokes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * TODO: document your custom view class.
 */
public class ScribbleView extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;
    public int mPenStroke;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    public static Paint mPaint;
    private Paint eraserPaint;
    private boolean mStreamingFlag;
    public ArrayList<MyPath> mPaths = new ArrayList<>();
    public ArrayList<MyPath> mPermanentPaths = new ArrayList<>();
    private MyPath mPath;
    private MyPath mEraserPath;
    public static float mImageWidth, mImageHeight;
    public static AnnotationListAdapter annotationListAdapter;

    public int mSlideNumber = 1;

    public ScribbleView(Context context) {
        super(context);
        init(null, 0);
    }

    public ScribbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ScribbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ScribbleView, defStyle, 0);

        mSlideNumber = 1;

        mStreamingFlag = false;

        mExampleString = a.getString(
                R.styleable.ScribbleView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.ScribbleView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.ScribbleView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.ScribbleView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.ScribbleView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPenStroke = PenStrokes.NORMAL_PEN;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4f);

        eraserPaint = new Paint();
        eraserPaint.setColor(Color.RED);
        eraserPaint.setStyle(Paint.Style.STROKE);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    public void updateBitmap (Bitmap newImage) {
        mExampleDrawable = new BitmapDrawable(getResources(), newImage);
        mStreamingFlag = true;
        this.invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            if (!mStreamingFlag) {
                // Draw the text.
                mExampleDrawable.setBounds(paddingLeft-15, paddingTop,
                        paddingLeft + contentWidth+15, paddingTop + contentHeight);
                mExampleDrawable.draw(canvas);
                canvas.drawText(mExampleString,
                        paddingLeft + (contentWidth - mTextWidth) / 2,
                        paddingTop + (contentHeight + mTextHeight) / 2,
                        mTextPaint);
            } else {
                mImageHeight = this.getHeight();
                mImageWidth = mExampleDrawable.getIntrinsicWidth() * this.getHeight()/ mExampleDrawable.getIntrinsicHeight();
                mExampleDrawable.setBounds(Math.round(this.getWidth() / 2 - mImageWidth / 2), paddingTop,
                        Math.round(this.getWidth() / 2 + mImageWidth / 2),
                        Math.round(paddingTop + mImageHeight));
                mExampleDrawable.draw(canvas);
                Iterator<MyPath> ite = mPaths.iterator();
                while (ite.hasNext()) {
                    MyPath path = ite.next();
                    if (path.getmSlideNumber() == mSlideNumber)
                    canvas.drawPath(path, path.getPaint());
                }
                if (mEraserPath != null) {
                    if (mEraserPath.getmSlideNumber() == mSlideNumber)
                    canvas.drawPath(mEraserPath, eraserPaint);
                }
            }
        }
    }

    public float getLeftPadding() {
        return (this.getWidth() - mImageWidth)/2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float leftPadding = (this.getWidth() - mImageWidth) / 2;
        float x = event.getX(), y = event.getY();
        Log.d("TouchEventChauncey", "Touch Point: " + x + "," + y);
        if (x >= leftPadding && y >= 0 && x <= leftPadding + mImageWidth && y <= mImageHeight) {
            ConnectionService service = ((PresentationFragment) ((Activity) this.getContext())
                    .getFragmentManager().findFragmentById(R.id.fragment_presentation_image))
                    .mConnectionService;
           Log.d("TouchEventChauncey", "View Size: " + mImageWidth + "," + mImageHeight);
           switch (mPenStroke) {
               case PenStrokes.NORMAL_PEN:
               case PenStrokes.PERMANENT_PEN:
                   switch (action) {
                       case MotionEvent.ACTION_DOWN:
                           touchStart(x, y);
                           Log.d("TouchEventChauncey", "Start" + String.valueOf(x) + String.valueOf(y));
                           service.write(Constants.PENDOWN
                                   + "," + produceOutput(x, y, leftPadding)
                                   + "," + ClientConstants.PEN +"\r\n");
                           break;
                       case MotionEvent.ACTION_MOVE:
                           touchMove(x, y);
                           Log.d("TouchEventChauncey", "Move" + String.valueOf(x) + String.valueOf(y));
                           service.write(Constants.PENMOVE
                                   + "," + produceOutput(x, y, leftPadding) + "\r\n");
                           Log.d("TouchEventChauncey", "onTouchEvent: "
                                   + (x - leftPadding) / mImageWidth
                                   + "," + Float.valueOf(y) / mImageHeight);
                           break;
                       case MotionEvent.ACTION_UP:
                           touchEnd(x, y);
                           Log.d("TouchEventChauncey", "End" + String.valueOf(x) + String.valueOf(y));
                           service.write(Constants.PENUP
                                   + "," + produceOutput(x, y, leftPadding) + "\r\n");
                   }
                   break;
               case PenStrokes.ERASER:
                   switch (action) {
                       case MotionEvent.ACTION_DOWN:
                           eraserPathStart(x, y);
                           service.write(Constants.PENDOWN
                                   + "," + produceOutput(x, y, leftPadding)
                                   + "," + ClientConstants.ERASER + "\r\n");
                           break;
                       case MotionEvent.ACTION_MOVE:
                           eraserPathMove(x, y);
                           service.write(Constants.PENMOVE
                                   + "," + produceOutput(x, y, leftPadding)
                                   + "\r\n");
                           break;
                       case MotionEvent.ACTION_UP:
                           eraserPathEnd(x, y);
                           service.write(Constants.PENUP
                                   + "," + produceOutput(x, y, leftPadding)
                                   + "\r\n");
                           break;
                   }
           }
            this.invalidate();
        }
        return true;
    }

    private String produceOutput(float x, float y, float leftPadding) {
        return (x - leftPadding)/mImageWidth + "," + y/mImageHeight;
    }

    private void eraserPathStart(float x, float y) {
        Log.d("TouchEventChauncey", "Eraser Mode start");
        mEraserPath = new MyPath(mSlideNumber);
        mEraserPath.moveTo(x, y);
    }

    private void eraserPathMove(float x, float y) {
        Log.d("TouchEventChauncey", "Eraser Mode Move");
        mEraserPath.lineTo(x, y);
    }

    private void eraserPathEnd(float x, float y) {
        mEraserPath.lineTo(x, y);
        mEraserPath.endLine();
        Iterator<MyPath> ite = mPaths.iterator();
        while (ite.hasNext()){
            MyPath path = ite.next();
            if (mEraserPath.intersectPath(path)) {
                Log.d("TouchEventChauncey", "Intersect Detected!!!!");
                ite.remove();
            }
        }
        mEraserPath = null;
    }

    private void touchStart (float x, float y) {

        mPath = new MyPath(mSlideNumber);
        switch (mPenStroke) {
            case PenStrokes.PERMANENT_PEN:
                mPermanentPaths.add(mPath);
                mPaths.add(mPath);
                break;
            case PenStrokes.NORMAL_PEN:
                mPaths.add(mPath);
        }
        mPath.moveTo(x, y);
    }

    private void touchMove (float x, float y) {
        mPath.lineTo(x, y);
    }

    private void touchEnd (float x, float y) {
        mPath.lineTo(x, y);
        mPath.endLine();
    }

    public void addPath (MyPath path) {
        path.setmSlideNumber(mSlideNumber);
//        path.offset((this.getWidth() - ScribbleView.mImageWidth) / 2, 0);
        sendPath(path);
        mPaths.add(path);
    }

    public void sendPath (MyPath path) {

        ConnectionService service = ((PresentationFragment) ((Activity) this.getContext())
                .getFragmentManager().findFragmentById(R.id.fragment_presentation_image))
                .mConnectionService;
        Vector<Float> points = path.getPoints();
        Iterator<Float> ite = points.iterator();
        while (ite.hasNext()) {
            float x = ite.next();
            float y = ite.next();
            service.write(Constants.PENDOWN
                    + "," + x/mImageWidth
                    + "," + y/mImageHeight
                    + "," + ClientConstants.PEN + "\r\n");
            Log.d("Sending Path", x +  "  , " + y);
            while (ite.hasNext()) {
                float tempX = ite.next();
                float tempY = ite.next();
                Log.d("Sending Path", x +  "  , " + y);
                if (tempX != -1) {
                    x = tempX;
                    y = tempY;
                    service.write(Constants.PENMOVE
                            + "," + x/mImageWidth
                            + "," + y/mImageHeight
                            + "\r\n");
                } else {
                    service.write(Constants.PENUP
                            + "," + x/mImageWidth
                            + "," + y/mImageHeight
                            + "\r\n");
                    break;
                }
            }
        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
