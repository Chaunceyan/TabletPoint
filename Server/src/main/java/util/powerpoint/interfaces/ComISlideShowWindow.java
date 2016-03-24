package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 2/25/2016.
 */
public interface ComISlideShowWindow {
    @ComProperty
    ComISlideShowView getView();
    @ComProperty
    float getHeight();
    @ComProperty
    float getLeft();
    @ComProperty
    float getWidth();
    @ComProperty
    float getTop();
    @ComMethod
    void Activate();
}
