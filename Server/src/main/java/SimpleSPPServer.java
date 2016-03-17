import server.SPPServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.bluetooth.*;
import javax.microedition.io.*;

/**
 * Class that implements an SPP Server which accepts single line of
 * message from an SPP client and sends a single line of response to the client.
 */
public class SimpleSPPServer {

    public static void main(String[] args) throws IOException {

        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: "+localDevice.getBluetoothAddress());
        System.out.println("Name: "+localDevice.getFriendlyName());

        SPPServer server = new SPPServer();
        server.run();
    }
}