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
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Chaun on 3/8/2016.
 */
public class ConnectionThread implements Runnable {
    private ConnectionService connectionService;
    private StreamConnection  streamConnection;
    private Executor executor;
    private final ArrayBlockingQueue<String> queue;

    public ConnectionThread (StreamConnection streamConnection) {
        this.streamConnection = streamConnection;
        queue = new ArrayBlockingQueue<String>(100);
    }


    public void run() {
        RemoteDevice dev;
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
        private BufferedImage curScreenCapture;
        private boolean sendFlag = true;
        private int width = Math.round(MSPowerPoint.getSlideShowViewWidth());
        private final int imageType = BufferedImage.TYPE_USHORT_555_RGB;
        private int fileIndex;

        private ConnectionService () {
            try {
                out = streamConnection.openOutputStream();
                writer = ImageIO.getImageWritersByFormatName("jpg").next();

                reader = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));

                param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.5f);
                fileIndex = 0;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String command;
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
                        } else if (command.equals(Constants.SLIDE_PREVIEW_REQUEST)) {
                            File[] files = new File("SlideImages").listFiles();
                            out.write(ByteBuffer.allocate(4).putInt(files.length).array());
                            sendSlidesPreview(fileIndex++, files);
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

        private boolean sendSlidesPreview(int index, File[] files) {
            if(index < files.length) {
                sendFile(files[index]);
                index ++;
                return true;
            } else {
                return false;
            }
        }

        public void sendImage() {
            try {
                if (sendFlag) {
                    // Screen Capture here
                    curScreenCapture = ScreenCapture.getScreenCapture();
                    System.out.println("Sending: " + curScreenCapture.getWidth() + ", " + curScreenCapture.getHeight());
                    // Try to resize
                    if (width < curScreenCapture.getWidth()) {
                        curScreenCapture = ScreenCapture.resizeImage(curScreenCapture,
                                width,
                                curScreenCapture.getHeight() * width / curScreenCapture.getWidth(),
                                imageType);
                    }
                    sendFrame(curScreenCapture);
                    curScreenCapture.flush();
                    curScreenCapture = null;
                }
            } catch(Exception e) {
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

        private void sendFile (File file) {
            try {
                byte[] data = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                System.out.println(data.length);
                out.write(ByteBuffer.allocate(4).putInt(data.length).array());
                out.write(data);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
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
            float width = 960, height = 540;
            switch (Integer.valueOf(params[0])) {
                case Commands.PenDown:
                    MSPowerPoint.setPointType(PpSlideShowPointerType.ppSlideShowPointerPen);
                    mX = Float.valueOf(params[1]) * width;
                    mY = Float.valueOf(params[2]) * height;
                    break;
                case Commands.PenMove:
                    endX = Float.valueOf(params[1]) * width;
                    endY = Float.valueOf(params[2]) * height;
                    MSPowerPoint.drawLine(mX, mY, endX, endY);
                    mX = endX;
                    mY = endY;
                    System.out.println("Incoming commands: Drawing Line" + mX + "," + mY);
                    break;
                case Commands.PenUp:
                    endX = Float.valueOf(params[1]) * width;
                    endY = Float.valueOf(params[2]) * height;
                    MSPowerPoint.drawLine(mX, mY, endX, endY);
                    mX = endX;
                    mY = endY;
            }
        }
    }
}
