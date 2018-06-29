package com.chinamobile.gdwy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Base64;


import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by liangzhongtai on 2018/5/17.
 */

public class CameraUtil {
    public static String fileName;
    public static String filePath;
    public static String FILEPROVIDER  = ".provider";
    public static int dataType;

    //打开系统相机
    public static  void showCamera(CordovaInterface cordova, CordovaPlugin plugin, int resultCode){

        LogUtil.setLog(true);
        // 调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 取当前时间为照片名
        fileName = System.currentTimeMillis()+ ".jpg";
        filePath = getPhotoPath() + fileName;
        // 通过文件创建一个uri中
        Uri imageUri = null;
        if(Build.VERSION.SDK_INT>=24) {
            try {
                File imageFile = new File(filePath);
                imageUri= FileProvider.getUriForFile(cordova.getActivity(), cordova.getActivity().getApplication().getPackageName() + FILEPROVIDER, imageFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            imageUri = Uri.fromFile(new File(filePath));
        }
        LogUtil.d("Camera_","filePath="+filePath);
        // 保存uri对应的照片于指定路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        LogUtil.d("Camera_",""+resultCode);
        cordova.setActivityResultCallback(plugin);
        cordova.getActivity().startActivityForResult(intent, resultCode);
    }

    public static void showAlbum(CordovaInterface cordova, CordovaPlugin plugin, int resultCode){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        cordova.setActivityResultCallback(plugin);
        cordova.getActivity().startActivityForResult(intent, resultCode);

    }

    //获得照片路径
    public static String getPhotoPath() {
        return Environment.getExternalStorageDirectory() + "/DCIM/";
    }

    //压缩图片
    public static Bitmap decodeSampleBitmap(String path, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width  = options.outWidth;
        reqHeight = reqHeight<=0?height/2:reqHeight;
        reqWidth  = reqWidth<=0?width/2:reqWidth;
        int inSampleSize;
        final int heightRatio = Math.round((float)height/(float)reqHeight);
        final int widthRatio = Math.round((float)width/(float)reqWidth);
        inSampleSize = heightRatio<widthRatio?heightRatio:widthRatio;
        return inSampleSize;
    }

    /**
     * 将图片转成Base64
     * @param bitmap
     * */
    public static String encodeBitmapForBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = null;
        byte[] buffer = null;
        try {
            baos = new ByteArrayOutputStream();
            /*if(path.endsWith("jpeg")*//*||path.endsWith("jpg")*//*){*/
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,baos);
            /*}else if(path.endsWith("webp")){
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100,baos);
            }else{
                bitmap.compress(Bitmap.CompressFormat.PNG,  100,baos);
            }*/
            baos.close();
            buffer = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(baos!=null) try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(buffer==null)return "";
            return Base64.encodeToString(buffer,0,buffer.length,Base64.DEFAULT);
        }
    }


    /**
    * 保存压缩质量的Bitmap
    *
    *
    * */
    public static String saveBitmap(Bitmap bitmap,int quality,String cachePath,String fileName) {
        String filePath = null;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //检查路径是否存在
                File dir = new File(cachePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                //检查文件是否存在
                File file = new File(cachePath, fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                filePath = file.getAbsolutePath();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return filePath;
        }

    }


    /**
     * 水印_绘制文字到左下方
     * @param context
     * @param bitmap
     * @param text
     * @param size
     * @param color
     * @param paddingLeft
     * @param paddingBottom
     * @return
     */
    public static  Bitmap drawTextToLeftBottom(Context context, Bitmap bitmap, String text,
                                               int size, int color, int paddingLeft, int paddingBottom) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(dp2px(context, size));
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return drawTextToBitmap(bitmap, text, paint, dp2px(context, paddingLeft),
                bitmap.getHeight() - dp2px(context, paddingBottom));
    }

    /**
     * 绘制图片文字
     * @param bitmap
     * @param text
     * @param paint
     * @param paddingLeft
     * @param paddingTop
     * @return
     */
    public static  Bitmap drawTextToBitmap(Bitmap bitmap, String text,
                                           Paint paint,int paddingLeft, int paddingTop) {
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawText(text, paddingLeft, paddingTop, paint);
        return bitmap;
    }


    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }



    /**
     * dip转pix
     * @param context
     * @param dp
     * @return
     */
    public static  int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 根据时间戳转成指定的format格式
     * @param format
     * @return
     */
    public static String formatDate(String format) {
        Date date = new Date();
        final SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }


    public static void setBoolean(String spKey,boolean spValue,Context context){
        //取得活动的Preferences对象
        SharedPreferences settings = context.getSharedPreferences(context.getPackageName(),Activity.MODE_PRIVATE);
        //取得编辑对象
        SharedPreferences.Editor editor = settings.edit();
        //添加值
        editor.putBoolean(spKey,spValue);
        //提交保存
        editor.commit();
    }

    public static boolean getBoolean(String spKey,Context context){
        //取得活动的Preferences对象
        SharedPreferences settings = context.getSharedPreferences(context.getPackageName(),Activity.MODE_PRIVATE);
        //取得值
        return settings.getBoolean(spKey,true);
    }

    /**
     * 检查app是否在前台
     * */
    public static  boolean checkTaskIsTop(Context context,String packageName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = manager.getRunningTasks(100);
        int i=0;
        if(list!=null)
            for (ActivityManager.RunningTaskInfo info : list) {
                if (info.topActivity.getPackageName().equals(packageName)|| info.baseActivity.getPackageName().equals
                        (packageName)) {
                    if(i==0)return true;
                }
                i++;
            }
        return false;
    }
}
