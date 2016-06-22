package com.wise.vub.tabletpointclient.connections;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.wise.vub.tabletpointclient.TabletPoint;
import com.wise.vub.tabletpointclient.data.MyPath;
import com.wise.vub.tabletpointclient.data.Point;
import com.wise.vub.tabletpointclient.utils.Constants;
import com.wise.vub.tabletpointclient.utils.MyXMLParser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Chaun on 4/23/2016.
 */
public class BTConnectionHandler extends Handler {

    private Handler mUIHandler;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    public BTConnectionHandler(Looper looper, InputStream inputStream, OutputStream outputStream, Handler uiHandler) {
        super(looper);
        mUIHandler = uiHandler;
        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        try {
            switch(msg.what) {
                case Constants.SEND_IMAGE_REQUEST:
                    write(Constants.IMAGE_REQEUST + "\r\n");
                    updateOneFrame();
                    break;
                case Constants.SEND_SLIDE_PREVIEW_REQUEST:
                    write(Constants.SLIDE_PREVIEW_REQUEST + "\r\n");
                    receiveImages();
                    break;
                case Constants.SEND_INKXML_REQUEST:
                    write(Constants.INKXML_REQUEST + "\r\n");
                    receiveInk();
                    break;
                case Constants.SEND_GOTO_SLIDE:
                    write(Constants.GOTO_SLIDE + "," + msg.obj + "\r\n");
                    break;
                case Constants.SEND_SAVE_INK:
                    write(Constants.SAVE_INK + "," + msg.obj + "\r\n");
                    break;
                case Constants.SEND_GOTO_NEXT:
                    write(Constants.GOTO_NEXT_SLIDE + "\r\n");
                    break;
                case Constants.SEND_GOTO_PREV:
                    write(Constants.GOTO_PREV_SLIDE + "\r\n");
                    break;
                case Constants.SEND_NEW_SLIDE:
                    write(Constants.NEW_SLIDE + "," + ((Integer) msg.obj + 1 ) + "\r\n");
                    receiveNewSlide();
                    mUIHandler.obtainMessage(Constants.GOTO_NEW_SLIDE).sendToTarget();
                    break;
                case Constants.SEND_PEN_DOWN:
                    write(Constants.PenDown
                            + "," + ((Point) msg.obj).getX()
                            + "," + ((Point) msg.obj).getY()
                            + "," + Constants.PEN + "\r\n");
                    break;
                case Constants.SEND_ERASER_DOWN:
                    write(Constants.PenDown
                            + "," + ((Point) msg.obj).getX()
                            + "," + ((Point) msg.obj).getY()
                            + "," + Constants.ERASER + "\r\n");
                    break;
                case Constants.SEND_PEN_MOVE:
                    write(Constants.PenMove
                            + "," + ((Point) msg.obj).getX()
                            + "," + ((Point) msg.obj).getY()
                            + "\r\n");
                    break;
                case Constants.SEND_PEN_UP:
                    write(Constants.PenUp
                            + "," + ((Point) msg.obj).getX()
                            + "," + ((Point) msg.obj).getY()
                            + "\r\n");
                    break;
                case Constants.SEND_SET_COLOR:
                    write(Constants.SET_COLOR
                        + "," + msg.obj + "\r\n");
                    break;
            }
        } catch (IOException e) {
                e.printStackTrace();
                // Here I will need to handle the exception
        }
    }

    private void receiveNewSlide() throws IOException{
        byte[] sizeBuffer = new byte[4];
        mInputStream.read(sizeBuffer);
        int fileSize = ByteBuffer.wrap(sizeBuffer).getInt();
        int progress = 0;
        // read one file
        byte[] tempBuffer = new byte[Constants.CHUNK_SIZE];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (progress < fileSize) {
            int readSize = mInputStream.read(tempBuffer);
            byteArrayOutputStream.write(tempBuffer, 0, readSize);
            progress += readSize;
        }
        byte[] fileBuffer = byteArrayOutputStream.toByteArray();
        Log.d("TabletPoint", "ImagePreviewSize: " + fileSize);
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap tempbitmap = BitmapFactory.decodeByteArray(fileBuffer ,0, fileSize, options);//Decode image, "thumbnail" is the object of image file
        Bitmap bitmap = Bitmap.createScaledBitmap(tempbitmap, tempbitmap.getWidth() / 4, tempbitmap.getHeight() / 4, true);// convert decoded bitmap into well scaled Bitmap format.
        mUIHandler.obtainMessage(Constants.ADD_NEWSLIDE, bitmap).sendToTarget();
    }

    private void receiveInk() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream));
        String xmlString = reader.readLine();
        mUIHandler.sendMessageAtFrontOfQueue(
                mUIHandler.obtainMessage(Constants.UPDATE_ANNOTATIONS, xmlString));
        Log.d("TabletPoint", xmlString);
    }

    // Read in one frame
    private void updateOneFrame() throws IOException {
        boolean waitingForHeader = true;
        byte[] headers = new byte[6];
        int headerIndex = 0;
        int imageSize = 0;
        int progress = 0;
        // Temporary store all our data here.
        ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();
        while (true) {
            if (waitingForHeader) {
                // Read every byte.
                byte[] headerBuffer = new byte[1];
                mInputStream.read(headerBuffer, 0, 1);
                headers[headerIndex++] = headerBuffer[0];

                // If all byte is read.
                if (headerIndex == 6) {
                    if (headers[0] == Constants.HEADER_MSB && headers[1] == Constants.HEADER_LSB) {
                        byte[] imageSizeBuffer = Arrays.copyOfRange(headers, 2, 6);
                        progress = imageSize = ByteBuffer.wrap(imageSizeBuffer).getInt();
                        waitingForHeader = false;
                    } else {
                        // Handles exception by reconnect;
                        throw new IOException();
                    }
                }
            } else {
                byte[] imageBuffer = new byte[Constants.CHUNK_SIZE];
                int byteRead = mInputStream.read(imageBuffer);
                progress -= byteRead;
                dataOutputStream.write(imageBuffer, 0, byteRead);
                if (progress <= 0) {
                    byte[] data = dataOutputStream.toByteArray();
                    Bitmap image = BitmapFactory.decodeByteArray(data, 0, imageSize);
                    mUIHandler.obtainMessage(Constants.UPDATE_IMAGE, image).sendToTarget();
                    break;
                    // Call UI thread to deal with the message;
                }
            }
        }
    }

    // receive all previews

    private void receiveImages() throws IOException {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        // Buffer suitable for int;
        byte[] sizeBuffer = new byte[4];
        mInputStream.read(sizeBuffer);
        int fileNumber = ByteBuffer.wrap(sizeBuffer).getInt();
        Log.d("TabletPoint", "File Number: " + fileNumber);
        do {
            mInputStream.read(sizeBuffer);
            int fileSize = ByteBuffer.wrap(sizeBuffer).getInt();
            int progress = 0;
            // read one file
            byte[] tempBuffer = new byte[Constants.CHUNK_SIZE];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (progress < fileSize) {
                int readSize = mInputStream.read(tempBuffer);
                byteArrayOutputStream.write(tempBuffer, 0, readSize);
                progress += readSize;
            }
            byte[] fileBuffer = byteArrayOutputStream.toByteArray();
            Log.d("TabletPoint", "ImagePreviewSize: " + fileSize);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap tempbitmap = BitmapFactory.decodeByteArray(fileBuffer ,0, fileSize, options);//Decode image, "thumbnail" is the object of image file
            Bitmap bitmap = Bitmap.createScaledBitmap(tempbitmap, tempbitmap.getWidth() / 4, tempbitmap.getHeight() / 4, true);// convert decoded bitmap into well scaled Bitmap format.
            bitmaps.add(bitmap);
            write(Constants.FILE_RECEIVED + "\r\n");
        } while (bitmaps.size() < fileNumber);

        // Now we get it loaded. We update UI thread.
        mUIHandler.obtainMessage(Constants.UPDATE_PREVIEW, bitmaps).sendToTarget();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void write(String message) {
        try {
            mOutputStream.write(message.getBytes());
            mOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
