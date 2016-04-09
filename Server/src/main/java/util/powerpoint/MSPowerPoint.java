package util.powerpoint;

import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.FileUtils;
import com.sun.jna.platform.win32.COM.util.Factory;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import org.apache.commons.codec.Charsets;
import util.ServerFactory;
import util.powerpoint.interfaces.*;


import java.awt.*;
import java.awt.event.InputEvent;
import java.io.*;
import java.util.*;

/**
 * Created by Chaun on 2/26/2016.
 */
public final class MSPowerPoint {
    private static boolean mainScreenFlag = true;
    private static ComIApplication app;
    private static ComIPresentation pres;
    private static ComISlideShowWindow window;
    private static ComISlideShowView view;
    private static Robot robot;
    static {
        Factory factory = new Factory();
        ComPowerPoint_Application powerpoint = factory.createObject(ComPowerPoint_Application.class);
        app = powerpoint.queryInterface(ComIApplication.class);
        robot = ServerFactory.getRobot();
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
    public static boolean penDown(float x, float y, PpSlideShowPointerType pointer) {
        if (isForegroundWindow()) {
            view.setPointerType(pointer);
            robot.mouseMove(Math.round(x), Math.round(y));
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            return true;
        } else {
            return false;
        }
    }

    public static boolean drawLine(float x, float y) {
        if (isForegroundWindow()) {
            robot.mouseMove(Math.round(x), Math.round(y));
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }
    public static boolean penUp() {
        if (isForegroundWindow()) {
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isForegroundWindow() {
        byte[] windowText = new byte[512];

        PointerType hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
        String programName = Native.toString(windowText);
        return programName.startsWith("PowerPoint Slide Show");
    }
    public static void savePresentationAsJPG(String fileName) {
        pres.SaveAs(fileName, PpSaveAsFileType.ppSaveAsJPG, MsoTriState.msoFalse);
    }
    public static float getSlideShowWindowHeigth() {
        return window.getHeight();
    }
    public static float getSlideShowWindowWidth() {
        return window.getWidth();
    }
    public static float getSlideShowViewHeigth() {
        return pres.getPageSetup().getSlideHeight();
    }
    public static float getSlideShowViewWidth() {
        return pres.getPageSetup().getSlideWidth();
    }

    public static String readInkFile() {
        if (app != null) {
            for (long i = 1; i<=pres.getCustomXMLParts().getCount(); i++) {
                String xml = pres.getCustomXMLParts().getItem(i).getXML();
                if (xml.startsWith("<WISEInkXML>")) {
                    System.out.println(xml);
                    return xml;
                }
            }
        }
        return null;
    }

    public static void addNewSlide(int position){
        if (pres != null) {
            pres.getSlides().Add(position, PpSlideLayout.ppLayoutBlank);
        }
    }

    public static void saveInkFile(String xmlString) {
        if (app != null) {
            for (long i = 1; i<=pres.getCustomXMLParts().getCount(); i++) {
                if (pres.getCustomXMLParts().getItem(i).getXML().startsWith("<WISEInkXML>")) {
                    System.out.println(pres.getCustomXMLParts().getItem(i).getXML());
                    pres.getCustomXMLParts().getItem(i).Delete();
                }
            }
        }
            ComICustomXMLPart part = pres.getCustomXMLParts().Add();
        System.out.println(xmlString);
            part.LoadXML(xmlString);
    }

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
        WinDef.HWND GetForegroundWindow();  // add this
        int GetWindowTextA(PointerType hWnd, byte[] lpString, int nMaxCount);
    }
}
