package com.chinamobile.gdwy;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.os.Message;
import android.util.Log;

/**
 * Created by liangzhongtai on 2018/6/28.
 */

public class FaceUtil {
    private FaceDetector faceDetector = null;
    private FaceDetector.Face[] face = null;
    private Bitmap srcFace = null;
    private final int N_MAX = 2;
    private Camera cordova;
    private Bitmap oriBitmap;
    private String path;
    /**
     * 初始化人脸识别的图片
     * @param bitmap:要检测的图片
     * */
    public void initFace(Camera cordova,String path,Bitmap bitmap){
        this.cordova = cordova;
        this.path = path;
        oriBitmap = bitmap;
        srcFace = bitmap.copy(Bitmap.Config.RGB_565, true);
        int w = srcFace.getWidth();
        int h = srcFace.getHeight();
        Log.d(Camera.TAG, "待检测图像宽高: w = " + w + "h = " + h);
        faceDetector = new FaceDetector(w, h, N_MAX);
        face = new FaceDetector.Face[N_MAX];
        cordova.mHandler.sendEmptyMessage(Camera.HANDLER_CHECK_START);
        checkFaceThread.start();
    }

    /**
     *检测得到人脸区域
     * */
    public Bitmap detectFace(){
        int possibleFace = faceDetector.findFaces(srcFace, face);
        Log.d(Camera.TAG, "检测到可能的人脸个数：= " + possibleFace);
        Message message = new Message();
        if(possibleFace==0){
            message.what = Camera.HANDLER_CHECK_NOT;
            message.obj = path;
            cordova.mHandler.sendMessage(message);
            return null;
        }
        int effect = 0;
        for(int i=0; i<possibleFace; i++){
            FaceDetector.Face f  = face[i];
            PointF midPoint = new PointF();
            float dis = f.eyesDistance();
            f.getMidPoint(midPoint);
            int dd = (int)(dis);
            //Point eyeLeft = new Point((int)(midPoint.x - dis/2), (int)midPoint.y);
            //Point eyeRight = new Point((int)(midPoint.x + dis/2), (int)midPoint.y);
            Rect faceRect = new Rect((int)(midPoint.x - dd), (int)(midPoint.y - dd), (int)(midPoint.x + dd), (int)(midPoint.y + dd));
            if(checkFace(faceRect)){
                /*Canvas canvas = new Canvas(srcFace);
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setStrokeWidth(8);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.GREEN);
                canvas.drawCircle(eyeLeft.x, eyeLeft.y, 20, p);
                canvas.drawCircle(eyeRight.x, eyeRight.y, 20, p);
                canvas.drawRect(faceRect, p);*/
                effect ++;
            }
        }
        if(effect==0){
            message.what = Camera.HANDLER_CHECK_NOT;
            message.obj = path;
        }else if(effect==1){
            message.what = Camera.HANDLER_CHECK_PASS;
            message.obj = path;
        }else{
            message.what = Camera.HANDLER_CHECK_MORE;
            message.obj = "检测到人脸个数大于1,请重新拍照";
        }
        if(cordova!=null)
        cordova.mHandler.sendMessage(message);
        return srcFace;

    }

    /**
     * 检测得到的人脸矩形是否符合人脸标准
     * @param rect 人脸矩形
     * */
    public boolean checkFace(Rect rect){
        int w = rect.width();
        int h = rect.height();
        int s = w*h;
        Log.d(Camera.TAG, "人脸 宽w = " + w + "高h = " + h + "人脸面积 s = " + s);
        if(s < 10000){
            Log.d(Camera.TAG, "无效人脸，舍弃.");
            return false;
        }else{
            Log.d(Camera.TAG, "有效人脸，保存.");
            return true;
        }
    }


    private Thread checkFaceThread = new Thread(){

        @Override
        public void run() {
            detectFace();
            Message m = new Message();
            m.what = Camera.HANDLER_CHECK_FINISH;
            if(cordova!=null)
            cordova.mHandler.sendMessage(m);
        }

    };

}
