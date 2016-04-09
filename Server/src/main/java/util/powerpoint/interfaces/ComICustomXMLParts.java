package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 3/30/2016.
 */
public interface ComICustomXMLParts {
    @ComProperty
    public ComICustomXMLPart getItem(Object index);

    @ComProperty
    public long getCount();

    @ComMethod
    public ComICustomXMLPart Add();
}
