package com.example.signalwaveform.waveform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.support.annotation.IntDef;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by liwb on 2017/9/1.
 * 两个模式，模式一：只增加显示范围  模式二：数据分析模式，可以进行缩放与拖动
 */

public class WaveFormEx extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WorkMode_One=0x01;
    public static final int WorkMode_Two=0x02;
    @IntDef({WorkMode_One,WorkMode_Two})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WorkMode{}

    private static final int GridLevel_100=0x01;
    private static final int GridLevel_200=0x02;
    private static final int GridLevel_300=0x03;
    private static final int GridLevel_400=0x04;
    private static final int GridLevel_500=0x05;
    private static final int GridLevel_600=0x06;
    private static final int GridLevel_800=0x08;
    private static final int GridLevel_1000=0x10;
    private static final int GridLevel_1200=0x12;
    @IntDef({GridLevel_100,GridLevel_200,GridLevel_300,GridLevel_400,GridLevel_500,GridLevel_600,GridLevel_800,GridLevel_1000,GridLevel_1200})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GridLevel{}


    private Context context;
    private DoWorkThread doWorkThread;
    private @WorkMode int workMode=WorkMode_One;
    private @GridLevel int gridLevel=GridLevel_100;

    private Bitmap bmp;
    private Canvas canvasGrid;
    private Paint paintGrid;
    /***
     * 开始距离，也就是开始的绘制距离。默认从0米开始
     *
     */
    private float startDistance=0;

    public WaveFormEx(Context context){
        super(context);
        this.context=context;
        init();
    }

    private void init(){
        SurfaceHolder holder = getHolder();
        holder.addCallback(this); //设置Surface生命周期回调
        //背景色，且透明
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        doWorkThread = new DoWorkThread(holder, context);

        bmp=Bitmap.createBitmap(1500,800, Bitmap.Config.ARGB_8888);
        canvasGrid=new Canvas(bmp);
        canvasGrid.drawColor(Color.WHITE);
        paintGrid=new Paint();

        drawGrid();
    }


    private void drawGrid(){
        switch (gridLevel){
            case GridLevel_100:
                drawGrid_100();
                break;
            case GridLevel_200:break;
            case GridLevel_300:break;
            case GridLevel_400:break;
            case GridLevel_500:break;
            case GridLevel_600:break;
            case GridLevel_800:break;
            case GridLevel_1000:break;
            case GridLevel_1200:break;
        }
    }

    private void drawGrid_100(){
        synchronized (bmp) {
        gridLevel = GridLevel_100;

        canvasGrid.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
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
           int LblDistance = 100 / 10;
           int offset = (int) (startDistance / 10.0f * 120);
            System.out.println("****************");
           for (int i = 100 ; i <= 1300; i += 120) {
               int PosX=i-offset%120;
              if (PosX<100) continue;

               System.out.println("posX:"+PosX +"  offset:"+ offset +"  startDistance:"+startDistance);
               canvasGrid.drawLine(PosX, 0, PosX, 700, paintGrid);
               if ((((PosX - 100) / 120) % 2) == 0) {
                   String s=String.format("%4.1f",(PosX - 100) / 120 * LblDistance + startDistance);
                   canvasGrid.drawText(s + "m", PosX - (40 / 2), 730, paintGrid);
               }
           }
       }
    }

    //region 属性

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(@WorkMode int workMode) {
        this.workMode = workMode;
    }

    public int getGridLevel() {
        return gridLevel;
    }

    //endregion


    //region 操作

    Point point1=new Point();
    Point point2=new Point();
    //只进行数据的变化，画图还在draw里面进行。
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (workMode==WorkMode_One) return super.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:{
                 point1.set((int)event.getX(),(int)event.getY());

            }break;
            case MotionEvent.ACTION_POINTER_DOWN:{
                point1.set((int)event.getX(1),(int)event.getY(1));
            }break;
            case MotionEvent.ACTION_MOVE:{
                 if (event.getPointerCount()==1){
                     //一个点在移动，即拖动
                     float offset= offset(point1,event);
                     startDistance +=offset/120.0f;

                     drawGrid_100();
                 }
                 if (event.getPointerCount()>=2){
                     //两个点在移动，查看是否是放大还是缩小
                     float zoom= distance(point1,point2,event);
                     if ( Math.abs(zoom)>10f){
                         point1.set((int)event.getX(0),(int) event.getY(1));
                         point2.set((int)event.getX(0),(int)event.getY(1));
                         float middle=middle(event).x;
                         if (zoom>0){
                             //放大一级
                         }else{
                             //缩小一级
                         }
                     }
                 }
            }break;
            case MotionEvent.ACTION_UP:{

            }break;
        }

        return true;
    }

    // 计算两个触摸点之间的距离
    // 大于0说明是放大，小于0说明是缩小
    private float distance(Point point1,Point point2,MotionEvent event) {
        float x = point1.x - point2.x;
        float y = point1.y - point2.y;
        float down=  (float)Math.sqrt(x * x + y * y);
        float x1=event.getX(0)-event.getX(1);
        float y1=event.getY(0)-event.getY(1);
        float move=(float)Math.sqrt(x1 * x1 + y1 * y1);
        return move-down;
    }

    // 计算两个触摸点的中点
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    //左右移动
    //小于0：左移  大于0：右移
    private float offset(Point point1,MotionEvent event){
         float tem=(point1.x-event.getX());
         point1.set((int) event.getX(),(int) event.getY());
        return tem;
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
            canvas.drawColor(Color.GREEN, PorterDuff.Mode.CLEAR);
            synchronized (bmp) {
                canvas.drawBitmap(bmp, 0, 0, null);
            }
        }
    }

    //endregion


    //region  interface SurfaceHolder.Callback

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


}
