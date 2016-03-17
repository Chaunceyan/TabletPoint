package server.Connections;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import image_produce.ImageCompressor;
import image_produce.ScreenCapture;
import sun.nio.ch.IOUtil;
import util.Constants;

import javax.bluetooth.RemoteDevice;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.microedition.io.StreamConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
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
    private ConnectionSender senderThread;
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
            senderThread = new ConnectionSender();
            senderThread.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectionSender implements Runnable {
        private File tempImage;
        private ImageWriteParam param;
        private ImageWriter writer;
        private PrintWriter printWriter;
        private BufferedImage curScreenCapture, prevScreenCapture;
        private boolean sendFlag = true;
        private boolean initFlag = false;
        private int width = 480;
        private String frameInfo;
        private List<String> frameInfos;
        private int interlacedPass = 2;

        private final int imageType = BufferedImage.TYPE_USHORT_555_RGB;

        private ConnectionSender () {
            frameInfos = new ArrayList<String>();
            try {
                OutputStream opt = streamConnection.openOutputStream();
                writer = ImageIO.getImageWritersByFormatName("jpg").next();

                param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.5f);

                printWriter = new PrintWriter(opt);
                tempImage = new File(String.valueOf(System.currentTimeMillis()));
                writer.setOutput(ImageIO.createImageOutputStream(new FileOutputStream(tempImage)));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
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
//
//                        // Change resolution
//                        if (!initFlag) {
//                            System.out.println("Initialize the header of the package");
//                            // Initialize the header of the package
//                            frameInfo = Constants.resolution.getName() +
//                                    imageType + separator +
//                                    curScreenCapture.getWidth() + separator +
//                                    curScreenCapture.getHeight() + separator;
////                                    ScreenCapture.getScreenCaptureBound().getWidth() + separator +
////                                    ScreenCapture.getScreenCaptureBound().height + separator;
//
////                            // Iterate all screens and stores everything
////                            for (Rectangle bound : ScreenCapture.getScreenBounds()) {
////                                frameInfo += bound.x + separator +
////                                        bound.y + separator +
////                                        bound.width + separator +
////                                        bound.height + separator;
////                            }
//                            sendFrameInfo(frameInfo);
//                            prevScreenCapture = null;
//                            initFlag = true;
//                        }

//                        for (int i = 0; i < interlacedPass; i++) {
//                            frameInfo = ImageCompressor.getImageDiffTrame(curScreenCapture, prevScreenCapture, i, interlacedPass);
//                            if (frameInfo.length() > 0) {
//                                sendFrameInfo(Constants.rgbs.getName() + frameInfo);
//                            }
//                        }

                        // Flush
//                        sendFrame(curScreenCapture);
                        prevScreenCapture = curScreenCapture;
                        frameInfo = null;
                        curScreenCapture.flush();
                        curScreenCapture = null;
                    }

//                    // Sending infos.
//                    synchronized (frameInfos) {
//                        if (frameInfos.size() > 0) {
//                            for (String frameInfo : frameInfos) {
//                                sendFrameInfo(frameInfo);
//                            }
//                            frameInfos.clear();
//                        }
//                    }



                    // Error handling
                } // While loop

            }catch(Exception e){
                e.printStackTrace();
            } finally {
                writer.abort();
            }
        } // Run function.


        private void sendFrameInfo (String frameInfo) {
            try {
                printWriter.write(frameInfo);
                System.out.println(frameInfo);
                printWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Output error, stopping thread!");
                stopThread();
            }
        } // Send frame information to buffer.

        private void sendFrame (BufferedImage image) {
            try {
                writer.write(null, new IIOImage(image, null, null), param);
                Path path = Paths.get(tempImage.getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
//                // Write header
//                out.write(Constants.resolution.getName().getBytes());
//                out.write(data.length);
                // Image Out
                printWriter.write(String.valueOf(data));
                printWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Output error, stopping thread!");
                stopThread();
            }
        }

        public void stopThread() {
            try {
                printWriter.close();
                streamConnection.close();
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
