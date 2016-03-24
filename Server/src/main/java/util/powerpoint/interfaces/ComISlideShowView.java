package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

/**
 * Created by Chaun on 2/26/2016.
 */
public interface ComISlideShowView {
    @ComProperty
    ColorFormat getPointerColor();
    @ComProperty
    void setPointerType(PpSlideShowPointerType pointerType);
    @ComMethod
    void Next();
    @ComMethod
    void Previous();
    @ComMethod
    void GotoSlide(int index, MsoTriState resetSlide);
    @ComMethod
    void First();
    @ComMethod
    void Last();
    @ComMethod
    void DrawLine(float BeginX,
                  float BeginY,
                  float EndX,
                  float EndY);
}
