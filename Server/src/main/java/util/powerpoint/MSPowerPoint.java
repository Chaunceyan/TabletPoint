package util.powerpoint;

import com.sun.jna.platform.win32.COM.util.Factory;
import util.powerpoint.interfaces.*;


import java.awt.*;
import java.util.*;

/**
 * Created by Chaun on 2/26/2016.
 */
public final class MSPowerPoint {
    private static ComIApplication app;
    private static ComIPresentation pres;
    private static ComISlideShowWindow window;
    private static ComISlideShowView view;
    static {
        Factory factory = new Factory();
        ComPowerPoint_Application powerpoint = factory.createObject(ComPowerPoint_Application.class);
        app = powerpoint.queryInterface(ComIApplication.class);
    }
    private MSPowerPoint(){}
    public static boolean openFile(String fileUrl) {
        pres = app.getPresentations().Open(fileUrl, MsoTriState.msoFalse, MsoTriState.msoFalse, MsoTriState.msoTrue);
//        app.setVisible(true)
        app.Activate();
        app.setVisible(true);
        return true;
    }
    public static boolean present() {
        if(pres != null) {
            window = pres.getSlideShowSettings().run();
            window.Activate();
            view = window.getView();
            return true;
        } else {
            return false;
        }
    }
    public static boolean gotoSlide(int slideIndex) {
        if(view != null) {
            view.GotoSlide(slideIndex, MsoTriState.msoTrue);
            return true;
        } else {
            return false;
        }
    }
    public static boolean gotoFisrt() {
        if (view != null) {
            view.First();
            return true;
        } else {
            return false;
        }
    }
    public static boolean gotoLast() {
        if (view != null) {
            view.Last();
            return true;
        } else {
            return false;
        }
    }
    public static boolean gotoNext() {
        if (view != null) {
            view.Next();
            return true;
        } else {
            return false;
        }
    }
    public static boolean gotoPrevious() {
        if (view != null) {
            view.Previous();
            return true;
        } else {
            return false;
        }
    }
    public static boolean setPointType(PpSlideShowPointerType pointerType){
        if (view != null) {
            view.setPointerType(pointerType);
            return true;
        } else {
            return false;
        }
    }
    public static boolean setPointerColor (Color color) {
        if (view != null) {
            view.getPointerColor().setRGB(color.getRGB());
            return true;
        } else {
            return false;
        }
    }
    public static boolean drawLine(float x, float y, float endX, float endY) {
        if (view != null) {
            view.DrawLine(x, y, endX, endY);
            return true;
        } else {
            return false;
        }
    }
}
