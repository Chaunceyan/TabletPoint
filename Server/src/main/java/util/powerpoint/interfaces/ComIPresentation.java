package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 2/25/2016.
 */
public interface ComIPresentation {

    @ComProperty
    ComISlideShowSettings getSlideShowSettings();

    @ComMethod
    void Close();
}