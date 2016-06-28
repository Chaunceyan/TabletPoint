package server;

import server.Connections.ConnectionThread;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.*;

/**
 * Created by Chaun on 3/5/2016.
 */
public class SPPServer extends Thread {

    private UUID uuid;
    private String connectionString;

    public SPPServer() {
        //Create a UUID for SPP
        uuid = new UUID(80087355);
        //Create the servicve url
        connectionString = "btspp://localhost:" + uuid+ ";name=Sample SPP Server";
    }

      public void run() {

        //open server url
        StreamConnectionNotifier streamConnNotifier = null;
        try {
            streamConnNotifier = (StreamConnectionNotifier) Connector.open( connectionString );
            StreamConnection streamConnection = streamConnNotifier.acceptAndOpen();
            ConnectionThread connectionThread = new ConnectionThread(streamConnection);
            new Thread(connectionThread).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
