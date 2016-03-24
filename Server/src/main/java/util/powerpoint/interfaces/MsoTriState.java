package util.powerpoint.interfaces;

import com.sun.jna.platform.win32.COM.util.IComEnum;

/**
 * Created by Chaun on 2/25/2016.
 */
public enum MsoTriState implements IComEnum {
    msoCTrue (1),
    msoFalse (0),
    msoTriStateMixed(-2),
    msoTriStateToggle(-3),
    msoTrue(-1);

    private MsoTriState(long value) {this.value = value;}
    private long value;
    public long getValue() {
        return this.value;
    }
}
