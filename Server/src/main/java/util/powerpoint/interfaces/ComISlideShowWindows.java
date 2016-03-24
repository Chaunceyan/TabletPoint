package util.powerpoint.interfaces;

import com.oracle.webservices.internal.api.message.PropertySet;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 3/24/2016.
 */
public interface ComISlideShowWindows {
    @ComProperty
    ComISlideShowWindow getItem(int index);
}
