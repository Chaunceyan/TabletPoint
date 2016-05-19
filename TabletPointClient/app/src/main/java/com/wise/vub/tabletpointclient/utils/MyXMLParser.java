package com.wise.vub.tabletpointclient.utils;

import android.util.Log;
import android.util.Xml;

import com.wise.vub.tabletpointclient.data.MyPath;
import com.wise.vub.tabletpointclient.data.Point;

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
 * Created by Chaun on 4/27/2016.
 */
public class MyXMLParser {

    public static ArrayList<ArrayList<MyPath>> pathsFrom(String xmlStr, float width, float height) {
        if (xmlStr.equals("null")) return null;
        try {
            InputStream in = new ByteArrayInputStream(xmlStr.getBytes());
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            ArrayList<ArrayList<MyPath>> graphs = new ArrayList<>();
            ArrayList<MyPath> paths = new ArrayList<>();
            MyPath path = new MyPath();
            boolean bPointer = false;
            boolean bPoints = false;
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                int event = parser.next();
                String startName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (startName.equalsIgnoreCase("Paths")) {
                            paths = new ArrayList<>();
                        } else if (startName.equalsIgnoreCase("Path")) {
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
                                String[] points = pointsStr.split(",");
                                ArrayList<String> pointsArray = new ArrayList<String>(Arrays.asList(points));
                                Iterator<String> ite = pointsArray.iterator();
                                if (ite.hasNext()) {
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
                            graphs.add(paths);
                        } else if (startName.equalsIgnoreCase("Path")) {
                            path.endLine();
                            paths.add(path);
                        }
                        break;
                }
            }
            return graphs;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }
    public static String pathsToXML (ArrayList<ArrayList<MyPath>> graphs) {
        Iterator<ArrayList<MyPath>> iterator = graphs.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        int slideNumber = 0;
        stringBuilder.append("<WISEInkXML>");
        while (iterator.hasNext()) {
            stringBuilder.append("<Paths>");
            ArrayList<MyPath> graph = iterator.next();
            Iterator<MyPath> pathIterator = graph.iterator();
            while (pathIterator.hasNext()) {
                stringBuilder.append("<Path>");
                stringBuilder.append("<Pointer>Pen</Pointer>");
                stringBuilder.append("<Points>");
                MyPath path = pathIterator.next();
                Vector<Point> points = path.getPoints();
                Iterator<Point> pointIterator = points.iterator();
                while (pointIterator.hasNext()){
                    Point point = pointIterator.next();
                    float x = point.getX();
                    float y = point.getY();
                    if (x > 0) {
                        stringBuilder.append(x+","+y+",");
                    }
                }
                stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
                stringBuilder.append("</Points></Path>");
            }
            stringBuilder.append("</Paths>");
        }
        stringBuilder.append("</WISEInkXML>");
        return stringBuilder.toString();
    }
}
