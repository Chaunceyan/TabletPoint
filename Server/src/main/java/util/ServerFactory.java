package util;

import server.SPPServer;


import java.awt.*;

/**
 * Created by Chaun on 3/8/2016.
 */
public class ServerFactory {
    private static Robot robot;
    public static Robot getRobot() {
        if (robot == null) {
            try {
                robot = new Robot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return robot;
    }
}
