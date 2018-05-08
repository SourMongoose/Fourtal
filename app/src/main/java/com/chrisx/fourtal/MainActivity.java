package com.chrisx.fourtal;

/**
 * Organized in order of priority:
 * @TODO everything
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private Bitmap bmp;
    static Canvas canvas;
    private LinearLayout ll;
    private float scaleFactor;

    static Bitmap[] portals4, portals6;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface re;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";

    private Maze maze;
    private String[] tests_4 = {
            "#CDBFCAEFBAGEGD#",
            "#EGCCFDEFGDABAB#",
            "#GCBEDDCAAFBEGF#",
            "#DABGBCDGECAEFF#",
            "#CCGFDGABBFEAED#",
            "#BFGACEGBAFDECD#",
            "#EADCGGBCDFBAEF#"
    };

    //frame data
    private static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;

    private float downX, downY;

    private Paint title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //creates the bitmap
        //note: Star 4.5 is 480x854
        int targetH = 854,
                wpx = Resources.getSystem().getDisplayMetrics().widthPixels,
                hpx = Resources.getSystem().getDisplayMetrics().heightPixels;
        scaleFactor = Math.min(1,(float)targetH/hpx);
        bmp = Bitmap.createBitmap(Math.round(wpx*scaleFactor),Math.round(hpx*scaleFactor),Bitmap.Config.RGB_565);

        //creates canvas
        canvas = new Canvas(bmp);

        ll = (LinearLayout) findViewById(R.id.draw_area);
        ll.setBackgroundDrawable(new BitmapDrawable(bmp));

        Resources res = getResources();
        portals4 = new Bitmap[19];
        portals6 = new Bitmap[portals4.length];
        portals4[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.portalwhite),
                Math.round(w()/4),Math.round(w()/4),false);
        portals6[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.portalwhite),
                Math.round(w()/6),Math.round(w()/6),false);
        for (int i = 1; i < portals4.length; i++) {
            portals4[i] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.portal00+i-1),
                    Math.round(w()/4),Math.round(w()/4),false);
            portals6[i] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.portal00+i-1),
                    Math.round(w()/6),Math.round(w()/6),false);
        }

        //initializes SharedPreferences
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        nanosecondsPerFrame = (long)1e9 / FRAMES_PER_SECOND;

        //initialize fonts
        re = Typeface.createFromAsset(getAssets(), "fonts/Rounded_Elegance.ttf");

        canvas.drawColor(Color.BLACK);

        //pre-defined paints
        title = newPaint(Color.WHITE);

        maze = new Maze(tests_4[(int)(Math.random()*tests_4.length)]);


        final Handler handler = new Handler();

        //Update thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });

                    frameCount++;

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();

        //UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                drawTitleMenu();

                //draw loop
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            canvas.drawColor(Color.BLACK);
                            maze.draw();

                            //update canvas
                            ll.invalidate();
                        }
                    });

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    //handles touch events
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX()*scaleFactor;
        float Y = event.getY()*scaleFactor;
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            maze = new Maze(tests_4[(int)(Math.random()*tests_4.length)]);
        }

        return true;
    }

    //shorthand for w() and h()
    static float w() {
        return canvas.getWidth();
    }
    static float h() {
        return canvas.getHeight();
    }

    //creates an instance of Paint set to a given color
    private Paint newPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(re);

        return p;
    }

    static float c480(float f) {
        return w() / (480 / f);
    }
    static float c854(float f) {
        return h() / (854 / f);
    }

    private long getHighScore() {
        return sharedPref.getInt("high_score", 0);
    }

    private double toRad(double deg) {
        return Math.PI/180*deg;
    }

    private void drawTitleMenu() {

    }
}
