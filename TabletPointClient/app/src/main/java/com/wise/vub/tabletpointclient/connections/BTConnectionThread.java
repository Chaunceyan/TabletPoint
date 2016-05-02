package com.wise.vub.tabletpointclient.connections;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.renderscript.ScriptGroup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Chaun on 4/23/2016.
 */
public class BTConnectionThread extends HandlerThread {

    private Handler mBTHandler;
    private Handler mUIHandler;
    private BluetoothSocket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;

    public BTConnectionThread(String name, int priority, Handler uiHanlder) {
        super(name, priority);
        mUIHandler = uiHanlder;
    }

    public Handler getHandler() {
        mBTHandler = new BTConnectionHandler(this.getLooper(), mInStream, mOutStream, mUIHandler);
        return mBTHandler;
    }

    public boolean connect(BluetoothSocket socket) {
        try {
            mSocket = socket;
            mSocket.connect();
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
