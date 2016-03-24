package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.IComEnum;

/**
 * Created by Chaun on 3/24/2016.
 */
public enum PpSaveAsFileType implements IComEnum {
    ppSaveAsJPG (17)
    ;
    private long value;

    private PpSaveAsFileType(long value) {
        this.value = value;
    }
    public long getValue() {
        return value;
    }
}
