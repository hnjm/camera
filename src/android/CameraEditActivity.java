package com.chinamobile.gdwy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by liangzhongtai on 2020/2/12.
 */

public class CameraEditActivity extends Activity {
    public static final String SNAP_SHOT_PATH_KEY = "snap_shot_path_key";
    private Button backBtn;
    private PaintableImageView paintIV;
    private EditText editText;
    // private GraffitiView paintIV;
    private RadioGroup colorsRG;
    private RadioGroup typesRG;
    private Button preBtn;
    private Button lastBtn;
    private Button cancelDrawBtn;
    private Button confirmBtn;
    private SeekBar seekBar;
    private String snapShotPath;
    private String fileName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snap_shot_layout);

        backBtn = (Button) findViewById(R.id.btn_back);
        paintIV = (PaintableImageView) findViewById(R.id.image_view);
        editText = (EditText) findViewById(R.id.edit_text);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        // paintIV = (GraffitiView) findViewById(R.id.image_view);
        colorsRG = (RadioGroup) findViewById(R.id.rg_colors);
        typesRG  = (RadioGroup) findViewById(R.id.rg_types);
        preBtn = (Button) findViewById(R.id.btn_pre);
        lastBtn = (Button) findViewById(R.id.btn_last);
        cancelDrawBtn = (Button) findViewById(R.id.btn_cancel);
        confirmBtn = (Button) findViewById(R.id.btn_confirm);
        snapShotPath = getIntent().getStringExtra(SNAP_SHOT_PATH_KEY);
        Log.d(Camera.TAG, "snapShotPath=" + snapShotPath);
        String[] arrs = snapShotPath.split("/");
        fileName = arrs[arrs.length - 1];
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                paintIV.setText(editText.getText().toString());
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    paintIV.setText(editText.getText().toString());
                }
                return false;
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*拖动条停止拖动时调用 */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(Camera.TAG, "拖动停止");
            }
            /*拖动条开始拖动时调用*/
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(Camera.TAG, "开始拖动");
            }
            /* 拖动条进度改变时调用*/
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(Camera.TAG, "当前进度为：" + progress + "%");
                paintIV.setTextSize(progress);
                // editText.setTextSize(paintIV.getTextSize());
            }
        });
        colorsRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.btn_white) {
                    paintIV.setColor("#FFFFFF");
                } else if (checkedId == R.id.btn_red) {
                    paintIV.setColor("#F23A15");
                } else if (checkedId == R.id.btn_yellow) {
                    paintIV.setColor("#FDC151");
                } else if (checkedId == R.id.btn_green) {
                    paintIV.setColor("#03B90C");
                } else if (checkedId == R.id.btn_blue) {
                    paintIV.setColor("#1E82FC");
                } else if (checkedId == R.id.btn_purple) {
                    paintIV.setColor("#9A62FD");
                }
            }
        });
        typesRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.btn_line) {
                    paintIV.setLineType(LineInfo.LineType.NormalLine);
                } else if (checkedId == R.id.btn_ring) {
                    paintIV.setLineType(LineInfo.LineType.RingLine);
                } else if (checkedId == R.id.btn_text) {
                    paintIV.setLineType(LineInfo.LineType.TextLine);
                } else if (checkedId == R.id.btn_arrow) {
                    paintIV.setLineType(LineInfo.LineType.ArrowLine);
                } else if (checkedId == R.id.btn_mosaic) {
                    paintIV.setLineType(LineInfo.LineType.MosaicLine);
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(SNAP_SHOT_PATH_KEY, snapShotPath);
                setResult(Camera.RESULTCODE_PIC_EDIT, intent);
                finish();
            }
        });
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintIV.setPreStep();
            }
        });
        lastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintIV.setLastStep();
            }
        });
        cancelDrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintIV.cancelDrawLastLine();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除原文件
                File file = new File(snapShotPath);
                if (file.exists()) {
                    file.delete();
                }
                // 保存涂鸦后的文件
                String[] fileNameArr = fileName.split("[.]");
                String path = paintIV.saveBitmap(fileNameArr[0] + "1" + "." + fileNameArr[1]);
                Intent intent = new Intent();
                intent.putExtra(SNAP_SHOT_PATH_KEY, path);
                setResult(Camera.RESULTCODE_PIC_EDIT, intent);
                finish();
            }
        });
        ViewTreeObserver viewTreeObserver = paintIV.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 自适应调整图片空间大小，并根据其大小压缩图片
                autoFitImageView(snapShotPath);

                ViewTreeObserver vto = paintIV.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    vto.removeOnGlobalLayoutListener(this);
                } else {
                    vto.removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private void autoFitImageView(String imgPath) {
        int imageViewHeight = paintIV.getHeight();

        Bitmap bitmap = getCompressedBitmap(imgPath, imageViewHeight);

        if (null != bitmap) {
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
            layoutParams.gravity = Gravity.CENTER;
            paintIV.setLayoutParams(layoutParams);
            paintIV.requestLayout();
            paintIV.setImageBitmap(bitmap);
        }
    }

    private static final int SAMPLE_SIZE = 2;
    public static Bitmap getCompressedBitmap(String filePath, int needHeight) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            // 第一次只解码原始长宽的值
            o.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, o);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            // 根据原始图片长宽和需要的长宽计算采样比例，必须是2的倍数，
            //  IMAGE_WIDTH_DEFAULT=768， IMAGE_HEIGHT_DEFAULT=1024
            int needWidth = (int) (needHeight * 1.0 / o.outHeight * o.outWidth);
            o2.inSampleSize = SAMPLE_SIZE;
            // 每像素采用RGB_565的格式保存
            o2.inPreferredConfig = Bitmap.Config.RGB_565;
            // 根据压缩参数的设置进行第二次解码
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(new File(filePath)), null, o2);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, needWidth, needHeight, true);

            // b.recycle();
            // b.recycle will cause prev Bitmap.createScaledBitmap null pointer exception on b occasionally
            System.gc();

            return scaledBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取采样比例
    public static int getImageScale(int outWidth, int outHeight, int needWidth, int needHeight) {
        int scale = 1;
        if (outHeight > needHeight || outWidth > needWidth) {
            int maxSize = needHeight > needWidth ? needHeight : needWidth;
            scale = (int) Math.pow(2, (int) Math.round(Math.log(maxSize /(double) Math.max(outHeight, outWidth)) / Math.log(0.5)));
        }

        return scale;
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
