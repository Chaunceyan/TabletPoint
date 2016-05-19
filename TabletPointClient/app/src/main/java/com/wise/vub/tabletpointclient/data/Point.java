package com.wise.vub.tabletpointclient.data;

/**
 * Created by Chaun on 4/30/2016.
 */
public class Point {

    public static int width, height;

    public Point(float x, float y) {
        this.x = x/width;
        this.y = y/height;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getNormalX() {
        return x * width;
    }

    public float getNormalY() {
        return y * height;
    }

    private float x, y;

    public void offset(float x, float y) {
        this.x = this.x + x/width;
        this.y = this.y + y/width;
    }
}
