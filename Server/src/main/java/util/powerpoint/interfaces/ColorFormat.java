package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 2/26/2016.
 */
public interface ColorFormat {
    @ComProperty
    void setObjectThemeColor(long color);
    @ComProperty
    void setRGB(int RGB);
}
