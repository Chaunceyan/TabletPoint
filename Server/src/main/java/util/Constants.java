package util;

/**
 * Created by Chaun on 3/13/2016.
 */
public enum Constants {
    resolution ("Resolution: "),
    rgbs ("RGBS: ");

    private String mName;
    Constants(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }
}
