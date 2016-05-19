package com.wise.vub.tabletpointclient.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.VectorDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
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
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ogaclejapan.arclayout.Arc;
import com.ogaclejapan.arclayout.ArcLayout;
import com.wise.vub.tabletpointclient.R;
import com.wise.vub.tabletpointclient.TabletPoint;
import com.wise.vub.tabletpointclient.adapters.AnnotationListAdapter;
import com.wise.vub.tabletpointclient.data.MyPath;
import com.wise.vub.tabletpointclient.data.Point;
import com.wise.vub.tabletpointclient.utils.AnimatorUtils;
import com.wise.vub.tabletpointclient.utils.Constants;
import com.wise.vub.tabletpointclient.utils.MyXMLParser;
import com.wise.vub.tabletpointclient.views.SlideshowView;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class PresentationFragment extends Fragment {

    private AnnotationListAdapter annotationListAdapter;
    View fab;
    View menuLayout;
    ArcLayout arcLayout;

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
                slideshowView.addAnnotation((ArrayList<MyPath>) ((ArrayList<MyPath>) parent.getItemAtPosition(position)).clone());
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

        newSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btHandler.sendMessageAtFrontOfQueue(
                        btHandler.obtainMessage(Constants.SEND_NEW_SLIDE, linearLayout.getChildCount())
                );
            }
        });

        fab = rootView.findViewById(R.id.fab);
        menuLayout = rootView.findViewById(R.id.menu_layout);
        arcLayout = (ArcLayout) rootView.findViewById(R.id.arc_layout);
        arcLayout.setArc(Arc.BOTTOM_RIGHT);

        for (int i = 0, size = arcLayout.getChildCount(); i < size; i++) {
            arcLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int drawableId = 0;
                    switch (v.getId()) {
                        case R.id.pen_button:
                            onToolsSelected(Constants.PEN_MENU_ID);
                            drawableId = R.drawable.pen;
                            break;
                        case R.id.pencil_button:
                            onToolsSelected(Constants.PENCIL_MENU_ID);
                            drawableId = R.drawable.pencil;
                            break;
                        case R.id.eraser_button:
                            onToolsSelected(Constants.ERASER_MENU_ID);
                            drawableId = R.drawable.eraser;
                            break;
                        case R.id.line_button:
                            onToolsSelected(Constants.LINE_MENU_ID);
                            drawableId = R.drawable.line;
                            break;
                        case R.id.square_button:
                            onToolsSelected(Constants.SQUARE_MENU_ID);
                            drawableId = R.drawable.square;
                            break;
                        case R.id.magic_wand_button:
                            onToolsSelected(Constants.MAGIC_WAND_ID);
                            drawableId = R.drawable.magic_wand;
                            break;
                        case R.id.color_button:
                            onToolsSelected(Constants.COLOR_PICKER);
                            return;
                    }
                    hideMenu();
                    ((ImageButton)fab).setImageDrawable(
                            getResources().getDrawable(drawableId)
                    );
                }
            });
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFabClick(v);
                onToolsSelected(v.getId());
            }
        });
        return rootView;
    }

    private void onFabClick(View v) {
        if (v.isSelected()) {
            hideMenu();
        } else {
            showMenu();
        }
        v.setSelected(!v.isSelected());
    }

    @SuppressWarnings("NewApi")
    private void showMenu() {
        menuLayout.setVisibility(View.VISIBLE);
        menuLayout.setClickable(true);
        List<Animator> animList = new ArrayList<>();

        for (int i = 0, len = arcLayout.getChildCount(); i < len; i++) {
            animList.add(createShowItemAnimator(arcLayout.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(400);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.playTogether(animList);
        animSet.start();
    }

    @SuppressWarnings("NewApi")
    private void hideMenu() {

        List<Animator> animList = new ArrayList<>();

        for (int i = arcLayout.getChildCount() - 1; i >= 0; i--) {
            animList.add(createHideItemAnimator(arcLayout.getChildAt(i)));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(400);
        animSet.setInterpolator(new AnticipateInterpolator());
        animSet.playTogether(animList);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                menuLayout.setVisibility(View.INVISIBLE);
            }
        });
        animSet.start();

    }

    private Animator createShowItemAnimator(View item) {

        float dx = fab.getX() - item.getX();
        float dy = fab.getY() - item.getY();

        item.setRotation(0f);
        item.setTranslationX(dx);
        item.setTranslationY(dy);

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(0f, 720f),
                AnimatorUtils.translationX(dx, 0f),
                AnimatorUtils.translationY(dy, 0f)
        );

        return anim;
    }

    private Animator createHideItemAnimator(final View item) {
        float dx = fab.getX() - item.getX();
        float dy = fab.getY() - item.getY();

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(720f, 0f),
                AnimatorUtils.translationX(0f, dx),
                AnimatorUtils.translationY(0f, dy)
        );

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                item.setTranslationX(0f);
                item.setTranslationY(0f);
            }
        });

        return anim;
    }

    public boolean onToolsSelected(int itemId) {
        // Handle item selection
        final SlideshowView slideshowView = (SlideshowView) getActivity().findViewById(R.id.view_slideshow);
        switch (itemId) {
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
            case Constants.MAGIC_WAND_ID:
                slideshowView.setPenStroke(Constants.MAGIC_WAND_ID);
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

                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onColorSelected(int color) {
                        mSelectedColor[0] = color;
                        slideshowView.setColor(color);
                        Drawable background = ((ImageButton)fab).getBackground();
                        ((GradientDrawable)background).setColor(color);
                        ((ImageButton) fab).setBackground(background);
                        fab.invalidate();
                        hideMenu();
                    }

                });

                dialog.show(getFragmentManager(), "color_picker");
                break;
        }
        return true;
    }

}
