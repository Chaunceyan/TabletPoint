package server.Connections;

import com.sun.org.apache.xpath.internal.SourceTree;
import image_produce.ScreenCapture;
import util.XMLWriter;
import util.constants.Commands;
import util.constants.Constants;
import util.powerpoint.MSPowerPoint;
import util.powerpoint.interfaces.PpSlideShowPointerType;

import javax.bluetooth.RemoteDevice;
import javax.imageio.*;
import javax.microedition.io.StreamConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Chaun on 3/8/2016.
 */
public class ConnectionThread implements Runnable {
    private ConnectionService connectionService;
    private StreamConnection  streamConnection;
    private Executor executor;
    private final ConcurrentLinkedQueue<String> queue;

    public ConnectionThread (StreamConnection streamConnection) {
        this.streamConnection = streamConnection;
        queue = new ConcurrentLinkedQueue<String>();
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

        private ConnectionService () {
            try {
                out = streamConnection.openOutputStream();
                writer = ImageIO.getImageWritersByFormatName("jpg").next();
                reader = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));

                param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.5f);
                ScreenCapture.init();

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
                    System.out.println("Incoming commands: " + command);
                    if (command != null) {
                        if (command.equals(Constants.IMAGE_REQEUST)) {
                            sendImage();
                        } else if (command.equals(Constants.SLIDE_PREVIEW_REQUEST)) {
                            File temp = new File("SlideImages");
                            File[] files = temp.listFiles();
                            sort(files);
                            out.write(ByteBuffer.allocate(4).putInt(files.length).array());
                            sendSlidesPreview(files);
                        } else if (command.equals(Constants.INKXML_REQUEST)) {
                            System.out.println(MSPowerPoint.readInkFile());
                            out.write((String.valueOf(ScreenCapture.getSlideViewRatio())
                                    + "," + MSPowerPoint.readInkFile() + "\r\n")
                                    .getBytes());
                        } else if (command.startsWith(Constants.NEW_SLIDE)) {
                            String[] params = command.split(",");
                            MSPowerPoint.addNewSlide(Integer.valueOf(params[1]));
                            File temp = new File("SlideImages"); // Location of the saved slide images.
                            int prevCount = temp.listFiles().length;
                            MSPowerPoint.savePresentationAsJPG(new File("SlideImages").getAbsolutePath());
                            File[] files = temp.listFiles();
                            while (files.length <= prevCount) {
                                files = temp.listFiles();
                            }
                            sort(files);
                            sendFile(files[files.length - 1]);
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

        private void sort(File[] files) {
            Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            int n1 = extractNumber(o1.getName());
                            int n2 = extractNumber(o2.getName());
                            return n1 - n2;
                        }

                        private int extractNumber (String name) {
                            int endIndex = name.lastIndexOf('.');
                            int number = Integer.valueOf(name.substring(5, endIndex));
                            return  number;
                        }
                    });
                for (File file : files) {
                    System.out.println(file.getName());
                }
        }

        private boolean sendSlidesPreview (File[] files) throws IOException {
            for (File file : files) {
                sendFile(file);
                if (!reader.readLine().equalsIgnoreCase(Constants.FILE_RECEIVED)){
                    return false;
                }
                System.out.println(file.getName());
            }
            return true;
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
                InputStream tempInputStraem = new FileInputStream(file);
                tempImage = new File(fileName);
                tempImage.delete();
                BufferedImage image = ImageIO.read(tempInputStraem);
                imageOut = new FileOutputStream(tempImage);
                writer.setOutput( ImageIO.createImageOutputStream(imageOut));
                param.setCompressionQuality(0.2f);
                writer.write(null, new IIOImage(image, null, null), param);
                byte[] data = Files.readAllBytes(Paths.get(tempImage.getAbsolutePath()));
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

        public Executor() {
        }

        private float mX, mY;
        public void run() {
            while(true) {
                try {
                    if (queue.isEmpty()) {
                        Thread.sleep(1000);
                    } else {
                        String command = queue.poll();
                        execute(command);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void execute (String command) {
            int leftPadding = ScreenCapture.leftPadding;
            int topPadding = ScreenCapture.topPadding;
            String [] params = command.split(",");
            float width = ScreenCapture.getScreenCaptureBound().width, height = ScreenCapture.getScreenCaptureBound().height;
            switch (Integer.valueOf(params[0])) {
                case Commands.PenDown:
                    mX = Float.valueOf(params[1]) * width + leftPadding;
                    mY = Float.valueOf(params[2]) * height + topPadding;
                    MSPowerPoint.penDown(mX, mY, PpSlideShowPointerType.valueOf(params[3]));
                    break;
                case Commands.PenMove:
                    mX = Float.valueOf(params[1]) * width + leftPadding;
                    mY = Float.valueOf(params[2]) * height + topPadding;
                    MSPowerPoint.drawLine(mX, mY);
                    break;
                case Commands.PenUp:
                    mX = Float.valueOf(params[1]) * width + leftPadding;
                    mY = Float.valueOf(params[2]) * height + topPadding;
                    MSPowerPoint.drawLine(mX, mY);
                    MSPowerPoint.penUp();
                    break;
                case Commands.GOTO_PREV_SLIDE:
                    MSPowerPoint.gotoPrevious();
                    break;
                case Commands.GOTO_NEXT_SLIDE:
                    MSPowerPoint.gotoNext();
                    break;
                case Commands.GOTO_SLIDE:
                    MSPowerPoint.gotoSlide(Integer.valueOf(params[1]));
                    break;
                case Commands.SAVE_INK:
                    MSPowerPoint.saveInkFile(command.substring(command.indexOf(",")+1));
                    break;
                case Commands.SET_COLOR:
                    int color = MSPowerPoint.transformColor(new Color(Integer.valueOf(params[1])));
                    MSPowerPoint.setPointerColor(color);
                    break;
            }
        }
    }
}
