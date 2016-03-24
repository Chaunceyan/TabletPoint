package com.wise.vub.tabletpoint;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.wise.vub.tabletpoint.util.ClientConstants;
import com.wise.vub.tabletpoint.util.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
        private ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "Creating ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

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
            write(ClientConstants.IMAGE_REQEUST + "\r\n");

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
                                Log.d(TAG, "Header Incorrect");
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