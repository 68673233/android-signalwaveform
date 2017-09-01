package com.example.signalwaveform.waveform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwb on 2017/8/22.
 */

public class WaveForm extends SurfaceView implements SurfaceHolder.Callback {

    private final static int GridLevel_400 = 0x01;
    private final static int GridLevel_800 = 0x02;
    private final static int GridLevel_1200 = 0x03;



    private Context context;
    private DoWorkThread doWorkThread;
    private Bitmap bmpGrid;
    private Bitmap bmpWave;
    private Canvas canvasGrid, canvasWave;
    private Paint paintGrid;
    private Paint paintWave;

    private int gridLevel = GridLevel_400;
    private List<Param> listS1 = new ArrayList<>();
    private List<Param> listS2 = new ArrayList<>();
    private List<Param> listS3 = new ArrayList<>();

    public WaveForm(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this); //设置Surface生命周期回调
        //背景色，且透明
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        doWorkThread = new DoWorkThread(holder, context);

        initBmp();
    }

    private void initBmp() {
        bmpGrid = Bitmap.createBitmap(1400, 900, Bitmap.Config.ARGB_8888);
        bmpWave = Bitmap.createBitmap(1200, 800, Bitmap.Config.ARGB_8888);
        canvasGrid = new Canvas(bmpGrid);
        canvasWave = new Canvas(bmpWave);
        paintGrid = new Paint();
        paintWave = new Paint();
        paintWave.setStrokeWidth(3);
        clearDraw();
        drawGrid_400();

    }

    //region  对外接口
    public void addS1(float x, float y) {
        paintWave.setColor(Color.RED);
        Param item = new Param(x, y);
        listS1.add(item);
        drawWave(x);
    }

    public void addS2(float x, float y) {
        paintWave.setColor(Color.GREEN);
        Param item = new Param(x, y);
        listS2.add(item);
        drawWave(x);
    }

    public void addS3(float x, float y) {
        paintWave.setColor(Color.BLUE);
        Param item = new Param(x, y);
        listS3.add(item);
        drawWave(x);
    }

    private void drawWave(float x) {

        if (x > 820 && getCurrGridLevel() == GridLevel_800) {
            //重新绘制，切网格切换到1200.
            clearDraw();
            drawGrid_1200();
            paintWave.setColor(Color.RED);
            for (int i = 0; i < listS1.size(); i++) {
                canvasWave.drawPoint(listS1.get(i).x, (400 - listS1.get(i).y * 100), paintWave);
            }
            paintWave.setColor(Color.GREEN);
            for (int i = 0; i < listS2.size(); i++) {
                canvasWave.drawPoint(listS2.get(i).x, (400 - listS2.get(i).y * 100), paintWave);
            }
            paintWave.setColor(Color.BLUE);
            for (int i = 0; i < listS3.size(); i++) {
                canvasWave.drawPoint(listS3.get(i).x, (700 - listS3.get(i).y * 80), paintWave);
            }


        } else if (x > 420 && getCurrGridLevel() == GridLevel_400) {
            //重新绘制800网络
            clearDraw();
            drawGrid_800();
            paintWave.setColor(Color.RED);
            for (int i = 0; i < listS1.size(); i++) {
                canvasWave.drawPoint(listS1.get(i).x*1.5f , (400 - listS1.get(i).y * 100), paintWave);
            }
            paintWave.setColor(Color.GREEN);

            for (int i = 0; i < listS2.size(); i++) {
                canvasWave.drawPoint(listS2.get(i).x*1.5f , (400 - listS2.get(i).y * 100), paintWave);
            }
            paintWave.setColor(Color.BLUE);
            for (int i = 0; i < listS3.size(); i++) {
                canvasWave.drawPoint(listS3.get(i).x*1.5f , (700 - listS3.get(i).y * 80), paintWave);
            }

        } else if (x <= 420) {
            paintWave.setColor(Color.RED);
            if (listS1.size()>0) canvasWave.drawPoint(listS1.get(listS1.size() - 1).x *3.0f, 400 - listS1.get(listS1.size() - 1).y * 100, paintWave);
            paintWave.setColor(Color.GREEN);
            if (listS2.size()>0) canvasWave.drawPoint(listS2.get(listS2.size() - 1).x*3.0f , 400 - listS2.get(listS2.size() - 1).y * 100, paintWave);
            paintWave.setColor(Color.BLUE);
            if (listS3.size()>0) canvasWave.drawPoint(listS3.get(listS3.size() - 1).x*3.0f , 700 - listS3.get(listS3.size() - 1).y * 80, paintWave);
        } else if (x <= 820) {
            paintWave.setColor(Color.RED);
            if (listS1.size()>0)  canvasWave.drawPoint(listS1.get(listS1.size() - 1).x*1.5f , 400 - listS1.get(listS1.size() - 1).y * 100, paintWave);
            paintWave.setColor(Color.GREEN);
            if (listS2.size()>0)  canvasWave.drawPoint(listS2.get(listS2.size() - 1).x*1.5f, 400 - listS2.get(listS2.size() - 1).y * 100, paintWave);
            paintWave.setColor(Color.BLUE);
            if (listS3.size()>0)  canvasWave.drawPoint(listS3.get(listS3.size() - 1).x*1.5f, 700 - listS3.get(listS3.size() - 1).y * 80, paintWave);
        } else {
            //1200的范围
            paintWave.setColor(Color.RED);
            if (listS1.size()>0) canvasWave.drawPoint(listS1.get(listS1.size() - 1).x, 400 - listS1.get(listS1.size() - 1).y * 100, paintWave);
            paintWave.setColor(Color.GREEN);
            if (listS2.size()>0)  canvasWave.drawPoint(listS2.get(listS2.size() - 1).x, 400 - listS2.get(listS2.size() - 1).y * 100, paintWave);
            paintWave.setColor(Color.BLUE);
            if (listS3.size()>0)  canvasWave.drawPoint(listS3.get(listS3.size() - 1).x, 700 - listS3.get(listS3.size() - 1).y * 80, paintWave);
        }
    }

    private int getCurrGridLevel() {
        return gridLevel;
    }

    /***
     * 清楚波形，表格，list数据
     */
    public void clear() {
        clearDraw();
        drawGrid_400();
        listS1.clear();
        listS2.clear();
        listS3.clear();
    }
    //endregion

    //region 画板上画图

    private void clearDraw() {
        clearDrawGrid();
        clearDrawWave();
    }

    private void clearDrawGrid() {
        canvasGrid.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
    }

    private void clearDrawWave() {
        canvasWave.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
    }

    /***
     * X轴： 也就是说，1，1.3，1.6，2 四个档分别放到像素，1，2，3，4上
     * Y轴： 第一个是50个像素表示0.5毫安，第二个是50个像素表示0.4毫安
     */
    private void drawGrid_400() {
        gridLevel = GridLevel_400;

        paintGrid.setColor(Color.BLACK);
        paintGrid.setTextSize(18);
        canvasGrid.drawLine(100, 0, 100, 700, paintGrid);
        canvasGrid.drawLine(100, 700, 1350, 700, paintGrid);

        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        paintGrid.setPathEffect(effects);

        //画横线
        for (int i = 50; i < 450; i += 50) {
            canvasGrid.drawLine(100, i, 1350, i, paintGrid);
            canvasGrid.drawText(String.format("%1.2f", (i - 50) * 0.01) + "A", 10, 450 - i, paintGrid);
        }
        //画横线
        for (int i = 550; i <= 700; i += 50) {
            if (i != 700) canvasGrid.drawLine(100, i, 1350, i, paintGrid);
            canvasGrid.drawText(String.format("%1.2f", ((i - 550) / 50) * 0.4) + "A", 10, 700 - (i - 550), paintGrid);
        }

        //画坚线
        for (int i = 100; i <= 1300; i += 120) {
            canvasGrid.drawLine(i, 0, i, 700, paintGrid);
            if ((((i - 100) / 120) % 2) == 0)
                canvasGrid.drawText(String.valueOf((i - 100) / 120 * 40) + "m", i - (40 / 2), 730, paintGrid);
        }
    }

    /***
     * X轴：也就是说1，1.5，2 三个档分别放到1，2，3个像素上。
     * Y轴： 第一个是50个像素表示0.5毫安，第二个是50个像素表示0.4毫安
     */
    private void drawGrid_800() {
        gridLevel = GridLevel_800;
        paintGrid.setColor(Color.BLACK);
        paintGrid.setTextSize(18);
        canvasGrid.drawLine(100, 0, 100, 700, paintGrid);
        canvasGrid.drawLine(100, 700, 1350, 700, paintGrid);

        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        paintGrid.setPathEffect(effects);

        //画横线
        for (int i = 50; i < 450; i += 50) {
            canvasGrid.drawLine(100, i, 1350, i, paintGrid);
            canvasGrid.drawText(String.format("%1.2f", (i - 50) * 0.01) + "A", 10, 450 - i, paintGrid);
        }
        //画横线
        for (int i = 550; i <= 700; i += 50) {
            if (i != 700) canvasGrid.drawLine(100, i, 1350, i, paintGrid);
            canvasGrid.drawText(String.format("%1.2f", ((i - 550) / 50) * 0.4) + "A", 10, 700 - (i - 550), paintGrid);
        }
        //画坚线
        for (int i = 100; i <= 1300; i += 60) {
            canvasGrid.drawLine(i, 0, i, 700, paintGrid);
            if ((((i - 100) / 60) % 2) == 0)
                canvasGrid.drawText(String.valueOf((i - 100) / 60 * 40) + "m", i - (40 / 2), 730, paintGrid);
        }
    }

    /***
     * X轴：也就是说 1就一个档，就放到1个像素上
     * Y轴： 第一个是50个像素表示0.5毫安，第二个是50个像素表示0.4毫安
     */
    private void drawGrid_1200() {
        gridLevel = GridLevel_1200;
        paintGrid.setColor(Color.BLACK);
        paintGrid.setTextSize(18);
        canvasGrid.drawLine(100, 0, 100, 700, paintGrid);
        canvasGrid.drawLine(100, 700, 1350, 700, paintGrid);

        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
        paintGrid.setPathEffect(effects);

        //画横线
        for (int i = 50; i < 450; i += 50) {
            canvasGrid.drawLine(100, i, 1350, i, paintGrid);
            canvasGrid.drawText(String.format("%1.2f", (i - 50) * 0.01) + "A", 10, 450 - i, paintGrid);
        }
        //画横线
        for (int i = 550; i <= 700; i += 50) {
            if (i != 700) canvasGrid.drawLine(100, i, 1350, i, paintGrid);
            canvasGrid.drawText(String.format("%1.2f", ((i - 550) / 50) * 0.4) + "A", 10, 700 - (i - 550), paintGrid);
        }
        //画坚线
        for (int i = 100; i <= 1300; i += 40) {
            canvasGrid.drawLine(i, 0, i, 700, paintGrid);
            if ((((i - 100) / 40) % 2) == 0)
                canvasGrid.drawText(String.valueOf(i - 100) + "m", i - 40 / 2, 730, paintGrid);
        }
    }


    //endregion


    //region surfaceHolder.callback
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        doWorkThread.isRunning = true;
        doWorkThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        doWorkThread.isRunning = false;
        try {
            doWorkThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region 显示到界面上

    class DoWorkThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private Context context;
        //private Paint paint;
        boolean isRunning;

        public DoWorkThread(SurfaceHolder surfaceHolder, Context context) {
            this.surfaceHolder = surfaceHolder;
            this.context = context;

        }

        @Override
        public void run() {
            Canvas c = null;

            while (isRunning) {

                try {
                    synchronized (surfaceHolder) {

                        c = surfaceHolder.lockCanvas(null);
                        doDraw(c);
                        //通过它来控制帧数执行一次绘制后休息50ms
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    surfaceHolder.unlockCanvasAndPost(c);
                }

            }
        }

        public void doDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(bmpGrid, 0, 0, null);
            canvas.drawBitmap(bmpWave,100,0,null);
        }
    }

    //endregion

    class Param {
        public float x;
        public float y;

        public Param() {
        }

        public Param(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

}
