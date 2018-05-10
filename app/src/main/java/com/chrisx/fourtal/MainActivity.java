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
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private Bitmap bmp;
    static Canvas canvas;
    private LinearLayout ll;
    private float scaleFactor;

    static Bitmap[] portals4, portals6;
    static Bitmap dot, cross, finish, green, blue, gray, triangle, restart, back;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface re;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";
    private String gamemode;
    private int page = 0;
    private int level;

    private Player player;
    private Maze maze;
    private String[] mazes_4x4_4, mazes_6x6_5, mazes_6x6_6;

    private float bannerWidth;

    private float levelsMargin;
    private int levelsPerRow = 3, rowsPerPage = 4;

    //frame data
    private static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;

    private int TRANSITION_MAX = FRAMES_PER_SECOND * 2 / 3;
    private int transition = TRANSITION_MAX / 2;

    private float downX, downY;

    private int background = Color.rgb(20,20,20);
    private Paint title, start, banner, shadow, mode, steps, bg, ls;

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

        levelsMargin = c480(20);
        int tmp = Math.round((h()-c854(250))/rowsPerPage);
        green = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.square_green),
                tmp, tmp, false);
        blue = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.square_blue),
                tmp, tmp, false);
        gray = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.square_gray),
                tmp, tmp, false);

        triangle = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.triangle),
                Math.round(c854(75)), Math.round(c854(75)), false);
        restart = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.restart),
                Math.round(c854(50)), Math.round(c854(50)), false);
        back = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.back),
                Math.round(c854(50)), Math.round(c854(50)), false);

        //initializes SharedPreferences
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        nanosecondsPerFrame = (long)1e9 / FRAMES_PER_SECOND;

        //initialize fonts
        re = Typeface.createFromAsset(getAssets(), "fonts/Rounded_Elegance.ttf");

        //pre-defined paints
        title = newPaint(Color.WHITE);
        title.setTextAlign(Paint.Align.CENTER);
        title.setTextSize(c480(100));

        start = new Paint(title);
        start.setTextSize(c480(50));

        int bannerColor = Color.rgb(0,170,255);
        int shadowColor = Color.rgb(Color.red(bannerColor)/2,Color.green(bannerColor)/2,Color.blue(bannerColor)/2);
        banner = newPaint(bannerColor);
        shadow = newPaint(shadowColor);
        bannerWidth = c854(120);

        mode = new Paint(title);
        mode.setTextSize(c854(70));
        steps = new Paint(mode);
        steps.setColor(Color.rgb(220,220,240));
        steps.setTextSize(c854(30));

        bg = newPaint(background);

        ls = new Paint(title);
        ls.setTextSize(c854(50));

        initMazes();


        final Handler handler = new Handler();

        //Update thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                if (transition < TRANSITION_MAX / 2) {
                                    if (menu.equals("game")) {
                                        if (player.atEnd()) {
                                            editor.putBoolean("completed_"+gamemode+"_"+level, true);
                                            editor.apply();

                                            int maxLevel = nLevels();
                                            if (level != maxLevel) goToLevel(level+1);
                                            else goToMenu("levels");
                                        } else if (player.outOfMoves()) {
                                            player.reset();
                                        }
                                    }
                                }

                                //fading transition effect
                                if (transition > 0) {
                                    transition--;
                                }
                            }
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
                //draw loop
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                if (transition < TRANSITION_MAX / 2) {
                                    canvas.drawColor(background);

                                    if (menu.equals("start")) {
                                        drawTitleMenu();
                                    } else if (menu.equals("modes")) {
                                        drawModes();
                                    } else if (menu.equals("levels")) {
                                        drawLevels();
                                    } else if (menu.equals("game")) {
                                        maze.draw();
                                        if (transition == 0) player.draw();
                                        drawMoves();

                                        canvas.drawText(level+"",w()/2,c854(50)-(mode.ascent()+mode.descent())/2,mode);

                                        canvas.save();
                                        canvas.translate(c854(50),c854(50));
                                        canvas.drawBitmap(back,-c854(25),-c854(25),null);
                                        canvas.translate(w()-c854(100),0);
                                        canvas.drawBitmap(restart,-c854(25),-c854(25),null);
                                        canvas.restore();
                                    }
                                }

                                //fading transition effect
                                if (transition > 0) {
                                    int t = TRANSITION_MAX / 2, alpha;
                                    if (transition > t) {
                                        alpha = 255 - 255 * (transition - t) / t;
                                    } else {
                                        alpha = 255 - 255 * (t - transition) / t;
                                    }
                                    canvas.drawColor(Color.argb(alpha, 20, 20, 20));
                                }

                                //update canvas
                                ll.invalidate();
                            }
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
        if (menu.equals("modes")) {
            goToMenu("start");
        } else if (menu.equals("levels")) {
            goToMenu("modes");
        } else if (menu.equals("game")) {
            goToMenu("levels");
        }
    }

    @Override
    //handles touch events
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX()*scaleFactor;
        float Y = event.getY()*scaleFactor;
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            downX = X;
            downY = Y;
        }

        if (menu.equals("start")) {
            if (action == MotionEvent.ACTION_UP) {
                goToMenu("modes");
            }
        } else if (menu.equals("modes")) {
            if (action == MotionEvent.ACTION_UP) {
                if (Y > h()/4-bannerWidth/2 && Y < h()/4+bannerWidth/2
                        && downY > h()/4-bannerWidth/2 && downY < h()/4+bannerWidth/2) {
                    gamemode = "4x4_4";
                    goToMenu("levels");
                } else if (Y > h()/2-bannerWidth/2 && Y < h()/2+bannerWidth/2
                        && downY > h()/2-bannerWidth/2 && downY < h()/2+bannerWidth/2) {
                    gamemode = "6x6_5";
                    goToMenu("levels");
                } else if (Y > h()*3/4-bannerWidth/2 && Y < h()*3/4+bannerWidth/2
                        && downY > h()*3/4-bannerWidth/2 && downY < h()*3/4+bannerWidth/2) {
                    gamemode = "6x6_6";
                    goToMenu("levels");
                }

                if (Y > h()-c854(100) && X > w()/2-c854(50) && X < w()/2+c854(50)) onBackPressed();
            }
        } else if (menu.equals("levels")) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (X < c854(150) && Y < c854(150)) {
                    page = Math.max(0,page-1);
                } else if (X > w()-c854(150) && Y < c854(150)) {
                    int maxPage = (nLevels()) / (levelsPerRow*rowsPerPage);
                    page = Math.min(page+1,maxPage);
                }

                int lpr = levelsPerRow, rpp = rowsPerPage;
                float w = (h()-c854(250))/rpp;
                float lm = (w()-lpr*w)/2;

                int maxPages = nLevels();
                for (int i = page*lpr*rpp; i < Math.min((page+1)*lpr*rpp,maxPages); i++) {
                    float topX = lm+i%lpr*w, topY = c854(150)+(i-page*lpr*rpp)/lpr*w;
                    if (X > topX && X < topX+w && Y > topY && Y < topY+w && unlocked(i+1)) {
                        goToMenu("game");
                        goToLevel(i+1);
                    }
                }
            } else if (action == MotionEvent.ACTION_UP) {
                if (Y > h()-c854(100) && X > w()/2-c854(50) && X < w()/2+c854(50)) onBackPressed();
            }
        } else if (menu.equals("game")) {
            if (action == MotionEvent.ACTION_DOWN) {
                if (X > w()-c854(100) && Y < c854(100)) player.reset();

                for (int i = 0; i < maze.size() * maze.size(); i++) {
                    float w = w() / maze.size();
                    float x = (i % maze.size()) * w,
                            y = h() / 2 + (i / maze.size() - maze.size() / 2) * w;
                    if (X > x && X < x + w && Y > y && Y < y + w) {
                        int diff = Math.abs(player.getPos() - i);
                        if (diff == maze.size() || diff == 1) player.goTo(i);
                    }
                }
            } else if (action == MotionEvent.ACTION_UP) {
                if (X < c854(100) && Y < c854(100)) onBackPressed();
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

    private int nLevels() {
        return gamemode.equals("4x4_4") ? 150 : 100;
    }

    private boolean completed(int level) {
        return sharedPref.getBoolean("completed_"+gamemode+"_"+level, false);
    }
    private boolean unlocked(int level) {
        int nMissing = 0;
        for (int i = 1; i < level; i++) {
            if (!completed(i)) nMissing++;
        }
        return nMissing < 5;
    }

    static float c480(float f) {
        return w() / (480 / f);
    }
    static float c854(float f) {
        return h() / (854 / f);
    }

    private void tri(float x1, float y1, float x2, float y2, float x3, float y3, Paint p) {
        p.setStyle(Paint.Style.FILL);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close();

        canvas.drawPath(path, p);
    }
    private void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, Paint p) {
        p.setStyle(Paint.Style.FILL);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x4, y4);
        path.close();

        canvas.drawPath(path, p);
    }

    private double toRad(double deg) {
        return Math.PI/180*deg;
    }

    private void goToMenu(String s) {
        transition = TRANSITION_MAX;

        if (s.equals("levels")) {
            if (menu.equals("modes")) {
                page = 0;
                int i = 1;
                while (completed(i) && i <= nLevels()) i++;
                if (i <= nLevels()) page = (i-1)/(rowsPerPage*levelsPerRow);
            }
        }

        menu = s;
    }

    private void goToLevel(int i) {
        level = i;
        if (gamemode.equals("4x4_4")) maze = new Maze(mazes_4x4_4[i-1]);
        else if (gamemode.equals("6x6_5")) maze = new Maze(mazes_6x6_5[i-1]);
        else if (gamemode.equals("6x6_6")) maze = new Maze(mazes_6x6_6[i-1]);
        player = new Player(maze);
        player.setAnimation(FRAMES_PER_SECOND/2);
    }

    private void initMazes() {
        mazes_4x4_4 = new String[]{
                "#DABGBCDGECAEFF#",
                "#CCGFDGABBFEAED#",
                "#BECBAGDAECDGFF#",
                "#GFBGCCFDADAEBE#",
                "#FEGAFBDCDBGACE#",
                "#BAFBEDCGEACGFD#",
                "#GABGDFEDAFCECB#",
                "#EBDEDGCFACGFBA#",
                "#ADFACEBGGDFCBE#",
                "#CBACFBGDEFADEG#",
                "#EGDEBFABGFADCC#",
                "#GBCGABFACEEFDD#",
                "#FGEFAAGCCEDBDB#",
                "#BEEBCFCDFGGADA#",
                "#ABEAFGFECBCDGD#",
                "#CEDAFBACGBFGDE#",
                "#GFDGFDECABACEB#",
                "#DEADEBBCFGGCAF#",
                "#AFGAGFCCBEEDDB#",
                "#BEDBFEAGCGACFD#",
                "#BCECDEGAFFDGBA#",
                "#EECAAGDFGCDBFB#",
                "#AGCABFDCGEFDBE#",
                "#AFCADBGDEGBFEC#",
                "#CFDCBGEGBFEAAD#",
                "#GCBEDDCAAFBEGF#",
                "#BFGACEGBAFDECD#",
                "#BABDDACGCEFEGF#",
                "#DBDBEGFECAAFGC#",
                "#GEDABFEAFGDBCC#",
                "#FEDCADACGEBFGB#",
                "#AEFCGBECBDFAGD#",
                "#GFFGEDCBBEAADC#",
                "#GFBGADCFEDCABE#",
                "#GDBAECCDABFGEF#",
                "#DECCDEABAGFBGF#",
                "#FBAFGDCBEGADEC#",
                "#CAECFGGBFEDBAD#",
                "#GFABDABGEEDFCC#",
                "#ABAGEFBGCCEFDD#",
                "#GEABDABGEFDFCC#",
                "#EDFDEBFBAGGACC#",
                "#BCBCGAAFGEDFED#",
                "#GFACCAEGFBEBDD#",
                "#GDBGAAEDEBFFCC#",
                "#DGAADBGFFEEBCC#",
                "#FFBEEADGCADCBG#",
                "#EDFEACCDFAGBGB#",
                "#AEAGGECBCDFBDF#",
                "#GBEFFCCBGEADDA#",
                "#GGBCDCBEDAFAEF#",
                "#BDBGACDGCEFAEF#",
                "#DEADGEBCABFGCF#",
                "#CDBCGDAAGEFBEF#",
                "#BGDGDACAFECFBE#",
                "#CAGCFDAGBEEFBD#",
                "#AGBGCBDACDEFFE#",
                "#EBDEGGFCFCDBAA#",
                "#DFADAGFGEBCBEC#",
                "#DDFBBCGFACGAEE#",
                "#BDEBFACDAFECGG#",
                "#EBDCCDAEAGBGFF#",
                "#DGBDBGCCEAFEAF#",
                "#CDGAFGCAEEFDBB#",
                "#AFEAFCBDECGDGB#",
                "#DGAEGEFBFACDBC#",
                "#GEABGFECBADFCD#",
                "#CDBCEGABEAFGDF#",
                "#ACCEEAFDGDBFGB#",
                "#CGFADEFCAGBEDB#",
                "#EFCEDBAFGDCGBA#",
                "#ACEGADCFBBEDGF#",
                "#GEBGADFECCDBAF#",
                "#CEDAFEGCGBDAFB#",
                "#DEFEBACGAFCDGB#",
                "#FABFEECGCBDAGD#",
                "#EEABDFFAGDCBGC#",
                "#FGFECDGCADBEAB#",
                "#ABECGFBFDCGDAE#",
                "#EAFECDADCBGFBG#",
                "#ACGADEFEBFBDCG#",
                "#GFBDGFEBAECDAC#",
                "#DGBDCAFBEFECGA#",
                "#EGCCFDEFGDABAB#",
                "#FFBGECADEGABCD#",
                "#BACAFBGGDDECFE#",
                "#FEACEDGACGBFDB#",
                "#GECEGABDBFFCAD#",
                "#ACGFFGBACEDBED#",
                "#CEBAAFGDCDGFEB#",
                "#ABFEDBGGDCFAEC#",
                "#FCBCDDAFAGGBEE#",
                "#FFEGADCBCDAEGB#",
                "#BCGDGAEDFACBFE#",
                "#CDDBCBGAEAFEGF#",
                "#DGCGADBECEFABF#",
                "#DBBFFCECDEAGGA#",
                "#FEBGEADFDBCGAC#",
                "#ABDECGCGEBFADF#",
                "#AACBGGFCDBFDEE#",
                "#EFAFEBDGDBCCAG#",
                "#ADAFFGDGCEBCBE#",
                "#CDGDGAFCFEBAEB#",
                "#DEDACCFAEBGFBG#",
                "#GCCABFGEDABDFE#",
                "#AFBDGEBDCGFCAE#",
                "#ABCGDFEABFCGDE#",
                "#FDCABBGFGCEADE#",
                "#ECFGDDBEGFABCA#",
                "#BEACGFDCGEDBFA#",
                "#CCBEABEDADFGFG#",
                "#DGAEACBDBCFEGF#",
                "#CBEAAECFBFGDGD#",
                "#EEAAGGCBFCBFDD#",
                "#BBADGGEFECCADF#",
                "#CFBFGGDBCAEADE#",
                "#DEADBFCABEGGFC#",
                "#BBEECFCFDDGGAA#",
                "#EFCFGDBCGBAEDA#",
                "#FEGEDADGBBCFAC#",
                "#GACAFDDEBGFBCE#",
                "#CDBFCAEFBAGEGD#",
                "#EADCGGBCDFBAEF#",
                "#GDECFEBCFGADAB#",
                "#DABCEFCGADEBFG#",
                "#EEDBGAFAGBCDCF#",
                "#CGFEDBBEDAGACF#",
                "#FEGAFEBDDGCABC#",
                "#FBCEACDGDEAFGB#",
                "#GEFBGDCCADFABE#",
                "#ADABCDBGCFGFEE#",
                "#GGADABDEFBECFC#",
                "#GCFBGEADCEAFBD#",
                "#EEDDFGBCGCAFBA#",
                "#CEFGDBBCAGDFAE#",
                "#GACAEEFBCGDDBF#",
                "#ACACGDGFFEBDBE#",
                "#BBFFCGECDADGEA#",
                "#FCAEBCDEBGDGFA#",
                "#BDGFAGCBEACFED#",
                "#EAGCCFEDABBGFD#",
                "#ECEAFFACDBGGDB#",
                "#CECFDABABDGFEG#",
                "#BADFCCBEGFEGDA#",
                "#ADFBBCEAECGFGD#",
                "#FADCEGCBEFBDGA#",
                "#FGCDEGACEBBDFA#",
                "#AGBGCCFEEDDBAF#",
                "#EADCEBGBAFFDCG#",
                "#DFCAEGBGFAECDB#"
        };
        mazes_6x6_5 = new String[]{
                "#KBJIIKGAFQNGLMEPOENCPHJFCLHADOQBMD#",
                "#MDEDNFPMKOGFNEHHLGOJBBPJLKIAQQCICA#",
                "#JQFDIGBGNDCIFJPHBHOQMKCPLOKLENMEAA#",
                "#LLBJHMBDJOFGMHKEPCGAQAICNIODPKENFQ#",
                "#GQJDJLFMOCMGEBEQPBAHNPNHACOKDFIILK#",
                "#CGIDECNMAOPNFKBFKGJBIOMQHJDLHQLEPA#",
                "#AAQGEFEFOLBBJGCPDOQMNLPHMKDJINHCIK#",
                "#EENBKAPHLDNIMLFPJAODQIKCCHJFGQOBMG#",
                "#FHIACFMGHIQOOLDNJNLGPBCEEMPAKBQJDK#",
                "#MOGECBICKDFPAOFLDAEIKNGQPHJHNMBQJL#",
                "#NDEFKAHAICMCMJOHGDBOKPPQBIJFLQNGEL#",
                "#BGQMCBIELAGEKAKFHQILJNDPFPOJNHMCOD#",
                "#GLQMEBBDPQIJPAJLDOGFFEINAOCMHCKKNH#",
                "#LCFMOIILGPPCDEFQJOHBAKJMAEHNQGBDNK#",
                "#CQPOLCQAKPHIEILJGDAMDGMFONHBNFKEJB#",
                "#HIJBFHFDNBGDAGPKAMQJQEICEMLOKCNPLO#",
                "#GAQHNAOMQCDBCPHIJEMILEGFFONJLKPBDK#",
                "#QBJQFBHGCKODNAJDCGMEIIFHPPEKLMANLO#",
                "#JPGIQPEOQIJBMAHCANLDOFKHMGENBDLFKC#",
                "#DNEQKDKNCQCJGIEJBAOHHOLFFAPMGLMIBP#",
                "#KIMFEKNIHDQCPGMCLHJONJPFLBDAEGQAOB#",
                "#OKMLADDGKHFLEABBICINGEJOPNPQHQCFJM#",
                "#IEQECMLMQDOHAAIHGBCNLNPJDFGPFBOKJK#",
                "#KMDQCGKBJAINFQADGLPJNFMEBCPLOHIHEO#",
                "#BOODFBDENJQLICQFEAKCNJGHAPPMILGMHK#",
                "#JDHQKNJPPHENCDALEOBIOGMQMIFABFGCLK#",
                "#AQDDPHCBHAPFILKINGLBQJOMJKFGOMEECN#",
                "#FAPMCAHEEJNQOFOBCHJDMILGGBPKLDQKNI#",
                "#PKOQLPFLJDGHCHFQNAGKIMEMEDIJNOBACB#",
                "#GMDHPLGPOLNAKIJHFANBCKDBCFQMEQOJIE#",
                "#JLKCOJHGADNQKMPOGFELHEDAIIBBFCMPQN#",
                "#GDHGHPAPCBMEAIQNDLCKNQLOEOKFJIBMFJ#",
                "#HFGLMHKQACMGAOBKNNQBOFDCJDELIEIPJP#",
                "#KGFOEKAHQLCADNGPMCJNDFBHQOEPBLIJMI#",
                "#IBBNDHHLNGGJJFECKKIQPEPAFMCLMQOOAD#",
                "#ODEPBLKPOGCEQMABIQKJNNMALGDIHCFFJH#",
                "#JAIIQFJKCLPGHBOCBFEQMKAHGDMNOENLDP#",
                "#IBFANBPQEFILGDCMNHJCHKPLQMOKDEAJGO#",
                "#AGKNINHEEACHFQIMQBDBLMCOPFGKJLDOJP#",
                "#ACPFQKAMDILMLOOPBJDKCHNFEGBEHGIJQN#",
                "#PADNGNFMEMQFGEDKIAHBHOICCJBOJQPKLL#",
                "#BPGBPCEGAKDKJHEHLMNOIIFJDFMQLCANQO#",
                "#KBEDFEAJCFIQPCMDOONJMIHQGABPLKGLNH#",
                "#OLNNELPPCIBDMOJHJEGKKHFMQQIBDAFGAC#",
                "#EKPGQAPGHFFKIECIOBMLOACDHDNQBJNMLJ#",
                "#KMHQOLQCEEJNJPABBNCHOGFLAGPKIFIMDD#",
                "#IPBCICGJMPBOKANMEGAKHQNODEJFLLQHDF#",
                "#KEJIDCPFBEQOFCBAGKPJHDAGNLNILOQMHM#",
                "#MAIDLFMJQGCHOCFDKPQKBNEIGLPENJHBOA#",
                "#HOCEKQDHCBIOQBAIFDMNPJEGLKFALMPJGN#",
                "#KANDHPBOLGDKQQFBJCPEOMGIEFCMHALIJN#",
                "#ACPEJAPNMOQBIKLGOIQFHGMDNLEDKHJBCF#",
                "#MAJICNGBOANMKJEHGPEBCLDKDOQQIPHLFF#",
                "#FLEAFLEJQHMONKDPAPMKDNGOCJHGIBIBCQ#",
                "#FCQAKOMLDIJMOFHEPGLIJHNEBANDQCGBPK#",
                "#JQFIEJFQMCAHDLMKNAOGBPKCOBNIEHLPDG#",
                "#AQHQEBJLPHDKOFOMILJFNPKMCBGIDGACNE#",
                "#QFCLEMALIOFDAHKIPHDJPKBNJCQEGMNBOG#",
                "#JMAQDABBHKCONKJFPCPFQEGOLDIENIHMLG#",
                "#GHIBDJPPAIKHFBMDEJKFEAQGLNCNQLOOCM#",
                "#JFHPIDFKDOQGPOECKGMCEQJNBLIHALBNAM#",
                "#MAHAPQQLGIBFJKLEOGDDMKHBEFJCPNNCIO#",
                "#DBFLJGDQKICAPKENLHJIOMMCEHNGAPQOFB#",
                "#NPOKHQBLHGKOCMDIBIFQLANGACFMJJEEPD#",
                "#FEMBMINHQJPKEFOGOJDQCANHIBACLDGPKL#",
                "#PQOCLMLJAGMNCIEEPOGFFHIQADJNBDKBHK#",
                "#IBGPBECJLIEDFJLCPQHMDOFGAQKOHNMANK#",
                "#HHMKAPKGQLJADFQPNEBCJLCBFIIMEOONGD#",
                "#FBEJALIOKNCMJBGLFQGCMEHQONKAPIDHDP#",
                "#DNCQOPNDJLHEGQCFMFIHOBKLGPJMAEKIBA#",
                "#DAJEPNNGQFODCKLOKMHAPIIBQEFMJBGLCH#",
                "#PEDJLMQONLBPFIBENHDKAKGCIJQCGOHMFA#",
                "#EBBNOODQKHAEGLADFHLPGPFMCJJNIMICKQ#",
                "#BHBGQLOCDLQJPOGEANNMKIFDFPIKJEHCAM#",
                "#DNJQOAIMKICPFLPDEHKEAMHNFOQGBJLCBG#",
                "#NQPBEEHJQGNMLLPCIAFFDDGIKBAKCJOHMO#",
                "#CHGCNNDDJBJKOFLOGFQPLMMIAPEKIHQEBA#",
                "#BKELBCQPOINHMFAMDCGGDPAJLFOKQHJNEI#",
                "#CLOAGNNJGOCQAHEEKBPIKMMBIDDLFFQJPH#",
                "#EIQHOFPAGGLJKBMOBJENKQPFCHMNADICDL#",
                "#GLMEKEKDQJAOIDFMNCNAHOJCPFQBHILPGB#",
                "#CPELFDIKJOFPENIMOHBNKMGACAGQJBHQDL#",
                "#NEPQCCMOBDAJINFHALFEOQPDJIBLGHKMGK#",
                "#OPQJCDJEGAHNPFIKELCGABBIOQKLMFHDNM#",
                "#BNQOIHHCJDEKBCMFAODMELPGLJPNQIKFAG#",
                "#CCONHFEPAJQMINDLBBIKDLEOJHKAGFMPQG#",
                "#QQCKMPFLBCGHODNJKAEIIOGJADNBFPHELM#",
                "#GLIBNOEKEKJQDCGPBPQCDLMFMOFIHJHAAN#",
                "#IGLKHHAEJBNMDOJFPGALIQCPBEOKCDQFNM#",
                "#LJMMQFOCQDCKEEBOKNBGHPGHPFAADJIILN#",
                "#JKMBOKGHAFPQEDQNPLCGEOMBFILJDHNICA#",
                "#DCKMELDIINNJBHQJAFCKEHGAOPPBMQGOLF#",
                "#OJANFHACLIQNPKMLMDFEQKPHEDOCBGGJIB#",
                "#JFAIMFQLNOEBKGPEHCDMKNCIAHDOPJLQGB#",
                "#OKIFNLDPKAMFPCGOMLNBEIJACBEHQJGDHQ#",
                "#MCBMPFCKADPIQLEQLEBKGJFOHJGHOINADN#",
                "#BGJCQOBQIIELGHAFPPANKHNEKDJOMMDCFL#",
                "#BEQJOIIEALNDCGBAMKHNPLPGJQCFOFKHMD#",
                "#OGHBKQCAHEMNFJBILCILGAOEPJPQNDMFKD#",
                "#IFHEQJNBLNJKPGPCKBCDGQFIHMDLAEOOAM#"
        };
        mazes_6x6_6 = new String[]{
                "#CLIPPCAEODJAHEINGLOFKGQHBBMKQFNDJM#",
                "#LCCHNLEQQMMENDJDAJKOGBFOKGAPBIFIHP#",
                "#PBIDJPAOHKOANEDLNBHCCLKGFIGFQMMJEQ#",
                "#GGLHAFQINCKCALBOBKDJJDEFMIOEPQNMHP#",
                "#JCGDKJAMDHPAGPQINCNQLFBEOHOILBMKEF#",
                "#OIGABOJCQFBIJCKAFGKEPNHQMPEMLHLNDD#",
                "#LGQFKLGBNOHDPDOMCFJJPQIACNIMAKBHEE#",
                "#HDQGJHFLACNCFPAMKQMNBKOGPBDIEOLIJE#",
                "#PPLDHLIICCEOBBOGJFMQQMKFGNAEJNHDAK#",
                "#OJCLGOJDDHLNGBPKNFIFKEIHEQCAMBPAQM#",
                "#CGDPQCQOIOGEBJPLAENANHMJBDMKKILHFF#",
                "#OCEADJJDQLPOKHLPHCKGBBQEINIMFGAMNF#",
                "#KGFHAKFEMMJDIIJEOHGPQNQBPOBNLCADCL#",
                "#QOGJMGFLDQNEKJOMAAKPPCFNIHLCBDHIEB#",
                "#MDLQDCMBENGCAOHOGJBFQKEAJHKIIPNPFL#",
                "#FDILLFHKKEGOPCJBCHAOMQBIEGMNJDNAQP#",
                "#GNFJQBKQGKIBIOJONFPMDECHCMLDAHPEAL#",
                "#HHILKDDFNLQENEKCGCOFIBPJMQOMJAGBPA#",
                "#HHAMKBBIJEPKQDLGJFLIGCEDCNOAMFONQP#",
                "#JHNJAHPQILABGCQCGNKKFIEBDDMMOLFPEO#",
                "#QPPDOCHLGHDNIBBQKLMIKFEAMFEJOCAGNJ#",
                "#KLLHEKNPAIPJGBJDDNCMCBFQOMAGEOHFQI#",
                "#AAHMKNQHNJLPJPCEIOFCBBEDQODLGMFKIG#",
                "#HJBLCHFIADLCKEQMNJKAMFDQNPPBOEIOGG#",
                "#DKBPGAAEFNLGMNQEFJCCIPOJHMKDLQBHOI#",
                "#EAJQOEBGLMNGKFFBKALPQOPIIDCCHDMNJH#",
                "#KNOLBEPOLQJQHDPJFMDFCHAEMBGIGKNACI#",
                "#QNCMCLNOHHKFJGGIMELBJIBPKDAQPFAOED#",
                "#OKKMAOEFEQNCGJJPBFHDAIBHCLGIPQDMLN#",
                "#KQGMBKFGOANFJIEPMQILBDAENJLPDCHCOH#",
                "#DEQIKODGCBCOEMBAIPGFJLLPAQMNHJFKNH#",
                "#CMEOIMKCBAGOPAHQJNPNFQJLLBIHDKEGFD#",
                "#MHEEFMLBJCOBIAIPLOKNHPFJKQNDGQADCG#",
                "#PFHLGFCLJONAHPIJMOICNKDBGAMQQDBKEE#",
                "#JKHILJKAEPIQQEDPCFGAFLBODGHNBMOCNM#",
                "#HMIIAHCQELPQOBCOGMKFJDFNPNJBGDLEKA#",
                "#QHPIFLLMAENMKGCEFHQAIBDGKPCONJJOBD#",
                "#ICPBOIBKDEEPGGNJOFMMKFDCNQQHAJHLAL#",
                "#FIPHGNKPOAONMQLBECJGIBDKQLAHEFCMJD#",
                "#NAPHLNQBMDOOECLQKMJGFEFDCBJGIHKAPI#",
                "#DMFQODKIONPHPBKCHAIMQGFLBEEGJLNCAJ#",
                "#HJIKBHGKFMNLDCDOCJPEAFMIOQGPNALQEB#",
                "#CILNACQGPFQNHJADKFJDPOEKLGBEMHIBOM#",
                "#JCAEIJGQECHQPLOOGPBNHKKMBNAIFFDMLD#",
                "#CCJPKFDOJLGFOBILDBGQHAMIMKHENQAEPN#",
                "#AAMILJKBLEMKJBCDEIPQDCPQOGNOHGHNFF#",
                "#QDPEIEQLHAFADINGMBPOOFGLMKJHCBNKJC#",
                "#JCOPMLKIMDHJFLQOCBFGKNPBAIEHDGAEQN#",
                "#MQKQMAGOLADDJPPKJHLFHGINCIFBECONBE#",
                "#NPCHPNCLDHJDMGIIEAOJKKQEBMGOQFABLF#",
                "#HJAEAEJFPGOQLMBMPHDDBNCKQLNFIKGOCI#",
                "#MMLDHQEOJJBPAGIHFCLDNFNEAOCBKQPGIK#",
                "#QEKQNFGPNCKPHBMEMOJLDGHAAJBLICFODI#",
                "#QEEFJAOQMCNBBAHNKOIMLPGHPJFKDLCIGD#",
                "#DABPNDOHKCLFAFEQGNKEPJMIOCBLGHIQJM#",
                "#NJEHDQGJHDNPBBAFLIPQGFKACICMOLEKMO#",
                "#IIEJALDKKGBECCMOGQNOBFHAMQHLPPNFJD#",
                "#MLFELMCPGJIFDDGNNKPABOOEJICAQHKBHQ#",
                "#PFPOHAFEIGEIGJJCBNONMKCQDDBLLAQHMK#",
                "#PKNHKIDNEOAFIBPBLAEOFHJDCLMGQCMJGQ#",
                "#MIJDFIMPBHKNJALLONACOGKPFEHCEBDGQQ#",
                "#OAEOPAFHHBMPNBMFECDCLLNGJGIKQQKDJI#",
                "#CFPDFCMDIBJLJHIHGNAGBMELPOKAQNEKQO#",
                "#LHJHQMINJGKCNKOCDQBFPADBMGIOELFPAE#",
                "#NQMIAPHCAOLGFBMKIKPECBJLGEDFJDOQNH#",
                "#ACGQOFHDNHEFNCIMKBGMQPJAIKOEPBDJLL#",
                "#IMIDHMEEDNCPNKJABJPQOOFQAGKHFGLBCL#",
                "#EDDEANFFNQKGLAKIQPIOOLBCMHGBCHJPMJ#",
                "#MPFLOQBHKNAMAIELGIQGBDDJJONFCHEKPC#",
                "#HLBQMHDNECJAOIKAFLJBFNDECPGGOMPIKQ#",
                "#LGDCPLMIJJFDNIQENFBKAOGBOQKMHCEAHP#",
                "#GMHDPGDELKPJONNQIIBLQMEAACHBFCKJOF#",
                "#DGLMADEGPIHCFNHNFQJMCELIOPAJBQKOKB#",
                "#MCBDCMHQAGHKBOAQFPKOJJEPDNLLEGFNII#",
                "#IGKDPIEEMHPAFLFDBJNGOKCQHLNQBOMAJC#",
                "#PANHGLKIJNJEFLEFPBBKAIDDCGOOQMMCHQ#",
                "#HGOKQQJNDMAHLPFBIIBPAMLGDFJEECONKC#",
                "#PCBFDDFCHIPKINAQLKNAHJOEOJGMMLEBQG#",
                "#EEFMPKKAIGCFAJQBHLDBCNLJOGMNPOHDIQ#",
                "#PPKIBNJGNQHFOJGLEEBMKDAOMCCFHDLIQA#",
                "#EHMEOHNADIJADKCBPFOJCFLBGMQPKILGQN#",
                "#BBQKAEIMDOOLNHCLCGJKPAQNDMPGIHFFEJ#",
                "#PFLJPJGIBFLDAOCDNNCGIMEMHAHQEKQBOK#",
                "#LBHEKFNACGOKNPMBMEQQPOJLIGHJDFIACD#",
                "#QAKKOHBNDMIPBHEAJDNFOCCIMEQPLGGFJL#",
                "#GHBIJOICLGOCDHKBANDJFQPLANFQEMMKPE#",
                "#PPLMQGGDNCFCAIQIKJDFHNEAJHBEOMLKBO#",
                "#EAPBAOGCLIQELIBKJDDKCNFOFHHNQMMPJG#",
                "#JMCFDEBOIFNACOLLJGHGQPPBNIHDKEAQMK#",
                "#ILODHFGKMMEENGCKAPJNHLOPFQQABDJICB#",
                "#AKCBDQAHENEHOFGKLOBJCIIFNJMLPQGMDP#",
                "#QOOICJBBINAJAPGDHQCKFLPNDKMLEGHFEM#",
                "#MPODQHOLGQJALCFPGEIKDKJMACNFBHEINB#",
                "#DIFDALPHPLIMAJMOBBQJNEGFHEGKCQONKC#",
                "#OAGOELBKEFFKNMJJLMQCGQDDABNIPHCHIP#",
                "#GBJDKAAPPLFINGQQOMLFCBHEJKCNIDEMOH#",
                "#NOCLDHPKCJBHMGKGFDALJBAQIIQMEPNFEO#",
                "#NABGOKEFACLGFIMIDEDCOJHLMJQHPNKBQP#",
                "#NQHNCMJJFPKHDOEGIBDKAOEIBPFMQGCLLA#",
                "#DOQKHJMFPACJBFMNILAINEEGHLCQOKBGDP#"
        };
    }

    private void drawTitleMenu() {
        canvas.drawText("FOURTAL",w()/2,h()/2,title);

        int tmp = (int) (Math.abs(Math.sin(frameCount/90.*Math.PI))*255);
        start.setAlpha(tmp);
        canvas.drawText("tap to",w()/2,h()*3/4,start);
        canvas.drawText("start",w()/2,h()*3/4+c480(50),start);
    }

    private void drawModes() {
        float margin = c480(40), w = bannerWidth;

        quad(w()-margin,h()/4-w/2,w()-margin,h()/4+w/2,margin,h()/2+w/2,margin,h()/2-w/2,shadow);
        quad(w()-margin,h()/2-w/2,w()-margin,h()/2+w/2,margin,h()*3/4+w/2,margin,h()*3/4-w/2,shadow);

        canvas.drawRect(margin,h()/4-w/2,w()-margin,h()/4+w/2,banner);
        canvas.drawRect(margin,h()/2-w/2,w()-margin,h()/2+w/2,banner);
        canvas.drawRect(margin,h()*3/4-w/2,w()-margin,h()*3/4+w/2,banner);

        canvas.drawText("4x4",w()/2,h()/4+c854(20),mode);
        canvas.drawText("4 moves",w()/2,h()/4+c854(40),steps);
        canvas.drawText("6x6",w()/2,h()/2+c854(20),mode);
        canvas.drawText("5 moves",w()/2,h()/2+c854(40),steps);
        canvas.drawText("6x6",w()/2,h()*3/4+c854(20),mode);
        canvas.drawText("6 moves",w()/2,h()*3/4+c854(40),steps);

        tri(margin,h()/4-w/2,margin*2,h()/4,margin,h()/4+w/2,bg);
        tri(w()-margin,h()*3/4+w/2,w()-margin*2,h()*3/4,w()-margin,h()*3/4-w/2,bg);

        canvas.drawBitmap(back, w()/2-c854(25), h()-c854(75), null);
    }

    private void drawLevels() {
        int lpr = levelsPerRow, rpp = rowsPerPage;
        float w = (h()-c854(250))/rpp;
        float lm = (w()-lpr*w)/2;

        for (int i = page*lpr*rpp; i < Math.min((page+1)*lpr*rpp,nLevels()); i++) {
            Bitmap bmp = completed(i+1) ? green : unlocked(i+1) ? blue : gray;
            canvas.drawBitmap(bmp, lm+i%lpr*w, c854(150)+(i-page*lpr*rpp)/lpr*w, null);
            canvas.drawText(i+1+"",lm+i%lpr*w+w/2,c854(150)+(i-page*lpr*rpp)/lpr*w+w/2-(ls.ascent()+ls.descent())/2, ls);
        }

        canvas.drawRect(0,0,w(),c854(150),bg);
        canvas.drawText("LEVEL",w()/2,c854(75),ls);
        canvas.drawText("SELECT",w()/2,c854(120),ls);

        canvas.save();
        canvas.translate(c854(75),c854(75));
        if (page > 0) canvas.drawBitmap(triangle,-c854(37.5f),-c854(37.5f),null);
        canvas.translate(w()-c854(150),0);
        canvas.rotate(180);
        if (page < nLevels()/(lpr*rpp)) canvas.drawBitmap(triangle,-c854(37.5f),-c854(37.5f),null);
        canvas.restore();

        canvas.drawBitmap(back, w()/2-c854(25), h()-c854(75), null);
    }

    private void drawMoves() {
        float y = h() - (h()-w())/4,
                r = w()/24,
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
