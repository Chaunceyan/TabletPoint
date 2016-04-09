package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;

/**
 * Created by Chaun on 4/9/2016.
 */
public interface ComISlides {
    @ComMethod
    ComISlide Add(int index, PpSlideLayout ppSlideLayout);
}
