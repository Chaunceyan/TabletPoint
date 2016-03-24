package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;

/**
 * Created by Chaun on 2/25/2016.
 */
@ComInterface(iid="{9149349D-5A91-11CF-8700-00AA0060263B}")
public interface ComISlideShowSettings{
    @ComMethod
    public ComISlideShowWindow run();
}
