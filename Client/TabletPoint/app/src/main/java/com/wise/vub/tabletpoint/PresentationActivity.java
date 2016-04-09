package com.wise.vub.tabletpoint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.wise.vub.tabletpoint.util.Constants;
import com.wise.vub.tabletpoint.util.PenStrokes;
import com.wise.vub.tabletpoint.util.XMLHandler;


public class PresentationActivity extends AppCompatActivity implements PresentationFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        final GridView gridView = (GridView) findViewById(R.id.grid_view_buttons);
        gridView.setAdapter(new ButtonsAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ConnectionService service = ((PresentationFragment) (getFragmentManager().findFragmentById(R.id.fragment_presentation_image))).mConnectionService;
                if (service.mConnectedFlag) {
                    ScribbleView scribbleView = (ScribbleView) ((View) view.getRootView()).findViewById(R.id.customized_view_scribble);
                    if (scribbleView != null) {
                        switch (position) {
                            case 0:
                                service.write(String.valueOf(Constants.GOTO_PREV_SLIDE) + "\r\n");
                                scribbleView.mSlideNumber -= 1;
                                break;
                            case 1:
                                service.write(String.valueOf(Constants.GOTO_NEXT_SLIDE) + "\r\n");
                                scribbleView.mSlideNumber += 1;
                                break;
                            case 2:
                                ((ImageView) ((GridView) parent).getChildAt(3)).setColorFilter(null);
                                ((ImageView) ((GridView) parent).getChildAt(4)).setColorFilter(null);
                                ((ImageView) view).setColorFilter(Color.GRAY, PorterDuff.Mode.LIGHTEN);
                                scribbleView.mPenStroke = PenStrokes.NORMAL_PEN;
                                break;
                            case 4:
                                ((ImageView) ((GridView) parent).getChildAt(3)).setColorFilter(null);
                                ((ImageView) ((GridView) parent).getChildAt(2)).setColorFilter(null);
                                ((ImageView) view).setColorFilter(Color.GRAY, PorterDuff.Mode.LIGHTEN);
                                scribbleView.mPenStroke = PenStrokes.ERASER;
                                break;
                            case 3:
                                ((ImageView) ((GridView) parent).getChildAt(2)).setColorFilter(null);
                                ((ImageView) ((GridView) parent).getChildAt(4)).setColorFilter(null);
                                ((ImageView) view).setColorFilter(Color.GRAY, PorterDuff.Mode.LIGHTEN);
                                scribbleView.mPenStroke = PenStrokes.PERMANENT_PEN;
                                break;
                            case 5:
                                Log.d("XML Producing", XMLHandler.pathsToXML(scribbleView.mPermanentPaths,
                                        scribbleView.getWidth(), scribbleView.getHeight(),
                                        scribbleView.getLeftPadding()));
                                service.write(String.valueOf(Constants.SAVE_INK) + "," + XMLHandler.pathsToXML(scribbleView.mPermanentPaths,
                                        scribbleView.getWidth(), scribbleView.getHeight(),
                                        scribbleView.getLeftPadding()) + "\r\n");
                                break;
                            case 6:
                                Log.d("Slide operation", "creating new slide");
                                service.write(String.valueOf(Constants.NEW_SLIDE + "," + scribbleView.mSlideNumber + "\r\n"));
                        }
                    }
                } else {
                    Toast.makeText(PresentationActivity.this, "Please Wait For Connection!", Toast.LENGTH_SHORT);
                }
            }
        });
  }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class ButtonsAdapter extends BaseAdapter {
        private Context mContext;
        public ButtonsAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(3, 3, 3, 3);
            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        private Integer[] mThumbIds = {
                R.drawable.prev, R.drawable.next,
                R.drawable.pencil, R.drawable.pen,
                R.drawable.rubber, R.drawable.save,
                R.drawable.new_slide
        };
    }
}
