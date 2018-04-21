package com.status.brickgame;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;
    public final int COUNT_SHAPE_TYPE = 7;
    public final int COUNT_SHAPE_ORIENTATION = 4;

    public static boolean gameOver = true;
    public static List<Integer> listCells = new ArrayList<Integer>(200);
    private boolean pause = false, fallingLR = false, fallingD = false;
    private boolean volume = true;
    private boolean restart = false, run = false;
    private boolean recordDisplayed;
    private int orientation;
    private int score, timeout, record;
    int soundIdRotate, soundIdFill;

    private CellsAdapter adapterCells;
    private ConstraintLayout rLayout;
    private ConstraintSet set;
    private TextView textGameOver, textScore;
    private TextView textRecord;
    private ImageView ivPreview;
    private ImageButton btnPause, btnRotate, btnDown, btnLeft, btnRight, btnVolume;
    private Shape curShape, nextShape;
    private SoundPool sp;
    private Semaphore sem;
    public static boolean debug;

    public static void writeLog(String m) {
//        if(debug) {
//            String path = Environment.getExternalStorageDirectory() + "/Download/log_bg.txt";
//            BufferedWriter bw;
//
//            try {
//                bw = new BufferedWriter(new FileWriter(path, true));
//                Date date = new Date();
//                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
//
//                bw.write(sdf.format(date) + " " + m + "\n");
//                bw.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Log.d("MYLOG", m);
//
//        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_main);

        rLayout = (ConstraintLayout) findViewById(R.id.cl);
        textGameOver = (TextView) findViewById(R.id.textGameOver);
        textScore = (TextView) findViewById(R.id.textScore);
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        btnPause = (ImageButton) findViewById(R.id.btnPause);
        btnVolume = (ImageButton) findViewById(R.id.btnVolume);
        btnRotate = (ImageButton) findViewById(R.id.btnRotate);
        btnDown = (ImageButton) findViewById(R.id.btnDown);
        btnLeft = (ImageButton) findViewById(R.id.btnLeft);
        btnRight = (ImageButton) findViewById(R.id.btnRight);
        btnDown.setOnTouchListener(this);
        btnLeft.setOnTouchListener(this);
        btnRight.setOnTouchListener(this);
        btnRotate.setOnTouchListener(this);

        initList();
        curShape = null;
        nextShape = null;

        textScore.setText(String.valueOf(score));

        final GridView gvCells = (GridView) findViewById(R.id.gvCells);

        adapterCells = new CellsAdapter(this, R.layout.item, listCells);

        gvCells.setAdapter(adapterCells);

        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

        soundIdRotate = sp.load(this, R.raw.rotate, 1);
        soundIdFill = sp.load(this, R.raw.fill, 1);

        Drawable dr = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                Paint p = new Paint();
                p.setColor(Color.argb(255, 77, 148, 255));
                canvas.drawPaint(p);

                p.setColor(Color.argb(10, 0, 0, 0));
                p.setTextSize(55);
                p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC));
                canvas.rotate(315);
                for (int i = -canvas.getHeight(); i < canvas.getWidth(); i += 400) {
                    for (int j = 0; j < canvas.getHeight() * 2; j += 150) {
                        canvas.drawText("BRICK GAME", i, j, p);
                    }
                }

                canvas.rotate(-315);
                Rect rectf = new Rect();

                gvCells.getGlobalVisibleRect(rectf);

                Path path = new Path();

                p.setColor(Color.argb(110, 0, 0, 0));
                int l = (int)rectf.left;

                path.reset();
                path.moveTo(rectf.left, rectf.top);
                path.lineTo(rectf.left - l, rectf.top - l);
                path.lineTo(rectf.left + rectf.width() + l, rectf.top - l);
                path.lineTo(rectf.left + rectf.width(), rectf.top);
                p.setShader(new LinearGradient(0, l, 0, 0, Color.BLACK, Color.argb(255, 200, 200, 200), Shader.TileMode.REPEAT));
                canvas.drawPath(path, p);

                path.reset();
                path.moveTo(rectf.left, rectf.top);
                path.lineTo(rectf.left - l, rectf.top - l);
                path.lineTo(rectf.left - l, rectf.top + rectf.height() + l);
                path.lineTo(rectf.left, rectf.top + rectf.height());
                p.setShader(new LinearGradient(l, 0, 0, 0, Color.BLACK, Color.argb(255, 200, 200, 200), Shader.TileMode.REPEAT));
                canvas.drawPath(path, p);

                path.reset();
                path.moveTo(rectf.left, rectf.top + rectf.height());
                path.lineTo(rectf.left - l, rectf.top + rectf.height() + l);
                path.lineTo(rectf.left + rectf.width() + l, rectf.top + rectf.height() + l);
                path.lineTo(rectf.left + rectf.width(), rectf.top + rectf.height());
                path.close();
                p.setShader(new LinearGradient(0, 0, 0, l, Color.BLACK, Color.argb(255, 200, 200, 200), Shader.TileMode.REPEAT));
                canvas.drawPath(path, p);

                path.reset();
                path.moveTo(rectf.left + rectf.width(), rectf.top + rectf.height());
                path.lineTo(rectf.left + rectf.width() + l, rectf.top + rectf.height() + l);
                path.lineTo(rectf.left + rectf.width() + l, rectf.top - l);
                path.lineTo(rectf.left + rectf.width(), rectf.top);
                path.close();
                p.setShader(new LinearGradient(0, 0, l, 0, Color.BLACK, Color.argb(255, 200, 200, 200), Shader.TileMode.REPEAT));
                canvas.drawPath(path, p);

            }

            @Override
            public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };

        rLayout.setBackgroundDrawable(dr);

        SharedPreferences prf = getPreferences(MODE_PRIVATE);
        volume = prf.getBoolean("volume", true);
        btnVolume.setImageResource(!volume ? R.drawable.volume1 : R.drawable.volume2);
        sem = new Semaphore(1);

        debug = false;
    }

    private void initList() {
        listCells.clear();
        for (int i = 0; i < 200; i++) {
            listCells.add(0);
        }
        ;
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnNewGame:
                if (run && !gameOver) {
                    restart = true;
                    while (restart) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                recordDisplayed = false;
                if (!run) {
                    SharedPreferences prf = getPreferences(MODE_PRIVATE);
                    //prf.edit().remove("record").commit();
                    record = prf.getInt("record", 0);
                    if (record == 0) {
                        recordDisplayed = true;
                    }
                }


                run = true;
                gameOver = false;
                score = 0;
                setTimeout();
                textScore.setText(String.valueOf(score));


                initList();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0, type;
                        Random r;
                        r = new Random();

                        type = r.nextInt(COUNT_SHAPE_TYPE) + 1;
                        orientation = r.nextInt(COUNT_SHAPE_ORIENTATION) + 1;
                        curShape = new Shape(type, orientation, sem);
                        type = r.nextInt(COUNT_SHAPE_TYPE) + 1;
                        orientation = r.nextInt(COUNT_SHAPE_ORIENTATION) + 1;
                        nextShape = new Shape(type, orientation, sem);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bmp = BitmapFactory.decodeResource(getResources(), nextShape.getPreview());
                                Matrix matrix = new Matrix();
                                matrix.postRotate((orientation - 1) * 90);
                                ivPreview.setImageBitmap(Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true));
                                textGameOver.setText("");
                            }
                        });

                        while (!gameOver && !restart) {
                            if (!pause) {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.adapterCells.notifyDataSetChanged();
                                    }
                                });
                                try {
                                    TimeUnit.MILLISECONDS.sleep(timeout);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (!curShape.move("down")) {
                                    if (gameOver) {
                                        for (int y = MainActivity.HEIGHT - 1; y >= 0; y--) {
                                            try {
                                                TimeUnit.MILLISECONDS.sleep(70);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            for (int x = MainActivity.WIDTH - 1; x >= 0; x--) {
                                                listCells.set(y * MainActivity.WIDTH + x, R.drawable.lightblue);
                                            }

                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainActivity.this.adapterCells.notifyDataSetChanged();
                                                }
                                            });
                                        }

                                        for (int y = 0; y <= MainActivity.HEIGHT - 1; y++) {
                                            try {
                                                TimeUnit.MILLISECONDS.sleep(70);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            for (int x = MainActivity.WIDTH - 1; x >= 0; x--) {
                                                listCells.set(y * MainActivity.WIDTH + x, 0);
                                            }

                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainActivity.this.adapterCells.notifyDataSetChanged();
                                                }
                                            });
                                        }

                                        break;
                                    } else {
                                        curShape = nextShape;
//                                        if(debug) {
//                                            writeLog("new shape " + " " + type + " " + curShape.list);
//                                        }
                                        r = new Random();
                                        type = r.nextInt(COUNT_SHAPE_TYPE) + 1;
                                        orientation = r.nextInt(COUNT_SHAPE_ORIENTATION) + 1;
                                        nextShape = new Shape(type, orientation, sem);
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Bitmap bmp = BitmapFactory.decodeResource(getResources(), nextShape.getPreview());
                                                Matrix matrix = new Matrix();
                                                matrix.postRotate((orientation - 1) * 90);
                                                ivPreview.setImageBitmap(Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true));
                                            }
                                        });


                                        if (DeleteFullRows()) {
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainActivity.this.adapterCells.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                        restart = false;
                    }
                });
                thread.start();
                break;
            case R.id.btnPause:
                pause = !pause;
                btnPause.setImageResource(!pause ? R.drawable.pause : R.drawable.play);
                break;
            case R.id.btnVolume:
                volume = !volume;
                btnVolume.setImageResource(!volume ? R.drawable.volume1 : R.drawable.volume2);
                SharedPreferences prf = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = prf.edit();
                editor.putBoolean("volume", volume);
                editor.commit();
                break;
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        final int viewId = v.getId();
        if (!pause && !gameOver) {
            switch (viewId) {
                case R.id.btnRotate:
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: // нажатие
                            if (volume) {
                                sp.play(soundIdRotate, 1, 1, 0, 0, 1);
                            }
                            btnRotate.animate().setDuration(100).scaleXBy(1).scaleX(0.9f).scaleYBy(1).scaleY(0.9f);
                            curShape.rotate(false);
                            adapterCells.notifyDataSetChanged();
                            break;
                        case MotionEvent.ACTION_UP: // отпускание
                            btnRotate.animate().setDuration(100).scaleXBy(0.9f).scaleX(1).scaleYBy(0.9f).scaleY(1);
                            break;
                    }

                    break;
                case R.id.btnLeft:
                case R.id.btnRight:
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: // нажатие
                            if (!fallingLR) {
                                if (volume) {
                                    sp.play(soundIdRotate, 1, 1, 0, 0, 1);
                                }
                                fallingLR = true;
                                if (viewId == R.id.btnRight) {
                                    btnRight.animate().setDuration(100).scaleXBy(1).scaleX(0.9f).scaleYBy(1).scaleY(0.9f);
                                } else {
                                    btnLeft.animate().setDuration(100).scaleXBy(1).scaleX(0.9f).scaleYBy(1).scaleY(0.9f);
                                }
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int timeout = 250;
                                        while (fallingLR) {
                                            boolean result = curShape.move(viewId == R.id.btnRight ? "right" : "left");
                                            if (!result) {
                                                fallingLR = false;
                                                break;
                                            }
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainActivity.this.adapterCells.notifyDataSetChanged();
                                                }
                                            });
                                            try {
                                                TimeUnit.MILLISECONDS.sleep(timeout);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            timeout = 100;
                                        }
                                    }
                                });
                                thread.start();
                            }
                            break;
                        case MotionEvent.ACTION_UP: // отпускание
                            fallingLR = false;
                            if (viewId == R.id.btnRight) {
                                btnRight.animate().setDuration(100).scaleXBy(0.9f).scaleX(1).scaleYBy(0.9f).scaleY(1);
                            } else {
                                btnLeft.animate().setDuration(100).scaleXBy(0.9f).scaleX(1).scaleYBy(0.9f).scaleY(1);
                            }
                            break;
                    }
                    break;
                case R.id.btnDown:

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: // нажатие
                            if (!fallingD) {
                                if (volume) {
                                    sp.play(soundIdRotate, 1, 1, 0, 0, 1);
                                }
                                btnDown.animate().setDuration(100).scaleXBy(1).scaleX(0.9f).scaleYBy(1).scaleY(0.9f);

                                fallingD = true;
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Shape cs = curShape;
                                        while (fallingD) {
                                            boolean result = cs.move("down");
                                            if (!result) {
                                                fallingD = false;
                                                break;
                                            }
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MainActivity.this.adapterCells.notifyDataSetChanged();
                                                }
                                            });
                                            try {
                                                TimeUnit.MILLISECONDS.sleep(40);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                thread.start();
                            }
                            break;
                        case MotionEvent.ACTION_UP: // отпускание
                            fallingD = false;
                            btnDown.animate().setDuration(100).scaleXBy(0.9f).scaleX(1).scaleYBy(0.9f).scaleY(1);
                            break;
                    }
                    break;
            }
        }
        return true;
    }

    private boolean DeleteFullRows() {
        boolean full, modify = false;
        int i, j, x, y, index, index1, count = 0;
        try {
            sem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (i = 0; i < HEIGHT; i++) {
            full = true;
            for (j = 0; j < WIDTH; j++) {
                if (listCells.get(i * WIDTH + j).intValue() == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
//                if(debug) {
//                    writeLog("delete begin " + listCells);
//                }

                if (volume) {
                    sp.play(soundIdFill, 1, 1, 0, 0, 1);
                }
                modify = true;
                count++;
                for (y = i; y > 0; y--) {
                    for (x = 0; x < WIDTH; x++) {
                        index = y * WIDTH + x;
                        index1 = (y - 1) * WIDTH + x;
                        if (!curShape.list.contains(index)) {
                            if (!curShape.list.contains(index1)) {
                                listCells.set(index, listCells.get(index1));
                            } else {
                                listCells.set(index, 0);
                            }
                        }
                    }
                }
                for (x = 0; x < WIDTH; x++) {
                    if (!curShape.list.contains(x)) {
                        listCells.set(x, 0);
                    }
                }
//                if(debug) {
//                    writeLog("delete end " + listCells);
//                }
            }
        }
        sem.release();

        score += count * count;
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textScore.setText(String.valueOf(score));
            }
        });
        if (score > record) {
            record = score;
            if (!recordDisplayed) {
                recordDisplayed = true;
                pause = true;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        ConstraintLayout.LayoutParams lParams = new ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);


                        set = new ConstraintSet();
                        textRecord = new TextView(MainActivity.this);
                        textRecord.setLayoutParams(lParams);
                        textRecord.setWidth(200);
                        textRecord.setHeight(20);
                        textRecord.setGravity(Gravity.CENTER);
                        textRecord.setText("New  record!!!");
                        textRecord.setTextSize(5);
                        textRecord.setTypeface(null, Typeface.BOLD_ITALIC);
                        textRecord.setTextColor(Color.WHITE);
                        rLayout.addView(textRecord);
                        set.clone(rLayout);
                        set.connect(textRecord.getId(), ConstraintSet.TOP, rLayout.getId(), ConstraintSet.TOP, getApplicationContext().getResources().getDisplayMetrics().heightPixels / 2);
                        set.connect(textRecord.getId(), ConstraintSet.LEFT, rLayout.getId(), ConstraintSet.LEFT, 0);
                        set.connect(textRecord.getId(), ConstraintSet.RIGHT, rLayout.getId(), ConstraintSet.RIGHT, 0);
                        set.applyTo(rLayout);
                        textRecord.setPivotX(100);
                        textRecord.setPivotY(10);
                        textRecord.animate().setDuration(1000).scaleXBy(1).scaleX(8).scaleYBy(1).scaleY(8);
                    }
                });
                try {
                    TimeUnit.MILLISECONDS.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rLayout.removeView(textRecord);
                    }
                });
                pause = false;
            }
        }
        setTimeout();

        return modify;
    }

    private void setTimeout() {
        if (score < 50) {
            timeout = 800;
        } else if (score < 100) {
            timeout = 700;
        } else if (score < 150) {
            timeout = 600;
        } else if (score < 200) {
            timeout = 500;
        } else if (score < 250) {
            timeout = 450;
        } else if (score < 300) {
            timeout = 400;
        } else if (score < 350) {
            timeout = 350;
        } else {
            timeout = 300;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences prf = getPreferences(MODE_PRIVATE);
        int recordOld = prf.getInt("record", 0);

        if (record > recordOld) {
            SharedPreferences.Editor editor = prf.edit();
            editor.putInt("record", record);
            editor.commit();
        }

        if (run && !gameOver) {
            restart = true;
            while (restart) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //finish();

    }
}
