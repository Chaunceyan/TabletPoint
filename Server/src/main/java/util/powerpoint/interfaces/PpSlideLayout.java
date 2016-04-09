package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.IComEnum;

/**
 * Created by Chaun on 4/9/2016.
 */
public enum PpSlideLayout implements IComEnum {
    ppLayoutBlank(12)
    ;

    private long mValue;

    PpSlideLayout(long value) {
        mValue = value;
    }

    @Override
    public long getValue() {
        return mValue;
    }
}
