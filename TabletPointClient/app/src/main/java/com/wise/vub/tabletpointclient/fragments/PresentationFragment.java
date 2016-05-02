package com.wise.vub.tabletpointclient.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.wise.vub.tabletpointclient.R;
import com.wise.vub.tabletpointclient.TabletPoint;
import com.wise.vub.tabletpointclient.adapters.AnnotationListAdapter;
import com.wise.vub.tabletpointclient.data.MyPath;
import com.wise.vub.tabletpointclient.data.Point;
import com.wise.vub.tabletpointclient.utils.Constants;
import com.wise.vub.tabletpointclient.utils.MyXMLParser;
import com.wise.vub.tabletpointclient.views.SlideshowView;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class PresentationFragment extends Fragment {

    private AnnotationListAdapter annotationListAdapter;

    public PresentationFragment() {
        // Required empty public constructor
    }

    public static PresentationFragment newInstance() {
        PresentationFragment fragment = new PresentationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View rootView = inflater.inflate(R.layout.fragment_presentation, container, false);

        // Handler for slides preview.
        final Handler btHandler = ((TabletPoint) getActivity()).mBTHandler;
        btHandler.obtainMessage(Constants.SEND_SLIDE_PREVIEW_REQUEST).sendToTarget();

        // Get SlideShowView object
        final SlideshowView slideshowView = (SlideshowView) rootView.findViewById(R.id.view_slideshow);
        final LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.linear_layout_previews);

        // Handler for annotations.
        btHandler.obtainMessage(Constants.SEND_INKXML_REQUEST).sendToTarget();

        final ListView listView = (ListView) rootView.findViewById(R.id.list_view_annotation);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                slideshowView.addAnnotation((ArrayList<MyPath>) parent.getItemAtPosition(position));
            }
        });

        // Timer for update the frame.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Handler btHandler = ((TabletPoint) getActivity()).mBTHandler;
                if (btHandler != null) {
                    btHandler.obtainMessage(Constants.SEND_IMAGE_REQUEST).sendToTarget();
                }
            }
        }, 100, 100);

        // Set onClick listener
        ImageButton previous = (ImageButton) rootView.findViewById(R.id.image_button_previous);
        ImageButton next = (ImageButton) rootView.findViewById(R.id.image_button_next);
        ImageButton save = (ImageButton) rootView.findViewById(R.id.image_button_save_ink);
        ImageButton reload = (ImageButton) rootView.findViewById(R.id.image_button_reload);
        ImageButton newSlide = (ImageButton) rootView.findViewById(R.id.image_button_new_slide);

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideshowView.mCurrentSlide -= 1;
                btHandler.sendMessageAtFrontOfQueue(
                        btHandler.obtainMessage(Constants.SEND_GOTO_PREV));
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideshowView.mCurrentSlide += 1;
                btHandler.sendMessageAtFrontOfQueue(
                        btHandler.obtainMessage(Constants.SEND_GOTO_NEXT));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ArrayList<MyPath>> graphs = new ArrayList<ArrayList<MyPath>>();
                ArrayList<MyPath> paths = slideshowView.getInkXMl();
                HashMap<Integer, ArrayList<MyPath>> hashMap = new HashMap<Integer, ArrayList<MyPath>>();
                Iterator<MyPath> iterator = paths.iterator();
                while (iterator.hasNext()) {
                    MyPath path = iterator.next();
                    int slideNumber = path.getSlideNumber();
                    if (hashMap.containsKey(slideNumber)) {
                        hashMap.get(slideNumber).add(path);
                    } else {
                        hashMap.put(slideNumber, new ArrayList<MyPath>());
                        hashMap.get(slideNumber).add(path);
                    }
                }
                graphs.addAll(hashMap.values());
                AnnotationListAdapter adapter = (AnnotationListAdapter) listView.getAdapter();
                for (int index = 0; index <adapter.getCount(); index++) {
                    ArrayList<MyPath> tempPaths = adapter.getItem(index);
                    graphs.add(tempPaths);
                }
                ArrayList<ArrayList<MyPath>> nonDupList = new ArrayList<>();
                for (ArrayList<MyPath> path : graphs) {
                    if (!nonDupList.contains(path)) {
                        nonDupList.add(path);
                    }
                }
                adapter.clear();
                adapter.addAll(nonDupList);
                btHandler.sendMessageAtFrontOfQueue(
                        btHandler.obtainMessage(Constants.SEND_SAVE_INK, MyXMLParser.pathsToXML(nonDupList)));
            }
        });

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btHandler.sendMessageAtFrontOfQueue(
                        btHandler.obtainMessage(Constants.SEND_SLIDE_PREVIEW_REQUEST)
                );
            }
        });

        newSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btHandler.sendMessageAtFrontOfQueue(
                        btHandler.obtainMessage(Constants.SEND_NEW_SLIDE, linearLayout.getChildCount())
                );
            }
        });
        return rootView;
    }


    // Option Menu To Select which tool to use.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, Constants.PENCIL_MENU_ID, 0, "Pencil");
        menu.add(0, Constants.PEN_MENU_ID, 0, "Pen");
        menu.add(0, Constants.ERASER_MENU_ID, 0, "Eraser");
        menu.add(0, Constants.LINE_MENU_ID, 0, "Line");
        menu.add(0, Constants.SQUARE_MENU_ID, 0, "Square");
        menu.add(0, Constants.COLOR_PICKER, 0, "Color");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        final SlideshowView slideshowView = (SlideshowView) getActivity().findViewById(R.id.view_slideshow);
        switch (item.getItemId()) {
            case Constants.PENCIL_MENU_ID:
                slideshowView.setPenStroke(Constants.PENCIL_MENU_ID);
                break;
            case Constants.PEN_MENU_ID:
                slideshowView.setPenStroke(Constants.PEN_MENU_ID);
                break;
            case Constants.ERASER_MENU_ID:
                slideshowView.setPenStroke(Constants.ERASER_MENU_ID);
                break;
            case Constants.LINE_MENU_ID:
                slideshowView.setPenStroke(Constants.LINE_MENU_ID);
                break;
            case Constants.SQUARE_MENU_ID:
                slideshowView.setPenStroke(Constants.SQUARE_MENU_ID);
                break;
            case Constants.COLOR_PICKER:
                int[] mColors = getResources().getIntArray(R.array.default_rainbow);
                final int[] mSelectedColor = {ContextCompat.getColor(getActivity(), R.color.flamingo)};
                ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                        mColors,
                        mSelectedColor[0],
                        5, // Number of columns
                        ColorPickerDialog.SIZE_SMALL);

                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                    @Override
                    public void onColorSelected(int color) {
                        mSelectedColor[0] = color;
                        slideshowView.setColor(color);
                    }

                });

                dialog.show(getFragmentManager(), "color_picker");
                break;
        }
        return true;
    }

}
