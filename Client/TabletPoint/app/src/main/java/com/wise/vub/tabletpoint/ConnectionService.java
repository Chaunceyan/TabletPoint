package com.wise.vub.tabletpoint;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wise.vub.tabletpoint.util.Constants;
import com.wise.vub.tabletpoint.util.ImageUpdater;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import static java.lang.Math.round;

/**
 * Created by Chaun on 3/12/2016.
 */
public class ConnectionService {
    private final BluetoothAdapter mAdapter;
    private final Activity mActivity;
    private BluetoothDevice mDevice;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "Chauncey: ";


    // Constructor
    public ConnectionService(Activity activity, String deviceMacAddr) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mActivity = activity;
        mDevice = mAdapter.getRemoteDevice(deviceMacAddr);
        mConnectThread = null;
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
    public synchronized void write() {

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
        }

        public void run() {
            Log.d(TAG, "BEGINING mConnectedThread");
//            byte[] buffer = new byte[1024];
//            int bytes;
            BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));
            boolean waitingForHeader = true;
            ByteArrayOutputStream dataOutputStream = new ByteArrayOutputStream();
            while (true) {
                int size;
                final Bitmap image;
                try {
                    Log.d(TAG, "Before update image0");
                    String imageInfo = reader.readLine();
                    Log.d(TAG, imageInfo);
//                    dataOutputStream.write(buffer, 0, bytesRead);
//                    byte[] data = dataOutputStream.toByteArray();
//                    image = BitmapFactory.decodeByteArray(data, 0, data.length);

                    Log.d(TAG, "Before update image2");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Before update image");
                            ImageView imageView = ((ImageView) mActivity.findViewById(R.id.image_view_presentation));
                            imageView.setImageDrawable(mActivity.getDrawable(R.drawable.test));
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Disconnected", e);
                    ConnectionService.this.connect();
                }
            }
        }

        public void cancel() {

        }
    }
}