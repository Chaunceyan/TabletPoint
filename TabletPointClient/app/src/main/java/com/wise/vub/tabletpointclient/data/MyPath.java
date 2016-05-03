package com.wise.vub.tabletpointclient.data;

import android.graphics.Color;
import android.graphics.Path;
import android.util.Log;

import com.wise.vub.tabletpointclient.TabletPoint;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Chaun on 4/27/2016.
 */
public class MyPath extends Path {

    private Vector<Point> mPoints = new Vector<>();
    private float mCurX = 0, mCurY = 0;

    public int getmColor() {
        return mColor;
    }

    public void setmColor(int mColor) {
        this.mColor = mColor;
    }

    private int mColor;

    public int getSlideNumber() {
        return mSlideNumber;
    }

    public void setSlideNumber(int mSlideNumber) {
        this.mSlideNumber = mSlideNumber;
    }

    private int mSlideNumber;

    public MyPath(int slideNumber) {
        mSlideNumber = slideNumber;
        mColor = Color.BLACK;
    }

    public MyPath() {
        mSlideNumber = 0;
        mColor = Color.BLACK;
    }

    public Vector<Point> getPoints() {
        return mPoints;
    }

    @Override
    public void moveTo(float x, float y) {
        super.moveTo(x, y);
        mCurX = x; mCurY = y;
        Point point = new Point(x, y);
        mPoints.add(point);
    }

    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
        super.moveTo(x, y);
        Point point = new Point(x, y);
        mPoints.add(point);
    }

    @Override
    public void rewind() {
        super.rewind();
        mPoints.clear();
    }

    public void lineToEnd(float x, float y) {
        super.lineTo(x, y);
        mPoints.clear();
        Point point = new Point(x, y);
        mPoints.add(point);
    }

    public void endLine() {
        Point point = new Point(-10, -10);
        mPoints.add(point);
    }

    public boolean intersectPath(MyPath path) {
        Vector<Point> points = path.getPoints();
        Iterator<Point> iterator1 = points.iterator();
        Point a1 = iterator1.next();
        while (iterator1.hasNext()) {
            Point b1 = iterator1.next();
            if (b1.getNormalX() > 0) {
                Iterator<Point> iterator2 = mPoints.iterator();
                Point a2 = iterator2.next();
                while (iterator2.hasNext()) {
                    Point b2 = iterator2.next();
                    Log.d("TabletPoint", b2.getNormalX() + "," + b2.getNormalY());
                    if (b2.getNormalX() > 0) {
                        if (intersectLine(a1.getNormalX(), a1.getNormalY(), b1.getNormalX(), b1.getNormalY(), a2.getNormalX(), a2.getNormalY(), b2.getNormalX(), b2.getNormalY())) {
                            Log.d("TabletPoint", "intersectPath: " + a1.getNormalX() + "," + a1.getNormalY() + "," + b1.getNormalX() + "," + b1.getNormalY() + "," + a2.getNormalX() + "," + a2.getNormalY() + "," + b2.getNormalX() + "," + b2.getNormalY());
                            return true;
                        }
                        a2 = b2;
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
            a1 = b1;
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

}
