package com.chinamobile.gdwy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;


/**
 * Created by liangzhongtai on 2018/5/17.
 */

public class Camera extends CordovaPlugin{
    public final static String TAG = "Camera_Plugin";
    public final static String SP_KEY = "sp_key_window";

    //cameraType:coolMethod动作
    //打开相机
    public final static int CAMERA_TYPE_CAMERA = 0;
    //打开相册
    public final static int CAMERA_TYPE_ALBUM  = 1;

    //status:动作的状态
    //正常
    public final static int NORMAL        = 0;
    //正在人脸检测
    public final static int FACE_CHECKING = 1;

    //相机返回码
    public final static int RESULTCODE_CAMERA = 10;
    //相册返回码
    public final static int RESULTCODE_ALBUM  = 40;
    //权限检查返回码
    public final static int RESULTCODE_PERMISSION = 20;
    //定位权限返回码
    public final static int RESULTCODE_LOCATION_PROVIDER = 30;


    //水印：用户名
    public static String name;
    //图片的压缩质量
    public static int mQuality=80;
    //图片宽度
    public static int targetWidth;
    //图片高度
    public static int targetHeight;
    public CordovaInterface cordova;
    public CordovaWebView webView;
    //第一次打开
    public boolean first = true;
    //相机是否显示角度悬浮窗
    public boolean floatingAngle;
    //图片是否添加水印
    public boolean watermark;
    //是否已经进入相机界面
    public boolean enterCamera;
    //拍照后是否使用人脸检测
    public boolean faceCheck;
    //
    public boolean preCamera;
    //插件动作
    public static int cameraType = CAMERA_TYPE_CAMERA;
    private CallbackContext callbackContext;
    //角度x：具体请参照MD文档
    private static float angleX;
    //角度y
    private static float angelY;
    //角度z
    private static float angelZ;
    private FaceUtil faceUtil;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        this.webView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        Log.d(TAG,"执行方法camera");
        if("coolMethod".equals(action)){
            name = args.getString(0);
            LogUtil.d(TAG,"相机name="+name);
            if(args.length()>1)
                mQuality = args.getInt(1);
            if(args.length()>2)
                floatingAngle = args.getInt(2)==1;
            if(args.length()>3)
                watermark = args.getInt(3)==1;
            if(args.length()>4)
                cameraType= args.getInt(4);
            if(args.length()>5)
                faceCheck = args.getInt(5)==1;
            if(args.length()>6)
                preCamera = args.getInt(6)==1;
            if(cameraType == CAMERA_TYPE_CAMERA
                    && Build.VERSION.SDK_INT>=Build.VERSION_CODES.N
                    &&CameraUtil.getBoolean(SP_KEY,cordova.getContext())){
                sendResultForError("请确保应用悬浮窗权限已经打开",true);
                CameraUtil.setBoolean(SP_KEY,false, cordova.getContext());
            }

            //权限
            try {
                if(!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        ||!PermissionHelper.hasPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        ||!PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                        ||!PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    PermissionHelper.requestPermissions(this,RESULTCODE_PERMISSION,new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                }else{
                    startWork(true);
                }
            }catch (Exception e){
                //权限异常
                callbackContext.error(cameraType==0?"照相机开启异常":"相册开启异常");
                manager(false);
                return true;
            }
            return true;
        }
        return super.execute(action, args, callbackContext);
    }

    @Override
    public Bundle onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }


    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                sendResultForError(cameraType==CAMERA_TYPE_CAMERA
                        ?"缺少权限,无法打开照相，定位服务或悬浮窗":"缺少权限,无法打开相册",true);
                return;
            }
        }
        switch (requestCode) {
            case RESULTCODE_PERMISSION:
                startWork(true);
                break;
            default:
                break;
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        enterCamera = false;
        if(requestCode == RESULTCODE_LOCATION_PROVIDER){
            startWork(false);
        }else if(requestCode == RESULTCODE_ALBUM){
            if (intent != null) {
                String path = CameraUtil.getRealPathFromUri(this.cordova.getActivity(), intent.getData());
                sendResultFromAlbum(path);
            } else {
                sendResultForError("图片损坏，请重新选择",true);
            }


        }else if(requestCode == RESULTCODE_CAMERA) {
            Log.d(TAG,"resultCode="+resultCode);
            if(floatingAngle)cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();

                    angleX = SensorUtil.getInstance(cordova.getActivity()).x;
                    angelY = SensorUtil.getInstance(cordova.getActivity()).y;
                    angelZ = SensorUtil.getInstance(cordova.getActivity()).z;
                    if(TextUtils.isEmpty(CameraUtil.filePath)||!new File(CameraUtil.filePath).exists()){
                        message.what = resultCode==0?HANDLER_CANCEL:HANDLER_ERROR;
                        message.obj = resultCode==0?"拍照已取消":"图片本地存储路径丢失";
                        mHandler.sendMessage(message);
                        //callbackContext.error("图片本地存储路径丢失");
                        return;
                    }



                    //压缩图片
                    Bitmap bitmap = CameraUtil.decodeSampleBitmap(CameraUtil.filePath,targetWidth,targetHeight);

                    //人脸检测
                    if(faceCheck) {
                        faceUtil = new FaceUtil();
                        sendResultFaceChecking();
                        faceUtil.initFace(Camera.this,bitmap);
                    }else {
                        continueDisposeBitmap(bitmap);
                    }
                }
            }).start();

        }
    }

    @Override
    public void onDestroy() {
        LocationUtils.getInstance(cordova.getActivity()).removeLocationUpdatesListener();
        SensorUtil.getInstance(cordova.getActivity()).removeSensorListener();
        cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
        if(mHandler!=null)mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private void startWork(boolean showLocationSettting) {

        if(LocationUtils.getInstance(cordova.getActivity()).checkLocationProviders()){
            if(cameraType==CAMERA_TYPE_ALBUM){
                //打开相册
                CameraUtil.showAlbum(cordova, this, RESULTCODE_ALBUM);
            }else {
                //打开传感器
                if (first)
                    LocationUtils.getInstance(cordova.getActivity()).start();
                SensorUtil.getInstance(cordova.getActivity());

                //打开罗盘悬浮窗
                if (floatingAngle) {
                    Log.d(TAG, "启动悬浮窗");
                    cordova.getActivity().startService(new Intent(cordova.getActivity(), CameraService.class));
                }

                if (!first) manager(true);
                first = false;

                enterCamera = true;
                //打开相机
                CameraUtil.showCamera(cordova, this, RESULTCODE_CAMERA,preCamera);
            }

        }else{
            if(showLocationSettting)LocationUtils.showLocationSetting(cordova,this,RESULTCODE_LOCATION_PROVIDER);
            if(floatingAngle)cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
            sendResultForError("无法定位，请打开定位服务",true);
        }
    }

    public void continueDisposeBitmap(Bitmap bitmap){
        if(watermark) {
            //时间
            String date = CameraUtil.formatDate("yyyy-MM-dd HH:mm");

            //LogUtil.d(TAG,"时间date="+date);

            //经度-维度
            Location location = LocationUtils.getInstance(cordova.getActivity()).showLocation();
            String address = "";
            if (location != null) {
                address = location.getLatitude() + ":" + location.getLongitude();
            }

            //LogUtil.d(TAG,"经纬度address="+address);

            //添加水印
            CameraUtil.drawTextToLeftBottom(cordova.getActivity().getApplicationContext(), bitmap, date + address, 12, 0xff9900, 14, 42);
            CameraUtil.drawTextToLeftBottom(cordova.getActivity().getApplicationContext(), bitmap, name, 12, 0xff9900, 14, 14);
        }
        //将图片返回
        Message message = new Message();
        try {

            message.what = HANDLER_FINISH;
            message.obj = bitmap;
            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            message.what = HANDLER_ERROR;
            message.obj = "图片处理异常";
            mHandler.sendMessage(message);
            manager(false);
        }
    }

    //暂停/恢复定位器监听/方向传感器监听
    private void manager(boolean start){
        if(start){
            LocationUtils.getInstance(cordova.getActivity()).start();
            SensorUtil.getInstance(cordova.getActivity()).start();
            if(floatingAngle)
            cordova.getActivity().startService(new Intent(cordova.getActivity(),CameraService.class));
        }else {
            LocationUtils.getInstance(cordova.getActivity()).stop();
            SensorUtil.getInstance(cordova.getActivity()).stop();
            cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
        }
    }

    //向JS回调相机拍照成功的数据
    private void sendResultFromCamera(Bitmap bitmap){
        //重新保存压缩过的图片
        String filePath = CameraUtil.saveBitmap(bitmap, mQuality, CameraUtil.getPhotoPath(), (int)angleX+"_"+CameraUtil.fileName);
        //LogUtil.d(TAG, "图片的路径=" + filePath);

        if (filePath == null) {
            sendResultForError("相机异常",true);
        } else {
            PluginResult pluginResult;
            JSONArray array = new JSONArray();
            try {
                array.put(0, /*CameraUtil.encodeBitmapForBase64(bitmap)*/"");
                array.put(1, SensorUtil.getInstance(cordova.getActivity()).showAngle());
                array.put(2, (int)angleX);
                array.put(3, (int)angelY);
                array.put(4, (int)angelZ);
                array.put(5, filePath);
                array.put(6,cameraType);
                array.put(7,NORMAL);
                pluginResult = new PluginResult(PluginResult.Status.OK, array);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                e.printStackTrace();
                sendResultForError("JSONArray构建异常",true);
            }

        }
        manager(false);
    }

    //向JS回调相册的选择结果数据
    private void sendResultFromAlbum(String path){
        //压缩图片
        Bitmap bitmap = CameraUtil.decodeSampleBitmap(path,targetWidth,targetHeight);
        //重新保存压缩过的图片
        String filePath = CameraUtil.saveBitmap(bitmap, mQuality, CameraUtil.getPhotoPath(), "album"+"_"+System.currentTimeMillis()+ ".jpg");

        PluginResult pluginResult;
        JSONArray array = new JSONArray();
        try {
            array.put(0, /*CameraUtil.encodeBitmapForBase64(bitmap)*/"");
            array.put(1, SensorUtil.getInstance(cordova.getActivity()).showAngle());
            array.put(2, 0);
            array.put(3, 0);
            array.put(4, 0);
            array.put(5, filePath);
            array.put(6,cameraType);
            array.put(7,NORMAL);
            pluginResult = new PluginResult(PluginResult.Status.OK, array);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } catch (JSONException e) {
            e.printStackTrace();
            sendResultForError("JSONArray构建异常",true);
        }

    }

    //正在人脸识别中
    private void sendResultFaceChecking(){
        PluginResult pluginResult;
        JSONArray array = new JSONArray();
        try {
            array.put(0, "");
            array.put(1, 0);
            array.put(2, 0);
            array.put(3, 0);
            array.put(4, 0);
            array.put(5, 0);
            array.put(6,cameraType);
            array.put(7,FACE_CHECKING);
            pluginResult = new PluginResult(PluginResult.Status.OK, array);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } catch (JSONException e) {
            e.printStackTrace();
            sendResultForError("JSONArray构建异常",true);
        }
    }

    //向JS回调插件执行异常的数据
    public void sendResultForError(String message,boolean keep){
        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
        pluginResult.setKeepCallback(keep);
        callbackContext.sendPluginResult(pluginResult);
    }

    //拍照结束
    private static int HANDLER_FINISH = 0;
    //拍照异常
    private static int HANDLER_ERROR  = 1;
    //取拍照消
    private static int HANDLER_CANCEL = 2;
    //人脸检测结束
    public final static int HANDLER_CHECK_FINISH  = 10;
    //人脸检测开始
    public final static int HANDLER_CHECK_START   = 11;
    //人脸检测未通过
    public final static int HANDLER_CHECK_NOT     = 12;
    //人脸检测通过
    public final static int HANDLER_CHECK_PASS    = 13;
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==HANDLER_FINISH){
                try {
                    sendResultFromCamera((Bitmap) msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(msg.what==HANDLER_ERROR||msg.what==HANDLER_CANCEL){
                sendResultForError((String) msg.obj,true);
            }else if(msg.what==HANDLER_CHECK_FINISH){
            }else if(msg.what==HANDLER_CHECK_START){
            }else if(msg.what==HANDLER_CHECK_NOT){
                sendResultForError((String) msg.obj, false);
            }else if(msg.what==HANDLER_CHECK_PASS){
                continueDisposeBitmap((Bitmap) msg.obj);
            }
        }
    };


}
