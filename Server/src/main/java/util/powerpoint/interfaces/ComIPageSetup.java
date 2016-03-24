package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 3/24/2016.
 */
public interface ComIPageSetup {
    @ComProperty
    float getSlideHeight();
    @ComProperty
    float getSlideWidth();
}
