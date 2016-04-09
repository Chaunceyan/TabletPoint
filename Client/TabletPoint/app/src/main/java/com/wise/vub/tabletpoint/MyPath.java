package com.wise.vub.tabletpoint;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import com.wise.vub.tabletpoint.util.ClientConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

/**
 * Created by Chaun on 4/1/2016.
 */
public class MyPath extends Path {
    private Vector<Float> mPoints;

    public int getmSlideNumber() {
        return mSlideNumber;
    }

    public void setmSlideNumber(int mSlideNumber) {
        this.mSlideNumber = mSlideNumber;
    }

    private int mSlideNumber;

    private float mCurX, mCurY;

    public Paint getPaint() {
        return mPaint;
    }

    private Paint mPaint;

    public MyPath(int slideNumber) {
        mPoints = new Vector<Float>();
        mCurX = mCurY = 0;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mSlideNumber = slideNumber;
    }

    public MyPath() {
        mPoints = new Vector<Float>();
        mCurX = mCurY = 0;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mSlideNumber = 0;
    }

    public Vector<Float> getPoints() {
        return mPoints;
    }

    public boolean intersectPath(MyPath path) {
        Vector<Float> points = path.getPoints();
        int length1 = mPoints.size();
        for (int mIndex = 0; mIndex < mPoints.size()-4; mIndex += 2) {
            float mX1 = mPoints.get(mIndex);
            float mY1 = mPoints.get(mIndex+1);
            if (mPoints.get(mIndex+2) != -1) {
                float mX2 = mPoints.get(mIndex+2);
                float mY2 = mPoints.get(mIndex+3);
                for (int index = 0; index < points.size()-4; index += 2) {
                    float x1 = points.get(index);
                    float y1 = points.get(index + 1);
                    if (points.get(index+2) != -1) {
                        float x2 = points.get(index + 2);
                        float y2 = points.get(index + 3);
                        if (intersectLine(mX1, mY1, mX2, mY2, x1, y1, x2, y2)) {
                            Log.d("Weird Bug", "Firse line: " + mX1 + "," + mY1 + "," + mX2 + "," + mY2 + "," + x1 + "," + y1 + "," + x2 + "," + y2);
                            return true;
                        }
                    } else {
                        index += 2;
                    }
                }
            } else {
                mIndex += 2;
            }
        }
        return false;
    }

    // I would call this magic. Don't touch if you didn't really understand.
    public boolean intersectLine(float x1, float y1, float x2, float y2,
                                 float x3, float y3, float x4, float y4) {
        float a1, a2, a3, a4;
        if((a1 = area(x1, y1, x2, y2, x3, y3)) == 0) {
            if (between(x1, y1, x2, y2, x4, y4)) {
                return true;
            } else {
                if (area(x1, y1, x2, y2, x4, y4) == 0) {
                    return between(x3, y3, x4, y4, x1, y1)
                            || between(x3, y3, x4, y4, x2, y2);
                } else {
                    return false;
                }
            }
        } else if ((a2 = area(x1, y1, x2, y2, x4, y4)) == 0) {
            return between(x1, y1, x2, y2, x4, y4);
        }

        if ((a3 = area(x3, y3, x4, y4, x1, y1)) == 0) {
            if (between(x3, y3, x4, y4, x1, y1)) {
                return true;
            } else {
                if ((area(x3, y3, x4, y4, x2, y2)) == 0) {
                    return between(x1, y1, x2, y2, x3, y3)
                            || between(x1, y1, x2, y2, x4, y4);
                } else {
                    return false;
                }
            }
        } else if ((a4 = area(x3, y3, x4, y4, x2, y2)) == 0) {
            return between(x3, y3, x4, y4, x2, y2);
        } else {
            return ((a1 > 0.0) ^ (a2 > 0.0)) && ((a3 > 0.0) ^ (a4 > 0.0));
        }
    }

    private static float area(float x1, float y1,
                              float x2, float y2,
                              float x3, float y3) {
        return (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);
    }

    private static boolean between(float x1, float y1,
                                  float x2, float y2,
                                  float x3, float y3) {
        if(x1 != x2) {
            return (x1 <= x3 && x3 <= x2) || (x1 >= x3 && x3 >= x2);
        } else {
            return (y1 <= y3 && y3 <= y2) || (y1 >= y3 && y3 >= y2);
        }
    }

    @Override
    public void moveTo(float x, float y) {
        super.moveTo(x, y);
        mCurX = x; mCurY = y;
        mPoints.add(mCurX); mPoints.add(mCurY);
    }

    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
        super.moveTo(x, y);
        mPoints.add(x); mPoints.add(y);
        mCurX = x; mCurY = y;
    }

    public void endLine() {
        mPoints.add((float) -1);
        mPoints.add((float) -1);
    }
}
