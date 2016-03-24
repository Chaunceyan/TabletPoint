package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;

/**
 * Created by Chaun on 2/25/2016.
 */
public interface ComIPresentations {
    @ComMethod
    ComIPresentation Add();

    @ComMethod
    ComIPresentation Open(String FileName,
                          MsoTriState ReadOnly,
                          MsoTriState Untitled,
                          MsoTriState WithWindowe);
}
