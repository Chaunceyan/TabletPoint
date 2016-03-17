package com.wise.vub.tabletpoint.util;

/**
 * Created by Chaun on 3/13/2016.
 */
public enum Constants {
    resolution ("Resolution: "),
    rgbs ("RGBS: "),
    object_separator (","),
    value_seperator ("#"),
    tag ("Chauncey: "),
    device_address_tag ("DeviceAddress"),
    coming_header ("ComingHeader");

    private String mName;
    Constants(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }
}
