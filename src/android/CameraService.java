package com.chinamobile.gdwy;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;



/**
 * Created by liangzhongtai on 2016/7/16.
 * 相机罗盘悬浮窗
 */
public class CameraService extends Service {
   private WindowManager windowManager;
   private WindowManager.LayoutParams wmParams;
   private LinearLayout floatingLL;
   private float mTouchStartX;
   private float mTouchStartY;
   private float x;
   private float y;
   private boolean viewAdded;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(windowManager == null){
            initWindow();
        }
        try {
            if(!viewAdded){
                viewAdded = true;
                windowManager.addView(floatingLL,wmParams);
            }
            SensorUtil.listener = (x, y, z) -> {
                if(!viewAdded)return;
                String[] texts = new String[]{"x: "+(int)x+" °","y: "+(int)y+"°","z: "+(int)z+"°"};
                for (int i=0;i<3;i++){
                    ((TextView)floatingLL.getChildAt(i)).setText(texts[i]);
                }
                windowManager.updateViewLayout(floatingLL,wmParams);
            };
        }catch (Exception e){
            CameraUtil.setBoolean(Camera.SP_KEY,true,getApplicationContext());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        try {
            windowManager.removeViewImmediate(floatingLL);
        }catch (Exception e){

        }
        windowManager = null;
        floatingLL    = null;
        viewAdded     = false;
        super.onDestroy();
    }

    //更新悬浮窗口位置参数
    private void updateViewPosition(){
        wmParams.x = (int)(x-mTouchStartX);
        wmParams.y = (int)(y-mTouchStartY);
        windowManager.updateViewLayout(floatingLL,wmParams);
    }


    private void initWindow() {
        startForeground(1, new Notification());
        floatingLL = new LinearLayout(this);
        floatingLL.setWeightSum(3);
        floatingLL.setBackgroundColor(Color.parseColor("#88000000"));

        String[] texts = new String[]{"x:","y:","z:"};
        for (int i=0;i<3;i++){
            TextView tv = new TextView(this);
            tv.setText(texts[i]);
            tv.setTextColor(Color.WHITE);
            tv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.width  = (int) (60*getResources().getDisplayMetrics().density);
            params.height = (int) (50*getResources().getDisplayMetrics().density);
            params.weight = 1;
            floatingLL.addView(tv,params);
        }

        //创建罗盘布局
        windowManager = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();

        //至于手机最顶层
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT&&Build.VERSION.SDK_INT<Build.VERSION_CODES.N) {
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }else if(Build.VERSION.SDK_INT>=26){
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        }else{
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //不接受按键事件
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 背景色，透明为：PixelFormat.TRANSPARENT
        wmParams.format = PixelFormat.RGBA_8888;

        //悬浮窗相对屏幕的位置
        wmParams.gravity = Gravity.LEFT|Gravity.TOP;
        //以屏幕左上角为原点，设置x，y初始值
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        floatingLL.setOnTouchListener((v, event) -> {
            //获取相对屏幕的坐标，即以屏幕左上角为原点
            x = event.getRawX();
            // 25是系统状态栏的高度，也可以通过方法得到准确的值，自己微调就是了
            y = event.getRawY();
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    //获取相对View的坐标，即以此View左上角为原点
                    mTouchStartX = event.getX();
                    mTouchStartY = event.getY()+floatingLL.getHeight()/2;
                    break;
                case MotionEvent.ACTION_MOVE:
                    updateViewPosition();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    updateViewPosition();
                    mTouchStartX = mTouchStartY = 0;
                    break;
            }
            return true;
        });
    }


    public interface SensorListener{
        void sendSemsor(float x, float y, float z);
    }

}
