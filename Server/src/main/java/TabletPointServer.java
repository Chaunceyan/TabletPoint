import server.SPPServer;
import util.powerpoint.MSPowerPoint;

import java.io.*;

import javax.bluetooth.*;
import javax.microedition.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Class that implements an SPP Server which accepts single line of
 * message from an SPP client and sends a single line of response to the client.
 */
public class TabletPointServer {

    public static void main(String[] args) throws IOException {

        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: "+localDevice.getBluetoothAddress());
        System.out.println("Name: "+localDevice.getFriendlyName());

        // Open frame, choose presentation file and start bluetooth server.
        JFrame parentFrame = new JFrame();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PPT format", "ppt", "pptx");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(parentFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileName = chooser.getSelectedFile().getAbsolutePath();
            System.out.println("Opening file: " + fileName);
            File temp = new File("SlideImages");
            MSPowerPoint.openFile(fileName);
            MSPowerPoint.present();
            MSPowerPoint.savePresentationAsJPG(temp.getAbsolutePath());
        }
        SPPServer server = new SPPServer();
        server.run();
    }
}