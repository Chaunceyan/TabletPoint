package com.wise.vub.tabletpoint;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.wise.vub.tabletpoint.util.Constants;


import java.io.IOException;
import java.util.logging.LogRecord;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PresentationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PresentationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresentationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private ConnectionService mConnectionService;

    // TODO: Rename and change types of parameters
    private String deviceMacAddr;

    private OnFragmentInteractionListener mListener;

    public PresentationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PresentationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PresentationFragment newInstance() {
        PresentationFragment fragment = new PresentationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceMacAddr = getActivity().getIntent().getStringExtra(Constants.device_address_tag.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_presentation, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure bluetooth is enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            // Device does not support Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        // Start the background service of bluetooth communication
      mConnectionService = new ConnectionService( getActivity(),deviceMacAddr);
      mConnectionService.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mConnectionService.stop();
    }

//    private final Handler mHandler = new Handler() {
//
//        public  void updateImage(Bitmap bitmap) {
//            Activity activity = getActivity();
//            ImageView imageView = (ImageView) activity.findViewById(R.id.image_view_presentation);
//            imageView.setImageBitmap(bitmap);
//        }
//    };

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
