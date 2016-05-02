package com.wise.vub.tabletpointclient;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.*;
import android.os.Process;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.vub.tabletpointclient.adapters.AnnotationListAdapter;
import com.wise.vub.tabletpointclient.connections.BTConnectionThread;
import com.wise.vub.tabletpointclient.data.MyPath;
import com.wise.vub.tabletpointclient.data.Point;
import com.wise.vub.tabletpointclient.fragments.DeviceListFragment;
import com.wise.vub.tabletpointclient.fragments.PresentationFragment;
import com.wise.vub.tabletpointclient.utils.Constants;
import com.wise.vub.tabletpointclient.utils.MyXMLParser;
import com.wise.vub.tabletpointclient.views.SlideshowView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class TabletPoint extends AppCompatActivity {

    public static final String TAG = "TabletPoint";
    public Handler mBTHandler;
    public Handler mUIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet_point);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUIHandler = new UIHandler(Looper.getMainLooper(), this);

        // Check bluetooth enabled or not.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            // Device does not support Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            DeviceListFragment deviceListFragment = new DeviceListFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, deviceListFragment).commit();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            DeviceListFragment deviceListFragment = new DeviceListFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container, deviceListFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tablet_point, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startConnection(BluetoothSocket socket) {
        BTConnectionThread btConnection = new BTConnectionThread("BluetoothConnectionThread",
                Process.THREAD_PRIORITY_FOREGROUND, mUIHandler);
        // If connection success. Go to the presentation fragment;
        if (btConnection.connect(socket)) {
            btConnection.start();
            mBTHandler = btConnection.getHandler();
            PresentationFragment presentationFragment = PresentationFragment.newInstance();
            getFragmentManager().beginTransaction().
                    addToBackStack(null).
                    replace(R.id.fragment_container, presentationFragment).commit();
        } else {
            Toast.makeText(this , "Connection Failed. Check if server is running.", Toast.LENGTH_LONG).show();
        }

    }

    protected class UIHandler extends Handler {

        private WeakReference<Context> mActivity;

        public UIHandler(Looper looper, Context context) {
            super(looper);
            mActivity = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SlideshowView slideshowView = (SlideshowView)
                            ((TabletPoint) mActivity.get()).
                                    findViewById(R.id.view_slideshow);
            switch (msg.what) {
                case Constants.UPDATE_IMAGE:
                    if (slideshowView != null) {
                        slideshowView.updateBitmap((Bitmap) msg.obj);
                    }
                    break;
                case Constants.UPDATE_PREVIEW:
                    LinearLayout linearLayout = (LinearLayout)
                            ((TabletPoint) mActivity.get()).
                                    findViewById(R.id.linear_layout_previews);
                    ArrayList<Bitmap> previews = (ArrayList<Bitmap>) msg.obj;
                    Iterator<Bitmap> iterator = previews.iterator();
                    int tag = 1;
                    if (linearLayout != null) {
                        linearLayout.removeAllViewsInLayout();
                        while (iterator.hasNext()) {
                            linearLayout.addView(
                                populateImageView(iterator.next(), tag, slideshowView));
                            tag++;
                        }
                    }
                    break;
                case Constants.UPDATE_ANNOTAIONS:
                    ListView listView = (ListView) ((TabletPoint) mActivity.get()).
                                    findViewById(R.id.list_view_annotation);
                    AnnotationListAdapter annotationListAdapter =
                            new AnnotationListAdapter(mActivity.get(), 0, slideshowView.getWidth(), slideshowView.getHeight());
                    Log.d("TabletPoint", "AnnotationMovement: " + slideshowView.getWidth() + "," + slideshowView.getHeight());
                    Point.width = slideshowView.getWidth();
                    Point.height = slideshowView.getHeight();
                    listView.setAdapter(annotationListAdapter);
                    String xmlStr = (String) msg.obj;
                    ArrayList<ArrayList<MyPath>> graph = MyXMLParser.pathsFrom(xmlStr.substring(xmlStr.indexOf(",") +1), slideshowView.getWidth(), slideshowView.getHeight());
                    if (graph != null) annotationListAdapter.addAll(graph);
                    break;
            }
        }

        private View populateImageView(Bitmap bitmap, int tag, final SlideshowView slideshowView) {
            LinearLayout linearLayout = (LinearLayout) View.inflate(mActivity.get(), R.layout.compound_view_previews, null);
            ImageView imageView = (ImageView) linearLayout.findViewById(R.id.image_view_preview);
            imageView.setImageBitmap(bitmap);
            TextView textView = (TextView) linearLayout.findViewById(R.id.text_view_slide_number);
            textView.setText("Slide " + tag);
            linearLayout.setTag(R.id.image_tag, tag);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int slideNumber = (int) v.getTag(R.id.image_tag);
                    mBTHandler.obtainMessage(Constants.SEND_GOTO_SLIDE, slideNumber).sendToTarget();
                    slideshowView.mCurrentSlide = slideNumber;
                }
            });
            return linearLayout;
        }

    }
}
