package com.chinamobile.gdwy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.chinamobile.gdwy.CameraEditActivity.SNAP_SHOT_PATH_KEY;


/**
 * Created by liangzhongtai on 2018/5/17.
 */

public class Camera extends CordovaPlugin{
    public final static String TAG = "Camera_Plugin";
    public final static String SP_KEY = "sp_key_window";

    //cameraType:coolMethod动作
    // 打开相机
    public final static int CAMERA_TYPE_CAMERA       = 0;
    // 打开相册
    public final static int CAMERA_TYPE_ALBUM        = 1;
    // 涂鸦
    public final static int CAMERA_TYPE_EDIT         = 2;
    // 相机，相册权限检测
    public final static int CAMERA_TYPE_PERMISSION   = 3;


    // status:动作的状态
    // 正常
    public final static int NORMAL        = 0;
    // 正在人脸检测
    public final static int FACE_CHECKING = 1;
    // 人脸检测失败
    public final static int FACE_FAILE    = 2;

    // 相机返回码
    public final static int RESULTCODE_CAMERA = 10;
    // 相册返回码
    public final static int RESULTCODE_ALBUM  = 40;
    // 权限检查返回码
    public final static int RESULTCODE_PERMISSION = 20;
    // 定位权限返回码
    public final static int RESULTCODE_LOCATION_PROVIDER = 30;
    // 悬浮窗权限检查
    public final static int RESULTCODE_OVERLAY_WINDOW = 40;
    // 照片涂鸦返回码
    public final static int RESULTCODE_PIC_EDIT = 50;


    // 水印：用户名
    public static String name;
    // 图片的压缩质量
    public static int mQuality=80;
    // 图片宽度
    public static int targetWidth;
    // 图片高度
    public static int targetHeight;
    public CordovaInterface cordova;
    public CordovaWebView webView;
    // 第一次打开
    public boolean first = true;
    // 相机是否显示角度悬浮窗
    public boolean floatingAngle;
    //图片是否添加水印
    public boolean watermark;
    // 是否已经进入相机界面
    public boolean enterCamera;
    // 拍照后是否使用人脸检测
    public boolean faceCheck;
    public boolean preCamera;
    // 插件动作
    public static int cameraType = CAMERA_TYPE_CAMERA;
    private CallbackContext callbackContext;
    // 角度x：具体请参照MD文档
    private static float angleX;
    // 角度y
    private static float angelY;
    // 角度z
    private static float angelZ;
    private FaceUtil faceUtil;
    // 照片名前缀
    private String preTag = "";
    // 是否自定义文件名
    private boolean definedFileName;
    // 照片文件名
    private String fileName = "";
    // 水印Object
    private JSONObject waterMarkObj;
    public static volatile int angleTip = 0;

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
        if (args != null) {
            Log.d(TAG, args.toString());
        }
        if("coolMethod".equals(action)){
            name = args.getString(0);
            preTag = "";
            definedFileName = false;
            fileName = "";
            LogUtil.d(TAG,"相机name="+name);
            if (args.length() > 1) {
                mQuality = args.getInt(1);
            }
            LogUtil.d(TAG,"相机-9");
            if (args.length() > 2) {
                floatingAngle = args.getInt(2) == 1;
            }
            LogUtil.d(TAG,"相机-8");
            if (args.length() > 3) {
                watermark = args.getInt(3) == 1;
            }
            LogUtil.d(TAG,"相机-7");
            if (args.length() > 4) {
                cameraType = args.getInt(4);
            }
            LogUtil.d(TAG,"相机-6");
            if (args.length() > 5) {
                faceCheck = args.getInt(5) == 1;
            }
            LogUtil.d(TAG,"相机-5");
            if (args.length() > 6) {
                preCamera = args.getInt(6) == 1;
            }
            LogUtil.d(TAG,"相机-4");
            if (args.length() > 7) {
                preTag = args.getString(7);
                if (floatingAngle) {
                    LogUtil.d(TAG,"相机-4-1");
                    angleTip = Integer.parseInt(preTag);
                }
            }
            LogUtil.d(TAG,"相机-3");
            if (args.length() > 8) {
                definedFileName = args.getBoolean(8);
            }
            LogUtil.d(TAG,"相机-2");
            if (args.length() > 9) {
                fileName = args.getString(9);
            }
            LogUtil.d(TAG,"相机-1");
            if (args.length() > 10 && args.get(10) != null) {
                waterMarkObj = args.getJSONObject(10);
            }
            LogUtil.d(TAG,"相机0");
            if (cameraType == CAMERA_TYPE_PERMISSION) {
                LogUtil.d(TAG,"相机0-1");
                boolean hasPermission = PermissionHelper.hasPermission(this, Manifest.permission.CAMERA);
                PluginResult pluginResult;
                JSONArray array = new JSONArray();
                try {
                    array.put(0, hasPermission);
                    pluginResult = new PluginResult(PluginResult.Status.OK, array);
                    pluginResult.setKeepCallback(true);
                    LogUtil.d(TAG,"相机0-2-1");
                    callbackContext.sendPluginResult(pluginResult);
                } catch (JSONException e) {
                    e.printStackTrace();
                    sendResultForError("JSONArray构建异常",true);
                }
                LogUtil.d(TAG,"相机0-2-2");
                // 没有权限，打开权限配置弹窗
                if (!hasPermission) {
                    LogUtil.d(TAG,"相机0-2-3");
                    PermissionHelper.requestPermissions(this, RESULTCODE_PERMISSION, new String[]{ Manifest.permission.CAMERA });
                }
                return true;
            }

            if (cameraType == CAMERA_TYPE_CAMERA
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                &&CameraUtil.getBoolean(SP_KEY,cordova.getContext())&&floatingAngle) {
                sendResultForError("请确保应用悬浮窗权限已经打开",true);
                CameraUtil.setBoolean(SP_KEY,false, cordova.getContext());
            }

            // 权限
            try {
                if (!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.CAMERA) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_NETWORK_STATE) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_WIFI_STATE) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.READ_PHONE_STATE) ||
                    !PermissionHelper.hasPermission(this,Manifest.permission.INTERNET)) {
                    LogUtil.d(TAG,"相机0-3");
                    PermissionHelper.requestPermissions(this, RESULTCODE_PERMISSION, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.INTERNET
                    });
                } else {
                    LogUtil.d(TAG,"相机0-4");
                    startWork(true);
                }
            } catch (Exception e) {
                // 权限异常
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

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }


    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                if (requestCode == RESULTCODE_PERMISSION) {
                    sendResultForError(cameraType == CAMERA_TYPE_CAMERA
                            ? "缺少权限,无法打开照相，定位服务" : "缺少权限,无法打开相册", true);
                } else if (requestCode==RESULTCODE_OVERLAY_WINDOW) {
                    sendResultForError(cameraType == CAMERA_TYPE_CAMERA
                            ? "缺少权限,无法显示悬浮窗" : "缺少权限,无法打开相册", true);
                }
                return;
            }
        }
        switch (requestCode) {
            case RESULTCODE_PERMISSION:
                startWork(true);
                break;
            case RESULTCODE_OVERLAY_WINDOW:
                startService();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        enterCamera = false;
        Log.d(TAG,"requestCode="+requestCode);
        if (requestCode == RESULTCODE_LOCATION_PROVIDER) {
            startWork(false);
        } else if (requestCode == RESULTCODE_ALBUM){
            if (intent != null) {
                String path = CameraUtil.getRealPathFromUri(this.cordova.getActivity(), intent.getData());
                sendResultFromAlbum(path);
            } else {
                sendResultForError("选择已取消",true);
            }


        } else if(requestCode == RESULTCODE_CAMERA) {
            Log.d(TAG,"resultCode="+resultCode);
            if (floatingAngle) {
                cordova.getActivity().stopService(new Intent(cordova.getActivity(), CameraService.class));
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();

                    angleX = SensorUtil.getInstance(cordova.getActivity()).x;
                    angelY = SensorUtil.getInstance(cordova.getActivity()).y;
                    angelZ = SensorUtil.getInstance(cordova.getActivity()).z;
                    if (TextUtils.isEmpty(CameraUtil.filePath) || !new File(CameraUtil.filePath).exists()) {
                        message.what = resultCode ==  0 ? HANDLER_CANCEL : HANDLER_ERROR;
                        message.obj = resultCode == 0 ? "拍照已取消" : "图片本地存储路径丢失";
                        mHandler.sendMessage(message);
                        return;
                    }

                    // 压缩图片
                    Bitmap bitmap = CameraUtil.decodeSampleBitmap(CameraUtil.filePath, targetWidth, targetHeight);
                    // 增加水印
                    if (watermark && waterMarkObj != null) {
                        Log.d(TAG, "添加水印");
                        bitmap = watermarkBitmap(bitmap, waterMarkObj);
                        CameraUtil.saveBitmap(bitmap, CameraUtil.filePath);
                    }
                    // 人脸检测
                    if (faceCheck) {
                        faceUtil = new FaceUtil();
                        sendResultFaceChecking();
                        faceUtil.initFace(Camera.this, CameraUtil.filePath, bitmap);
                    } else {
                        sendResultFromCamera(CameraUtil.filePath, NORMAL);
                    }
                }
            }).start();
        // 返回涂鸦的照片
        } else if (requestCode == RESULTCODE_PIC_EDIT && intent!= null) {
            String path = intent.getStringExtra(CameraEditActivity.SNAP_SHOT_PATH_KEY);
            if (TextUtils.isEmpty(path)) {
                return;
            }
            PluginResult pluginResult;
            JSONArray array = new JSONArray();
            try {
                array.put(0, "");
                array.put(1, 0);
                array.put(2, 0);
                array.put(3, 0);
                array.put(4, 0);
                array.put(5, path);
                array.put(6, cameraType);

                array.put(7, NORMAL);
                array.put(8, preTag);
                pluginResult = new PluginResult(PluginResult.Status.OK, array);
                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                e.printStackTrace();
                sendResultForError("JSONArray构建异常", true);
            }
        }
    }

    @Override
    public void onDestroy() {
        LocationUtils.getInstance(cordova.getActivity()).removeLocationUpdatesListener();
        SensorUtil.getInstance(cordova.getActivity()).removeSensorListener();
        cordova.getActivity().stopService(new Intent(cordova.getActivity(), CameraService.class));
        if (mHandler!=null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }


    private void startWork(boolean showLocationSettting) {
        LogUtil.d(TAG,"相机1");
        if (LocationUtils.getInstance(cordova.getActivity()).checkLocationProviders()) {
            if (cameraType == CAMERA_TYPE_ALBUM) {
                //打开相册
                CameraUtil.showAlbum(cordova, this, RESULTCODE_ALBUM);
            } else if (cameraType == CAMERA_TYPE_EDIT) {
                //打开涂鸦
                Intent intent = new Intent(cordova.getContext(), CameraEditActivity.class);
                intent.putExtra(SNAP_SHOT_PATH_KEY, name);
                Log.d(TAG, "图片路径name=" + name);
                cordova.setActivityResultCallback(this);
                cordova.getActivity().startActivityForResult(intent, RESULTCODE_PIC_EDIT);
            } else if (cameraType == CAMERA_TYPE_PERMISSION) {

            } else {
                //打开传感器
                if (first) {
                    LocationUtils.getInstance(cordova.getActivity()).start();
                }
                SensorUtil.getInstance(cordova.getActivity());
                LogUtil.d(TAG,"相机2");
                //打开罗盘悬浮窗
                if (floatingAngle) {
                    Log.d(TAG, "启动悬浮窗");
                    //cordova.getActivity().startService(new Intent(cordova.getActivity(), CameraService.class));
                    if(Build.VERSION.SDK_INT >= 23) {
                        if (Settings.canDrawOverlays(cordova.getActivity())) {
                            //有悬浮窗权限开启服务绑定 绑定权限
                            startService();
                        } else {
                            //没有悬浮窗权限m,去开启悬浮窗权限
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                Uri packageURI = Uri.parse("package:" + cordova.getActivity().getPackageName());
                                intent.setData(packageURI);
                                cordova.getActivity().startActivityForResult(intent, RESULTCODE_OVERLAY_WINDOW);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            sendResultForError("请授予应用通知，悬浮窗权限!",false);
                            return;
                        }
                    } else {
                        //默认有悬浮窗权限
                       startService();
                    }
                }

                if (!first) {
                    manager(true);
                }
                first = false;
                LogUtil.d(TAG,"相机3");
                enterCamera = true;
                //打开相机
                CameraUtil.showCamera(cordova, this, RESULTCODE_CAMERA,preCamera);
            }

        } else {
            if (showLocationSettting) {
                LocationUtils.showLocationSetting(cordova,this,RESULTCODE_LOCATION_PROVIDER);
            }
            if (floatingAngle) {
                cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
            }
            sendResultForError("无法定位，请打开定位服务",true);
        }
    }

    private void startService(){
        Intent intent = new Intent(cordova.getActivity(), CameraService.class);
        cordova.getActivity().startService(intent);
    }

    // 添加水印
    public Bitmap watermarkBitmap(Bitmap bitmap, JSONObject waterMarkObj){
        List<String> list = new ArrayList<>();
        Iterator iter = waterMarkObj.keys();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            try {
                list.add(key + ": " + waterMarkObj.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        int widthest = 0;
        int lineHeight;
        int fontSize = 20;
        int margin = 15;
        int padding = 10;
        float density =  cordova.getContext().getResources().getDisplayMetrics().density;
        Paint paint = new Paint();
        paint.setTextSize(fontSize * density);
        Rect rect = new Rect();
        for (int i = 0; i < list.size(); i++) {
            paint.getTextBounds(list.get(i), 0, list.get(i).length(), rect);
            if (rect.width() < widthest) {
                continue;
            }
            widthest = rect.width();
        }
        lineHeight = (int)(rect.height() / density);
        // 添加水印, 外边距15，内边距5
        bitmap = CameraUtil.drawBgToLeftTop(cordova.getContext(), bitmap, (int)(widthest * 1.06 / density) ,
                (int) (lineHeight * 1.025) * list.size() + padding * (list.size() - 1), Color.BLACK, margin, margin);
        for (int i = 0; i < list.size(); i++) {
            bitmap = CameraUtil.drawTextToLeftTop(cordova.getContext(), bitmap, list.get(i),
                    (int)(fontSize * density), Color.WHITE, margin + padding, margin + padding * (i + 2) + lineHeight * i);
        }
        return bitmap;
    }

    // 暂停/恢复定位器监听/方向传感器监听
    private void manager(boolean start){
        if (start) {
            LocationUtils.getInstance(cordova.getActivity()).start();
            SensorUtil.getInstance(cordova.getActivity()).start();
            if (floatingAngle) {
                cordova.getActivity().startService(new Intent(cordova.getActivity(), CameraService.class));
            }
        } else {
            LocationUtils.getInstance(cordova.getActivity()).stop();
            SensorUtil.getInstance(cordova.getActivity()).stop();
            cordova.getActivity().stopService(new Intent(cordova.getActivity(),CameraService.class));
        }
    }

    // 向JS回调相机拍照成功的数据
    private void sendResultFromCamera(String path, int faceType){
        // 再次压缩
        Luban.with(cordova.getActivity())
            .load(path)
            .ignoreBy(80)
            .setTargetDir(CameraUtil.getPhotoPath())
            .filter(new CompressionPredicate() {
                @Override
                public boolean apply(String path) {
                    return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                }
            })
            .setCompressListener(new OnCompressListener() {
                @Override
                public void onStart() {
                    // TODO 压缩开始前调用，可以在方法内启动 loading UI
                }

                @Override
                public void onSuccess(File file) {
                    // TODO 压缩成功后调用，返回压缩后的图片文件
                    String filePath = file.getAbsolutePath();
                    if (filePath == null) {
                        sendResultForError("相机拍照异常",true);
                    } else {
                        //调整压缩后的照片的方向
                        int angle = CameraUtil.readPictureDegree(filePath);
                        if (angle > 0) {
                            Bitmap rotateBitmap = CameraUtil.rotaingImageView(90, BitmapFactory.decodeFile(filePath));
                            CameraUtil.saveBitmap(rotateBitmap, filePath);
                        }
                        String oriName = new File(path).getName();
                        String lastName = (int)angleX + "_" + ("".equals(preTag)? "" : (preTag + "_")) + CameraUtil.fileName;
                        if (definedFileName && TextUtils.isEmpty(fileName)) {
                            lastName = oriName;
                        }
                        if (definedFileName && !TextUtils.isEmpty(fileName)) {
                            lastName = fileName;
                        }
                        String lastPath = CameraUtil.saveBitmap(
                                BitmapFactory.decodeFile(filePath),
                                mQuality,
                                CameraUtil.getPhotoPath(),
                                lastName);
                        // 删除多余的照片
                        if (!lastName.equals(oriName)) {
                            new File(path).delete();
                        }
                        file.delete();


                        LogUtil.d(TAG, "相机照片的大小=" + file.length());
                        LogUtil.d(TAG, "相机照片的名称=" + lastName);
                        LogUtil.d(TAG, "相机照片的路径=" + lastPath);
                        PluginResult pluginResult;
                        JSONArray array = new JSONArray();
                        try {
                            array.put(0, /*CameraUtil.encodeBitmapForBase64(bitmap)*/"");
                            array.put(1, SensorUtil.getInstance(cordova.getActivity()).showAngle());
                            array.put(2, (int)angleX);
                            array.put(3, (int)angelY);
                            array.put(4, (int)angelZ);
                            array.put(5, lastPath);
                            array.put(6, cameraType);
                            array.put(7, faceType);
                            array.put(8, preTag);
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

                @Override
                public void onError(Throwable e) {
                    // TODO 当压缩过程出现问题时调用
                    e.printStackTrace();
                    Log.d(TAG,"e="+e.toString());
                    sendResultForError("照片压缩异常",true);
                    manager(false);
                }
            }).launch();
    }

    // 向JS回调相册的选择结果数据
    private void sendResultFromAlbum(String path) {
        LogUtil.d(TAG, "相册的路径=" + path);
        if (path == null) {
            sendResultForError("相册图片读取异常",true);
        } else {
            Luban.with(cordova.getActivity())
                    .load(path)
                    .ignoreBy(80)
                    .setTargetDir(CameraUtil.getPhotoPath())
                    .filter(new CompressionPredicate() {
                        @Override
                        public boolean apply(String path) {
                            return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                        }
                    })
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        @Override
                        public void onSuccess(File file) {
                            // TODO 压缩成功后调用，返回压缩后的图片文件
                            String filePath = file.getAbsolutePath();
                            LogUtil.d(TAG, "相册照片的大小=" + file.length());
                            LogUtil.d(TAG, "相册照片的路径=" + filePath);
                            if (filePath == null) {
                                sendResultForError("读取相册图片失败",true);
                            } else {
                                //调整压缩后的照片的方向
                                int angle = CameraUtil.readPictureDegree(filePath);
                                if (angle > 0) {
                                    Bitmap rotateBitmap = CameraUtil.rotaingImageView(90, BitmapFactory.decodeFile(filePath));
                                    CameraUtil.saveBitmap(rotateBitmap, filePath);
                                }
                                String oriName = new File(path).getName();
                                String lastName = "album" + "_" + ("".equals(preTag) ? "" : (preTag+"_")) + System.currentTimeMillis() + ".jpg";
                                if (definedFileName && TextUtils.isEmpty(fileName)) {
                                    lastName = oriName;
                                }
                                if (definedFileName && !TextUtils.isEmpty(fileName)) {
                                    lastName = fileName;
                                }
                                String lastPath = CameraUtil.saveBitmap(
                                        BitmapFactory.decodeFile(filePath),
                                        100,
                                        CameraUtil.getPhotoPath(),
                                        lastName);

                                PluginResult pluginResult;
                                JSONArray array = new JSONArray();
                                try {
                                    array.put(0, /*CameraUtil.encodeBitmapForBase64(bitmap)*/"");
                                    array.put(1, SensorUtil.getInstance(cordova.getActivity()).showAngle());
                                    array.put(2, 0);
                                    array.put(3, 0);
                                    array.put(4, 0);
                                    array.put(5, lastPath);
                                    array.put(6, cameraType);
                                    array.put(7, NORMAL);
                                    array.put(8, preTag);
                                    pluginResult = new PluginResult(PluginResult.Status.OK, array);
                                    pluginResult.setKeepCallback(true);
                                    callbackContext.sendPluginResult(pluginResult);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    sendResultForError("JSONArray构建异常", true);
                                }

                            }
                            manager(false);
                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO 当压缩过程出现问题时调用
                            e.printStackTrace();
                            Log.d(TAG,"e="+e.toString());
                            sendResultForError("照片压缩异常",true);
                            manager(false);
                        }
                    }).launch();
        }
    }

    // 正在人脸识别中
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
            array.put(8, preTag);
            pluginResult = new PluginResult(PluginResult.Status.OK, array);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } catch (JSONException e) {
            e.printStackTrace();
            sendResultForError("JSONArray构建异常",true);
        }
    }

    // 向JS回调插件执行异常的数据
    public void sendResultForError(String message,boolean keep){
        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
        pluginResult.setKeepCallback(keep);
        callbackContext.sendPluginResult(pluginResult);
    }

    // 拍照结束
    private static int HANDLER_FINISH = 0;
    // 拍照异常
    private static int HANDLER_ERROR  = 1;
    // 取拍照消
    private static int HANDLER_CANCEL = 2;
    // 人脸检测结束
    public final static int HANDLER_CHECK_FINISH  = 10;
    // 人脸检测开始
    public final static int HANDLER_CHECK_START   = 11;
    // 人脸检测未通过
    public final static int HANDLER_CHECK_NOT     = 12;
    // 人脸检测通过
    public final static int HANDLER_CHECK_PASS    = 13;
    // 人脸检测数大于1
    public final static int HANDLER_CHECK_MORE    = 14;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG,"msg.what="+msg.what);
            if (msg.what == HANDLER_FINISH) {
                try {
                    Object[] objs = (Object[])msg.obj;
                    sendResultFromCamera((String) objs[0],(int)objs[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msg.what == HANDLER_ERROR || msg.what == HANDLER_CANCEL) {
                sendResultForError((String) msg.obj,true);
            } else if (msg.what == HANDLER_CHECK_FINISH) {
            } else if (msg.what == HANDLER_CHECK_START) {
            } else if (msg.what == HANDLER_CHECK_NOT) {
                sendResultFromCamera((String) msg.obj, FACE_FAILE);
            } else if (msg.what == HANDLER_CHECK_MORE) {
                sendResultForError((String) msg.obj, false);
            } else if (msg.what == HANDLER_CHECK_PASS) {
                sendResultFromCamera((String) msg.obj,NORMAL);
            }
        }
    };


}
