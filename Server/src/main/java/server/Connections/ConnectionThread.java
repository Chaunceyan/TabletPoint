package server.Connections;

import image_produce.ScreenCapture;
import util.constants.Commands;
import util.constants.Constants;
import util.powerpoint.MSPowerPoint;
import util.powerpoint.interfaces.PpSlideShowPointerType;

import javax.bluetooth.RemoteDevice;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.microedition.io.StreamConnection;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Chaun on 3/8/2016.
 */
public class ConnectionThread implements Runnable {
    private ConnectionService connectionService;
    private StreamConnection  streamConnection;
    private Executor executor;
    private String separator = ",";
    private final ArrayBlockingQueue<String> queue;

    public ConnectionThread (StreamConnection streamConnection) {
        this.streamConnection = streamConnection;
        queue = new ArrayBlockingQueue<String>(100);
    }


    public void run() {
        RemoteDevice dev = null;
        try {
            dev = RemoteDevice.getRemoteDevice(streamConnection);
            System.out.println("Remote device address: "+dev.getBluetoothAddress());
            System.out.println("Remote device name: "+dev.getFriendlyName(true));

            System.out.println("Executor started");
            executor = new Executor();
            new Thread(executor).start();

            connectionService = new ConnectionService();
            new Thread(connectionService).start();


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
                            System.out.println("Putting commands into queue");
                            queue.add(command);
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

    private class Executor implements Runnable{

        private float mX, mY;
        public void run() {
            while(true) {
                try {
                    if (queue.isEmpty()) {
                        Thread.sleep(1000);
                    } else {
                        String command = queue.take();
                        execute(command);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void execute (String command) {
            float endX,endY;
            String [] params = command.split(",");
            switch (Integer.valueOf(params[0])) {
                case Commands.PenDown:
                    MSPowerPoint.setPointType(PpSlideShowPointerType.ppSlideShowPointerPen);
                    mX = Float.valueOf(params[1]);
                    mY = Float.valueOf(params[2]);
                    break;
                case Commands.PenMove:
                    System.out.println("Incoming commands: Drawing Line" );
                    endX = Float.valueOf(params[1]);
                    endY = Float.valueOf(params[2]);
                    MSPowerPoint.drawLine(mX, mY, endX, endY);
                    mX = endX;
                    mY = endY;
                    break;
                case Commands.PenUp:
                    endX = Float.valueOf(params[1]);
                    endY = Float.valueOf(params[2]);
                    MSPowerPoint.drawLine(mX, mY, endX, endY);
                    mX = endX;
                    mY = endY;
            }
        }
    }
}
