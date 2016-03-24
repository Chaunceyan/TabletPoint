package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.IComEnum;

/**
 * Created by Chaun on 2/26/2016.
 */
public enum PpSlideShowPointerType implements IComEnum {
    ppSlideShowPointerAlwaysHidden(3),
    ppSlideShowPointerArrow(1),
    ppSlideShowPointerAutoArrow(4),
    ppSlideShowPointerEraser(5),
    ppSlideShowPointerNone(0),
    ppSlideShowPointerPen(2);

    private long value;
    private PpSlideShowPointerType(long value) {this.value = value;}
    public long getValue(){return this.value;}
}
