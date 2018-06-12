package com.chinamobile.gdwy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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


/**
 * Created by liangzhongtai on 2018/5/17.
 */

public class Camera extends CordovaPlugin{
    public final static String TAG = "Camera_Plugin";
    public final static int RESULTCODE_CAMERA = 10;
    public static final int RESULTCODE_PERMISSION = 20;
    public final static int RESULTCODE_LOCATION_PROVIDER = 30;

    public static String name;
    public static int mQuality=80;
    public static int targetWidth;
    public static int targetHeight;
    public CordovaInterface cordova;
    public CordovaWebView webView;
    public boolean first = true;
    public boolean floatingAngle;
    public boolean watermark;
    public boolean enterCamera;
    private CallbackContext callbackContext;
    private static float angleX;
    private static float angelY;
    private static float angelZ;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        this.webView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        //Log.d(TAG,"执行方法camera");
        if("coolMethod".equals(action)){
            name = args.getString(0);
            //LogUtil.d(TAG,"相机name="+name);
            if(args.length()>1)
                mQuality = args.getInt(1);
            if(args.length()>2)
                floatingAngle = args.getInt(2)==1;
            if(args.length()>3)
                watermark = args.getInt(3)==1;
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
                callbackContext.error("照相机功能异常");
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
                callbackContext.error("缺少权限,无法打开照相，定位服务或悬浮窗");
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


    private long distansce;
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG,"onActivityResult***********");
        enterCamera = false;
        distansce = System.currentTimeMillis();
        if(requestCode == RESULTCODE_LOCATION_PROVIDER){
            startWork(false);
        }else if(requestCode == RESULTCODE_CAMERA) {
            if(floatingAngle)cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();

                    angleX = SensorUtil.getInstance(cordova.getActivity()).x;
                    angelY = SensorUtil.getInstance(cordova.getActivity()).y;
                    angelZ = SensorUtil.getInstance(cordova.getActivity()).z;
                    if(TextUtils.isEmpty(CameraUtil.filePath)||!new File(CameraUtil.filePath).exists()){
                        message.what = HANDLER_ERROR;
                        message.obj = "图片本地存储路径丢失";
                        mHandler.sendMessage(message);
                        //callbackContext.error("图片本地存储路径丢失");
                        return;
                    }

                    //压缩图片
                    Bitmap bitmap = CameraUtil.decodeSampleBitmap(CameraUtil.filePath,targetWidth,targetHeight);

                    //LogUtil.d(TAG,"图片bitmap="+bitmap);
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
                    try {

                        message.what = HANDLER_FINISH;
                        message.obj = bitmap;
                        mHandler.sendMessage(message);
                        //processResultFromCamera(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        message.what = HANDLER_ERROR;
                        message.obj = "图片处理异常";
                        mHandler.sendMessage(message);
                        //callbackContext.error("图片处理异常");
                        manager(false);
                    }
                }
            }).start();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationUtils.getInstance(cordova.getActivity()).removeLocationUpdatesListener();
        SensorUtil.getInstance(cordova.getActivity()).removeSensorListener();
        cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
        if(mHandler!=null)mHandler.removeCallbacksAndMessages(null);
    }


    private void startWork(boolean showLocationSettting) {
        if(LocationUtils.getInstance(cordova.getActivity()).checkLocationProviders()){
            //打开传感器
            if(first)
            LocationUtils.getInstance(cordova.getActivity()).start();
            SensorUtil.getInstance(cordova.getActivity());

            //打开罗盘悬浮窗
            if(floatingAngle) {
                Log.d(TAG,"启动悬浮窗");
                cordova.getActivity().startService(new Intent(cordova.getActivity(), CameraService.class));
            }

            if(!first)manager(true);
            first = false;

            enterCamera = true;
            //打开相机
            CameraUtil.showCamera(cordova,this,RESULTCODE_CAMERA);


        }else{
            if(showLocationSettting)LocationUtils.showLocationSetting(cordova,this,RESULTCODE_LOCATION_PROVIDER);
            if(floatingAngle)cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
            callbackContext.error("无法定位，请打开定位服务");
        }
    }

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

    private void processResultFromCamera(Bitmap bitmap) throws Exception {
        //重新保存压缩过的图片
        String filePath = CameraUtil.saveBitmap(bitmap, mQuality, CameraUtil.getPhotoPath(), (int)angleX+"_"+CameraUtil.fileName);
        //LogUtil.d(TAG, "图片的路径=" + filePath);

        if (filePath == null) {
            callbackContext.error("Camera Error!");
        } else {
            //JSONObject obj = new JSONObject();
            //obj.put("image",CameraUtil.encodeBitmapForBase64(bitmap));
            JSONArray array = new JSONArray();
            array.put(0, CameraUtil.encodeBitmapForBase64(bitmap));
            array.put(1, SensorUtil.getInstance(cordova.getActivity()).showAngle());
            array.put(2, (int)angleX);
            array.put(3, (int)angelY);
            array.put(4, (int)angelZ);
            array.put(5, filePath);
            callbackContext.success(array);
        }
        manager(false);
        //Log.d(TAG,"耗费:"+(System.currentTimeMillis()-distansce)+"/ms");
    }


    private static int HANDLER_FINISH = 0;
    private static int HANDLER_ERROR  = 1;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==HANDLER_FINISH){
                try {
                    processResultFromCamera((Bitmap) msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(msg.what==HANDLER_ERROR){
                callbackContext.error((String) msg.obj);
            }
        }
    };
}
