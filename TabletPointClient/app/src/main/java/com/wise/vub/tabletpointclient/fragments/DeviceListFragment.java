package com.wise.vub.tabletpointclient.fragments;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wise.vub.tabletpointclient.R;
import com.wise.vub.tabletpointclient.TabletPoint;
import com.wise.vub.tabletpointclient.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Chaun on 4/22/2016.
 */
public class DeviceListFragment extends Fragment {
    public DeviceListFragment(){}
    private BluetoothAdapter mBTAdapter;
    // Adapter to dynamically modify the list.
    private ArrayList<String> mDeviceInfoList = new ArrayList<>();
    private ArrayAdapter<String> mDeviceListAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_device_list, container, false);
        // Get bluetooth adapter, initiate the device list, setOnclickListener.
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        ListView deviceList = (ListView) rootView.findViewById(R.id.list_view_device);
        mDeviceListAdapter = new ArrayAdapter<> (getActivity(),
            R.layout.item_device_list,
            mDeviceInfoList);
        deviceList.setAdapter(mDeviceListAdapter);
        deviceList.setOnItemClickListener(new onDeviceClickListener());
        return rootView;
    }

    /*
    * Listener for device list.
    * If user click one device, the listener tries to connect to the device.
    * Upon success, fragment transaction begins
    * */
    protected class onDeviceClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String macAddress = mDeviceListAdapter.getItem(position).split("\r\n")[1];
            Toast.makeText(getActivity(),
                    "Trying to connect with remote device: " +
                    mDeviceListAdapter.getItem(position),
                    Toast.LENGTH_LONG).show();
            // Connect to the device and return a reference.
            try {
                BluetoothDevice device = mBTAdapter.getRemoteDevice(macAddress);
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
                ((TabletPoint) getActivity()).startConnection(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() == null) {
            Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
            mDeviceListAdapter.clear();
            for (BluetoothDevice device : pairedDevices) {
                mDeviceListAdapter.add(device.getName() + "\r\n" + device.getAddress());
            }
        }
    }
}
