package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 3/30/2016.
 */
@ComInterface(iid = "914934F6-5A91-11CF-8700-00AA0060263B")
public interface ComICustomXMLPart {
    @ComProperty
    public String getXML();
    @ComProperty
    public String getID();
    @ComMethod
    public boolean LoadXML(String xml);
    @ComMethod
    public boolean Load(String FilePath);
    @ComMethod
    public void Delete();
}
