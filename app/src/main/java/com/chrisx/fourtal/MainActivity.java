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
    static Bitmap dot, cross, finish;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface re;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";

    private Player player;
    private Maze maze;
    private boolean four = true;
    private String[] tests_4 = {
            "#CDBFCAEFBAGEGD#",
            "#EGCCFDEFGDABAB#",
            "#GCBEDDCAAFBEGF#",
            "#DABGBCDGECAEFF#",
            "#CCGFDGABBFEAED#",
            "#BFGACEGBAFDECD#",
            "#EADCGGBCDFBAEF#",
            "#GDECFEBCFGADAB#",
            "#FFBGECADEGABCD#",
            "#BACAFBGGDDECFE#",
            "#BABDDACGCEFEGF#",
            "#DBDBEGFECAAFGC#",
            "#FADCEGCBEFBDGA#",
            "#GEDABFEAFGDBCC#",
            "#FEDCADACGEBFGB#",
            "#AEFCGBECBDFAGD#",
            "#FGCDEGACEBBDFA#",
            "#DABCEFCGADEBFG#",
            "#GFFGEDCBBEAADC#",
            "#FEACEDGACGBFDB#",
            "#EEDBGAFAGBCDCF#",
            "#GFBGADCFEDCABE#",
            "#GDBAECCDABFGEF#",
            "#GECEGABDBFFCAD#",
            "#ACGFFGBACEDBED#",
            "#DECCDEABAGFBGF#",
            "#CGFEDBBEDAGACF#",
            "#BECBAGDAECDGFF#",
            "#GFBGCCFDADAEBE#",
            "#FEGAFEBDDGCABC#",
            "#FBAFGDCBEGADEC#",
            "#FBCEACDGDEAFGB#",
            "#CEBAAFGDCDGFEB#",
            "#CAECFGGBFEDBAD#",
            "#GFABDABGEEDFCC#",
            "#FEGAFBDCDBGACE#",
            "#GEFBGDCCADFABE#",
            "#BAFBEDCGEACGFD#",
            "#ABFEDBGGDCFAEC#",
            "#ABAGEFBGCCEFDD#",
            "#FCBCDDAFAGGBEE#",
            "#FFEGADCBCDAEGB#",
            "#GEABDABGEFDFCC#",
            "#EDFDEBFBAGGACC#",
            "#BCBCGAAFGEDFED#",
            "#BCGDGAEDFACBFE#",
            "#GFACCAEGFBEBDD#",
            "#CDDBCBGAEAFEGF#",
            "#GDBGAAEDEBFFCC#",
            "#GABGDFEDAFCECB#",
            "#DGCGADBECEFABF#",
            "#DGAADBGFFEEBCC#",
            "#ADABCDBGCFGFEE#",
            "#FFBEEADGCADCBG#",
            "#EDFEACCDFAGBGB#",
            "#EBDEDGCFACGFBA#",
            "#DBBFFCECDEAGGA#",
            "#GGADABDEFBECFC#",
            "#FEBGEADFDBCGAC#",
            "#GCFBGEADCEAFBD#",
            "#ADFACEBGGDFCBE#",
            "#AEAGGECBCDFBDF#",
            "#GBEFFCCBGEADDA#",
            "#GGBCDCBEDAFAEF#",
            "#ABDECGCGEBFADF#",
            "#CBACFBGDEFADEG#",
            "#EEDDFGBCGCAFBA#",
            "#AACBGGFCDBFDEE#",
            "#CEFGDBBCAGDFAE#",
            "#EGDEBFABGFADCC#",
            "#EFAFEBDGDBCCAG#",
            "#BDBGACDGCEFAEF#",
            "#GBCGABFACEEFDD#",
            "#FGEFAAGCCEDBDB#",
            "#DEADGEBCABFGCF#",
            "#ADAFFGDGCEBCBE#",
            "#GACAEEFBCGDDBF#",
            "#CDGDGAFCFEBAEB#",
            "#DEDACCFAEBGFBG#",
            "#ACACGDGFFEBDBE#",
            "#CDBCGDAAGEFBEF#",
            "#BBFFCGECDADGEA#",
            "#BGDGDACAFECFBE#",
            "#GCCABFGEDABDFE#",
            "#BEEBCFCDFGGADA#",
            "#AFBDGEBDCGFCAE#",
            "#CAGCFDAGBEEFBD#",
            "#ABCGDFEABFCGDE#",
            "#ABEAFGFECBCDGD#",
            "#FCAEBCDEBGDGFA#",
            "#BDGFAGCBEACFED#",
            "#AGBGCBDACDEFFE#",
            "#FDCABBGFGCEADE#",
            "#EBDEGGFCFCDBAA#",
            "#DFADAGFGEBCBEC#",
            "#DDFBBCGFACGAEE#",
            "#BDEBFACDAFECGG#",
            "#CEDAFBACGBFGDE#",
            "#EBDCCDAEAGBGFF#",
            "#GFDGFDECABACEB#",
            "#EAGCCFEDABBGFD#",
            "#ECFGDDBEGFABCA#",
            "#DGBDBGCCEAFEAF#",
            "#BEACGFDCGEDBFA#",
            "#CCBEABEDADFGFG#",
            "#CDGAFGCAEEFDBB#",
            "#AFEAFCBDECGDGB#",
            "#DEADEBBCFGGCAF#",
            "#DGAEGEFBFACDBC#",
            "#ECEAFFACDBGGDB#",
            "#AFGAGFCCBEEDDB#",
            "#DGAEACBDBCFEGF#",
            "#BEDBFEAGCGACFD#",
            "#GEABGFECBADFCD#",
            "#CDBCEGABEAFGDF#",
            "#CECFDABABDGFEG#",
            "#ACCEEAFDGDBFGB#",
            "#CGFADEFCAGBEDB#"
    };
    private String[] tests_6 = {
            "#FEMBMINHQJPKEFOGOJDQCANHIBACLDGPKL#",
            "#BKELBCQPOINHMFAMDCGGDPAJLFOKQHJNEI#",
            "#HOCEKQDHCBIOQBAIFDMNPJEGLKFALMPJGN#",
            "#CLOAGNNJGOCQAHEEKBPIKMMBIDDLFFQJPH#",
            "#EIQHOFPAGGLJKBMOBJENKQPFCHMNADICDL#",
            "#KBEDFEAJCFIQPCMDOONJMIHQGABPLKGLNH#",
            "#QQCKMPFLBCGHODNJKAEIIOGJADNBFPHELM#",
            "#QHPIFLLMAENMKGCEFHQAIBDGKPCONJJOBD#",
            "#BBQKAEIMDOOLNHCLCGJKPAQNDMPGIHFFEJ#",
            "#ICPBOIBKDEEPGGNJOFMMKFDCNQQHAJHLAL#",
            "#PKNHKIDNEOAFIBPBLAEOFHJDCLMGQCMJGQ#",
            "#PBIDJPAOHKOANEDLNBHCCLKGFIGFQMMJEQ#",
            "#MIJDFIMPBHKNJALLONACOGKPFEHCEBDGQQ#",
            "#OAEOPAFHHBMPNBMFECDCLLNGJGIKQQKDJI#",
            "#GGLHAFQINCKCALBOBKDJJDEFMIOEPQNMHP#",
            "#CFPDFCMDIBJLJHIHGNAGBMELPOKAQNEKQO#",
            "#MDLQDCMBENGCAOHOGJBFQKEAJHKIIPNPFL#",
            "#PFLJPJGIBFLDAOCDNNCGIMEMHAHQEKQBOK#",
            "#LBHEKFNACGOKNPMBMEQQPOJLIGHJDFIACD#",
            "#FDILLFHKKEGOPCJBCHAOMQBIEGMNJDNAQP#",
            "#JCGDKJAMDHPAGPQINCNQLFBEOHOILBMKEF#",
            "#FIPHGNKPOAONMQLBECJGIBDKQLAHEFCMJD#",
            "#NAPHLNQBMDOOECLQKMJGFEFDCBJGIHKAPI#",
            "#DMFQODKIONPHPBKCHAIMQGFLBEEGJLNCAJ#",
            "#GNFJQBKQGKIBIOJONFPMDECHCMLDAHPEAL#",
            "#PQOCLMLJAGMNCIEEPOGFFHIQADJNBDKBHK#",
            "#KANDHPBOLGDKQQFBJCPEOMGIEFCMHALIJN#",
            "#MDEDNFPMKOGFNEHHLGOJBBPJLKIAQQCICA#",
            "#LLBJHMBDJOFGMHKEPCGAQAICNIODPKENFQ#",
            "#JKMBOKGHAFPQEDQNPLCGEOMBFILJDHNICA#",
            "#BOODFBDENJQLICQFEAKCNJGHAPPMILGMHK#",
            "#KMHQOLQCEEJNJPABBNCHOGFLAGPKIFIMDD#",
            "#EBBNOODQKHAEGLADFHLPGPFMCJJNIMICKQ#",
            "#JMAQDABBHKCONKJFPCPFQEGOLDIENIHMLG#",
            "#HIJBFHFDNBGDAGPKAMQJQEICEMLOKCNPLO#",
            "#ACPEJAPNMOQBIKLGOIQFHGMDNLEDKHJBCF#",
            "#CGIDECNMAOPNFKBFKGJBIOMQHJDLHQLEPA#",
            "#BGJCQOBQIIELGHAFPPANKHNEKDJOMMDCFL#",
            "#JPGIQPEOQIJBMAHCANLDOFKHMGENBDLFKC#",
            "#JDHQKNJPPHENCDALEOBIOGMQMIFABFGCLK#",
            "#IBGPBECJLIEDFJLCPQHMDOFGAQKOHNMANK#",
            "#HHMKAPKGQLJADFQPNEBCJLCBFIIMEOONGD#",
            "#GAQHNAOMQCDBCPHIJEMILEGFFONJLKPBDK#",
            "#FBEJALIOKNCMJBGLFQGCMEHQONKAPIDHDP#",
            "#IBBNDHHLNGGJJFECKKIQPEPAFMCLMQOOAD#",
            "#OKMLADDGKHFLEABBICINGEJOPNPQHQCFJM#",
            "#DNEQKDKNCQCJGIEJBAOHHOLFFAPMGLMIBP#"
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

        dot = BitmapFactory.decodeResource(res, R.drawable.dot);
        cross = BitmapFactory.decodeResource(res, R.drawable.cross);
        finish = BitmapFactory.decodeResource(res, R.drawable.checkers);

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
        player = new Player(maze);


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
                            player.draw();
                            drawMoves();

                            if (player.atEnd()) {
                                if (four) maze = new Maze(tests_4[(int)(Math.random()*tests_4.length)]);
                                else maze = new Maze(tests_6[(int)(Math.random()*tests_6.length)]);

                                player.setMaze(maze);
                                player.reset();
                            } else if (player.outOfMoves()) {
                                player.reset();
                            }

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

        if (action == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < maze.size()*maze.size(); i++) {
                float w = w() / maze.size();
                float x = (i%maze.size()) * w,
                        y = h()/2 + (i/maze.size()-maze.size()/2) * w;
                if (X > x && X < x+w && Y > y && Y < y+w) {
                    int diff = Math.abs(player.getPos() - i);
                    if (diff == maze.size() || diff == 1) player.goTo(i);
                }
            }

            if (four && X > w()/2 && Y < h()/2-w()/2) {
                four = false;
                maze = new Maze(tests_6[(int)(Math.random()*tests_6.length)]);
                player.setMaze(maze);
            } else if (!four && X < w()/2 && Y < h()/2-w()/2) {
                four = true;
                maze = new Maze(tests_4[(int)(Math.random()*tests_4.length)]);
                player.setMaze(maze);
            }
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

    private void drawMoves() {
        float y = h() - (h()-w())/4,
                r = c480(25),
                margin = c480(60);

        canvas.save();
        canvas.translate(0, y);
        canvas.translate(margin, 0);
        for (int i = 0; i < maze.getMoves(); i++) {
            if (i < player.getMoves()) {
                canvas.drawBitmap(cross, null, new RectF(-r, -r, r, r), null);
            } else {
                canvas.save();
                canvas.rotate(frameCount * 2 % 360);
                canvas.drawBitmap(dot, null, new RectF(-r, -r, r, r), null);
                canvas.restore();
            }
            canvas.translate((w()-margin*2)/(maze.getMoves()-1), 0);
        }
        canvas.restore();
    }
}
