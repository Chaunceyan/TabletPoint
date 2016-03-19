package server.Connections;

import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import com.sun.xml.internal.ws.util.*;
import image_produce.ImageCompressor;
import image_produce.ScreenCapture;
import sun.nio.ch.IOUtil;
import util.*;
import util.Constants;

import javax.bluetooth.RemoteDevice;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.microedition.io.StreamConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Chaun on 3/8/2016.
 */
public class ConnectionThread implements Runnable {
    private ConnectionService connectionService;
    StreamConnection  streamConnection;
    private String separator = ",";

    public ConnectionThread (StreamConnection streamConnection) {
        this.streamConnection = streamConnection;
    }

    public void run() {
        RemoteDevice dev = null;
        try {
            dev = RemoteDevice.getRemoteDevice(streamConnection);
            System.out.println("Remote device address: "+dev.getBluetoothAddress());
            System.out.println("Remote device name: "+dev.getFriendlyName(true));
            System.out.println("Starting sender thread!!");
            connectionService = new ConnectionService();
            connectionService.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectionService implements Runnable {
        private String fileName = "TempFile";
        private File tempImage;
        private ImageWriteParam param;
        private ImageWriter writer;
        private OutputStream imageOut;
        private OutputStream out;
        private BufferedReader reader;
        private BufferedImage curScreenCapture, prevScreenCapture;
        private boolean sendFlag = true;
        private boolean initFlag = false;
        private int width = 480;
        private String frameInfo;
        private List<String> frameInfos;
        private int interlacedPass = 2;

        private final int imageType = BufferedImage.TYPE_USHORT_555_RGB;

        private ConnectionService () {
            frameInfos = new ArrayList<String>();
            try {
                out = streamConnection.openOutputStream();
                writer = ImageIO.getImageWritersByFormatName("jpg").next();

                reader = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));

                param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.5f);



            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String command = null;
                while (true) {
                    tempImage = new File(fileName);
                    tempImage.delete();
                    imageOut = new FileOutputStream(tempImage);
                    writer.setOutput( ImageIO.createImageOutputStream(imageOut));
                    command = reader.readLine();
                    if (command != null) {
                        System.out.println("Incoming command: " + command);
                        if (command.equals(Constants.IMAGE_REQEUST)) {
                            sendImage();
                        } else {
                            System.out.println("Commands to be implemented.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendImage() {
            try {
                if (sendFlag) {
                    System.out.println("Sending");
                    // Screen Capture here
                    curScreenCapture = ScreenCapture.getScreenCapture();

                    // Try to resize
                    if (width < curScreenCapture.getWidth()) {
                        curScreenCapture = ScreenCapture.resizeImage(curScreenCapture,
                                width,
                                curScreenCapture.getHeight() * width / curScreenCapture.getWidth(),
                                imageType);
                    }
                    sendFrame(curScreenCapture);
                    prevScreenCapture = curScreenCapture;
                    frameInfo = null;
                    curScreenCapture.flush();
                    curScreenCapture = null;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        } // Run function.


        private void sendFrame (BufferedImage image) {
            try {
                writer.write(null, new IIOImage(image, null, null), param);
                Path path = Paths.get(tempImage.getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
                System.out.println();
                // Write header
                out.write(Constants.HEADER_MSB);
                out.write(Constants.HEADER_LSB);
                // Write size
                out.write(ByteBuffer.allocate(4).putInt(data.length).array());
                // Image Out
                out.write(data);
                out.flush();
                imageOut.flush();
                imageOut.close();
                writer.reset();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Output error, stopping thread!");
                stopThread();
            }
        }

        public void stopThread() {
            try {
                streamConnection.close();
                tempImage.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // Stops thread
    } // Connection Sender Class

    private class ConnectionReceiver implements Runnable {

        public void run() {
            
        } // Run method

    } // Connection Receiver Class
}
