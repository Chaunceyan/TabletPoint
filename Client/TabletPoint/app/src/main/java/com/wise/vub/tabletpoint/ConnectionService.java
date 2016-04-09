package com.wise.vub.tabletpoint;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wise.vub.tabletpoint.AnnotationListView.AnnotationListAdapter;
import com.wise.vub.tabletpoint.RecyclerView.DividerItemDecoration;
import com.wise.vub.tabletpoint.RecyclerView.SlidesPreviewAdapter;
import com.wise.vub.tabletpoint.util.ClientConstants;
import com.wise.vub.tabletpoint.util.Constants;
import com.wise.vub.tabletpoint.util.XMLHandler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Chaun on 3/12/2016.
 */
public class ConnectionService extends Service {
    private final BluetoothAdapter mAdapter;
    private final Activity mActivity;
    private BluetoothDevice mDevice;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    public float ratio;
    public boolean mConnectedFlag;
    public AnnotationListAdapter annotationListAdapter;
    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "Chauncey: ";

    private final class ServiceHandler extends Handler {
        public ServiceHandler (Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.GOTO_SLIDE:
                    System.out.println(msg.toString());
                    write(msg.toString());
            }
        }
    }

    // Constructor
    public ConnectionService(Activity activity, String deviceMacAddr) {
        super();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mActivity = activity;
        mDevice = mAdapter.getRemoteDevice(deviceMacAddr);
        mConnectThread = null;
        mConnectedFlag = false;
    }

    @Override
    public void onCreate() {

    }


    /**
     * Three functions this service class provides.
     */

    // Connect to specific device
    public synchronized void connect() {
        Log.d(TAG, "Connect to: " + mDevice);

        mConnectThread = new ConnectThread(mDevice);
        mConnectThread.start();
    }

    // Start a connection
    public synchronized void connected(BluetoothSocket socket) {
        Log.d(TAG, "Connected for data transfer");

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        mConnectedFlag = true;
    }

    // Stop all threads
    public synchronized void stop() {
        Log.d(TAG, "Stopping");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    // Write message to the server
    public synchronized void write(String commands) {
        if (mConnectedThread != null) {
            mConnectedThread.write(commands);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Three thread inner classes to connect, read and send information.
     */

    // Make an attemption to connect.
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        private ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            // Establish connection
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket " + "create() failed", e);
            } finally {
                mmSocket = tmp;
            }
        }

        public void run() {
            Log.d(TAG, "Beginning connection");
            connected(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Closing connection" + mmSocket.toString() + " failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Bitmap image;
        private int fileIndex;
        private ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "Creating ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            fileIndex = 0;

            // Get the BluetoothSocket input and output stream.
            try {
                socket.connect();
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Sockets creation failed.", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

            // Load the previews of the slides and the annotations
            preLoadSlidesAndNotes();
            preLoadInk();
            write(ClientConstants.IMAGE_REQEUST + "\r\n");
        }

        public void preLoadInk() {
            try {
                write(ClientConstants.INKXML_REQUEST + "\r\n");
                BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));
                final String xmlStr = reader.readLine();
                Log.d("XML Parsing", xmlStr);
                ratio = Float.valueOf(xmlStr.split(",")[0]);
                final float[] height = new float[1];
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView annotationList = (ListView) mActivity.findViewById(R.id.list_view_annotation);
                        ScribbleView sView = (ScribbleView) mActivity.findViewById(R.id.customized_view_scribble);
                        height[0] = sView.getMeasuredHeight();
                        Log.e("Why", String.valueOf(height[0]));
                        annotationListAdapter = new AnnotationListAdapter(mActivity, 0, ratio, height[0]);
                        annotationList.setAdapter(annotationListAdapter);
                        annotationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                MyPath path = (MyPath) parent.getItemAtPosition(position);
                                Log.d("XML Parser", path.getPoints().toString());
                                ScribbleView sView = (ScribbleView) mActivity.findViewById(R.id.customized_view_scribble);
                                sView.addPath(path);
                            }
                        });
                        int start = xmlStr.indexOf(",");
                        float leftPadding = (sView.getWidth() - height[0]/ratio) / 2;
                        if (XMLHandler.pathsFrom(xmlStr.substring(start + 1), ratio, height[0], leftPadding) != null)
                        annotationListAdapter.addAll(XMLHandler.pathsFrom(xmlStr.substring(start + 1), ratio, height[0], leftPadding));
                    }
                });
            } catch (IOException e) {
                Log.e("Chauncey Error:", e.getMessage());
            }
        }

        public void preLoadSlidesAndNotes() {
            try {
                final ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                write(ClientConstants.SLIDE_PREVIEW_REQUEST + "\r\n");
                byte[] sizeBuffer = new byte[4];
                int fileNumeber;
                do {
                    mmInStream.read(sizeBuffer);
                    fileNumeber = ByteBuffer.wrap(sizeBuffer).getInt();
                    Log.d(TAG, "File Number: " + fileNumeber);
                    mmInStream.read(sizeBuffer);
                    int dataSize = ByteBuffer.wrap(sizeBuffer).getInt();
                    int progress = 0;
                    while (!(progress >= dataSize)) {
                        byte[] dataBuffer = new byte[ClientConstants.CHUNK_SIZE];
                        int readSize = mmInStream.read(dataBuffer);
                        byteArrayOutputStream.write(dataBuffer, 0, readSize);
                        progress += readSize;
                        Log.d(TAG, "Progress: " + progress + " Data size: " + dataSize);
                    }
                    byte[] totalData = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.reset();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(totalData, 0, dataSize);
                    bitmaps.add(bitmap);
                    if (fileIndex != fileNumeber - 1) write(ClientConstants.SLIDE_PREVIEW_REQUEST + "\r\n");
                }  while (++fileIndex < fileNumeber);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Before update image");
                        RecyclerView recyclerView = (RecyclerView) mActivity.findViewById(R.id.slide_preview_list);
                        recyclerView.setHasFixedSize(true);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        Bitmap[] bitmapArray = bitmaps.toArray(new Bitmap[bitmaps.size()]);
                        SlidesPreviewAdapter mAdapter = new SlidesPreviewAdapter(mActivity, bitmapArray);
                        recyclerView.setAdapter(mAdapter);
                        recyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.HORIZONTAL_LIST));
                    }
                });
                Log.d(TAG, "Bitmap Loaded.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                Log.d(TAG, "BEGINING mConnectedThread");
                boolean waitingForHeader = true;
                ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();
                byte[] headers = new byte[6];
                int headerIndex = 0;
                int imageSize = 0;
                int progress = 0;
                while (true) {
                    if (waitingForHeader) {
                        byte[] headerBuffer = new byte[1];
                        mmInStream.read(headerBuffer, 0, 1);
                        Log.d(TAG, "Received Header Byte: " + headerBuffer[0]);
                        headers[headerIndex++] = headerBuffer[0];
                        if (headerIndex == 6) {
                            if (headers[0] == ClientConstants.HEADER_MSB && headers[1] == ClientConstants.HEADER_LSB) {
                                Log.d(TAG, "Header received.");
                                byte[] imageSizeBuffer = Arrays.copyOfRange(headers, 2, 6);
                                imageSize = ByteBuffer.wrap(imageSizeBuffer).getInt();
                                progress = imageSize;
                                Log.d(TAG, "Image size: " + String.valueOf(imageSize));
                                waitingForHeader = false;
                            } else {
                                Log.d(TAG, "Header Incorrect: " + headers[0] + headers[1]);
                                mmSocket.close();
                                break;
                            } // Header
                        } // Loop when header is not fully read.
                    } else {
                        byte[] buffer = new byte[ClientConstants.CHUNK_SIZE];
                        int byteRead = mmInStream.read(buffer);
                        Log.d(TAG, "Read in data size: " + byteRead);
                        Log.d(TAG, "Expecting." + (progress -=  byteRead));
                        dataOutputStream.write(buffer, 0, byteRead);

                        if (progress <= 0) {
                            Log.d(TAG, "Data fully received.");
                            final byte[] data = dataOutputStream.toByteArray();
                            Log.d(TAG, "Data length: " + data.length);
                            final Bitmap image = BitmapFactory.decodeByteArray(data, 0, imageSize);
                            Log.d(TAG, "Bitmap is created");
                            /* Updating the UI */
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "Before update image");
                                    ScribbleView customizedView = (ScribbleView) mActivity.findViewById(R.id.customized_view_scribble);
                                    customizedView.updateBitmap(image);
                                }
                            });
                            waitingForHeader = true;
                            headerIndex = 0;
                            imageSize = 0;
                            progress = 0;
                            dataOutputStream.reset();
                            write(ClientConstants.IMAGE_REQEUST + "\r\n");
                            Log.d(TAG, "New request out");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {

        }

        public void write(String commands) {
            try {
                mmOutStream.write(commands.getBytes());
                mmOutStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}