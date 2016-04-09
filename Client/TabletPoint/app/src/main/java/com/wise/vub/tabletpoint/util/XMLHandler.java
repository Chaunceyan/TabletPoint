package com.wise.vub.tabletpoint.util;

import android.graphics.Path;
import android.sax.EndElementListener;
import android.sax.StartElementListener;
import android.util.Log;
import android.util.Xml;

import com.wise.vub.tabletpoint.MyPath;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Chaun on 4/4/2016.
 */
public class XMLHandler {
    public static ArrayList<MyPath> pathsFrom(String xmlStr, float ratio, float height, float leftPadding) {
        float width = height/ratio;

        try {
            InputStream in = new ByteArrayInputStream(xmlStr.getBytes());
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            ArrayList<MyPath> paths = new ArrayList<MyPath>();
            MyPath path = new MyPath();
            boolean bPointer = false;
            boolean bPoints = false;
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                int event = parser.next();
                String startName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (startName.equalsIgnoreCase("Paths")) {
                            path = new MyPath();
                        } else if (startName.equalsIgnoreCase("Pointer")) {
                            bPointer = true;
                        } else if (startName.equalsIgnoreCase("Points")) {
                            bPoints = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (bPointer) {
                            // Future extend;
                            bPointer = false;
                        } else if (bPoints) {
                            if (bPointer) {
                                // Future extend;
                                bPointer = false;
                            } else if (bPoints) {
                                bPoints = false;
                                String pointsStr = parser.getText();
                                Log.d("XML Parser", pointsStr);
                                String[] ponits = pointsStr.split(",");
                                ArrayList<String> pointsArray = new ArrayList<String>(Arrays.asList(ponits));
                                Iterator<String> ite = pointsArray.iterator();
                                if (ite.hasNext()) {
                                    String mX = ite.next();
                                    String mY = ite.next();
                                    Log.d("XML Parser",mX + mY);
                                    float x = Float.valueOf(ite.next()) * width;
                                    float y = Float.valueOf(ite.next()) * height;
                                    path.moveTo(x, y);
                                    while (ite.hasNext()) {
                                        x = Float.valueOf(ite.next()) * width;
                                        y = Float.valueOf(ite.next()) * height;
                                        path.lineTo(x, y);
                                    }
                                }
                                Log.d("XML Parser", path.getPoints().toString());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (startName.equalsIgnoreCase("Paths")) {
                            path.offset(leftPadding, 0);
                            paths.add(path);
                        } else if (startName.equalsIgnoreCase("Path")) {
                            path.endLine();
                        }
                        break;
                }
            }
            return paths;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }
    public static String pathsToXML (ArrayList<MyPath> paths, float width, float height, float leftPadding) {
        if (paths.size() == 0) return null;
        Iterator<MyPath> iterator = paths.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        int slideNumber = -1;
        stringBuilder.append("<WISEInkXML>");
        stringBuilder.append("<Paths>");
        Float x,y;
        while (iterator.hasNext()) {
            MyPath path = iterator.next();
            Vector<Float> points = path.getPoints();
            Iterator<Float> iterator1 = points.iterator();
            if (path.getmSlideNumber() != slideNumber && slideNumber != -1) {
                stringBuilder.append("</Paths><Paths>");
            }
            stringBuilder.append("<Path>");
            stringBuilder.append("<Pointer>Pen</Pointer>");
            stringBuilder.append("<Points>");
            while (iterator1.hasNext()){
                float tempX = iterator1.next();
                float tempY = iterator1.next();
                if (tempX != -1) {
                    x = tempX/width;
                    y = tempY/height;
                    stringBuilder.append(x+","+y+",");
                } else {
                    stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
                    stringBuilder.append("</Points>");
                    if (iterator1.hasNext()) {
                        stringBuilder.append("</Path><Path>");
                    } else {
                        stringBuilder.append("</Path>");
                    }
                }
            }
        }
        stringBuilder.append("</Paths></WISEInkXML>");
        return stringBuilder.toString();
    }
}
