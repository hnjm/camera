package com.chinamobile.gdwy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liangzhongtai on 2020/2/12.
 */

public class PaintableImageView extends android.support.v7.widget.AppCompatImageView {
    // 线条列表
    private List<LineInfo> lineList;
    // 当前线条
    private LineInfo currentLine;
    // 当前位置
    private int currentIndex;
    // 当前线条类型
    private LineInfo.LineType currentLineType = LineInfo.LineType.NormalLine;

    // private Paint normalPaint = new Paint();
    private static final float NORMAL_LINE_STROKE = 5.0f;

    private Paint mosaicPaint = new Paint();
    // 马赛克每个大小40*40像素，共三行
    private static final int MOSAIC_CELL_LENGTH = 30;

    private Drawable drawable;
    private Bitmap bitmap;
    // 马赛克绘制中用于记录某个马赛克格子的数值是否计算过
    private boolean mosaics[][];
    // 马赛克行数
    private int mosaicRows;
    // 马赛克列数
    private int mosaicColumns;

    private String color = "#FFFFFF";
    private float textSize = 18;
    private float lineWidth = 10;
    private String text = "";

    {
        lineList = new ArrayList<>();
    }

    public PaintableImageView(Context context) {
        super(context);
    }

    public PaintableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置线条类型
     * @param type
     */
    public void setLineType(LineInfo.LineType type) {
        currentLineType = type;
    }

    public void setColor(String color) {
        this.color = color;
        if (lineList.size() > 0) {
            lineList.get(currentIndex).color = color;
            invalidate();
        }
    }

    public void setTextSize(float percent) {
        if (currentLineType == LineInfo.LineType.TextLine) {
            textSize = 18;
            textSize = textSize * (1 + (percent - 50) / 20);
            textSize  = textSize < 2 ? 2 : textSize;
            if (lineList.size() > 0) {
                lineList.get(currentIndex).fontSize = textSize;
                lineList.get(currentIndex).percent = percent;
                invalidate();
            }
        } else {
            lineWidth = 10;
            lineWidth = lineWidth * (1 + (percent - 50) / 20);
            lineWidth = lineWidth < 1 ? 1 : lineWidth;
            if (lineList.size() > 0) {
                lineList.get(currentIndex).lineWidth = lineWidth;
                lineList.get(currentIndex).percent = percent;
                invalidate();
            }
        }
    }


    public void setText(String text) {
        this.text = text;
        if (lineList.size() > 0 &&
            lineList.get(currentIndex).getLineType() == LineInfo.LineType.TextLine) {
            lineList.get(currentIndex).text = text;
            invalidate();
        }
    }

    public void setPreStep() {
        if (lineList.size() > 0) {
            int length = lineList.size();
            currentIndex--;
            currentIndex = currentIndex < 0 ? Math.abs(currentIndex % length) : currentIndex;
            invalidate();
        }
    }

    public void setLastStep() {
        if (lineList.size() > 0) {
            int length = lineList.size();
            currentIndex++;
            currentIndex = currentIndex > lineList.size() - 1 ?  Math.abs(currentIndex % length) : currentIndex;
            invalidate();
        }
    }

    public String  saveBitmap(String fileName) {
        currentIndex = -1;
        invalidate();
        //创建Bitmap,最后一个参数代表图片的质量.
        Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        //创建Canvas，并传入Bitmap.
        Canvas canvas = new Canvas(bitmap);
        //View把内容绘制到canvas上，同时保存在bitmap.
        this.draw(canvas);
        CameraUtil.saveBitmap(bitmap, 100,
                CameraUtil.getPhotoPath(), fileName);
        return CameraUtil.getPhotoPath() + "/" + fileName;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentLine = new LineInfo(currentLineType);
                currentLine.addPoint(new PointInfo(xPos, yPos));
                currentLine.color = color;
                currentLine.fontSize = textSize;
                currentLine.lineWidth = lineWidth;
                currentLine.text = text;
                lineList.add(currentLine);
                currentIndex = lineList.size() - 1;
                invalidate();
                // return true消费掉ACTION_DOWN事件，否则不会触发ACTION_UP
                return true;
            case MotionEvent.ACTION_MOVE:
                currentLine.addPoint(new PointInfo(xPos, yPos));
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                currentLine.addPoint(new PointInfo(xPos, yPos));
                invalidate();
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mosaicRows; i++) {
            for (int j = 0; j < mosaicColumns; j++) {
                mosaics[i][j] = false;
            }
        }
        try {
            for (int i = 0; i < lineList.size(); i++) {
                LineInfo lineinfo = lineList.get(i);
                if (lineinfo.getLineType() == LineInfo.LineType.NormalLine) {
                    drawNormalLine(canvas, lineinfo, i);
                } else if (lineinfo.getLineType() == LineInfo.LineType.RingLine) {
                    drawRingLine(canvas, lineinfo, i);
                } else if (lineinfo.getLineType() == LineInfo.LineType.ArrowLine) {
                    drawArrowLine(canvas, lineinfo, i);
                } else if (lineinfo.getLineType() == LineInfo.LineType.TextLine) {
                    drawTextLine(canvas, lineinfo, i);
                } else if (lineinfo.getLineType() == LineInfo.LineType.MosaicLine) {
                    drawMosaicLine(canvas, lineinfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制普通线条
     * @param canvas
     * @param lineinfo
     */
    private void drawNormalLine(Canvas canvas, LineInfo lineinfo, int index) {
        if (lineinfo.getPointList().size() <= 1) {
            return;
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineinfo.lineWidth);
        paint.setColor(Color.parseColor(lineinfo.color));
        if (currentIndex == index) {
            paint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        }
        for (int i = 0; i < lineinfo.getPointList().size() - 1; i++) {
            PointInfo startPoint  = lineinfo.getPointList().get(i);
            PointInfo endPoint  = lineinfo.getPointList().get(i + 1);
            canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint);
        }
    }

    /**
     * 绘制矩形
     * */
    private void drawRingLine(Canvas canvas, LineInfo lineinfo, int index) {
        int length = lineinfo.getPointList().size();
        if (length <= 1) {
            return;
        }
        PointInfo startPoint  = lineinfo.getPointList().get(0);
        PointInfo endPoint  = lineinfo.getPointList().get(length - 1);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineinfo.lineWidth);
        paint.setColor(Color.parseColor(lineinfo.color));
        if (currentIndex == index) {
            paint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        }
        canvas.drawRect(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint);
    }

    /**
     * 绘制箭头
     * */
    private void drawArrowLine(Canvas canvas, LineInfo lineinfo, int index) {
        int length = lineinfo.getPointList().size();
        if (length <= 1) {
            return;
        }
        PointInfo startPoint  = lineinfo.getPointList().get(0);
        PointInfo endPoint  = lineinfo.getPointList().get(length - 1);
        Paint normalPaint = new Paint();
        normalPaint.setStyle(Paint.Style.STROKE);
        normalPaint.setAntiAlias(true);
        normalPaint.setStrokeWidth(lineinfo.lineWidth);
        normalPaint.setColor(Color.parseColor(lineinfo.color));
        if (currentIndex == index) {
            normalPaint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
        }
        drawArrow(startPoint.x, startPoint.y, endPoint.x, endPoint.y, canvas, normalPaint, index);
    }
    public void drawArrow(float startX, float startY, float endX, float endY, Canvas canvas,
                          Paint paint, int index) {
        double H = 10;  //箭头高度
        double L = 6;  //底边的一半
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H);  //箭头角度
        double arraow_len = Math.sqrt(L * L + H * H);  //箭头的长度
        double[] arrXY_1 = rotateVec(endX - startX, endY - startY, awrad, true, arraow_len, index);
        double[] arrXY_2 = rotateVec(endX - startX, endY - startY, -awrad, true, arraow_len, index);
        double x_3 = endX - arrXY_1[0];  //(x3,y3)是第一端点
        double y_3 = endY - arrXY_1[1];
        double x_4 = endX - arrXY_2[0];  //(x4,y4)是第二端点
        double y_4 = endY - arrXY_2[1];
        Double X3 = new Double(x_3);
        x3 = X3.intValue();
        Double Y3 = new Double(y_3);
        y3 = Y3.intValue();
        Double X4 = new Double(x_4);
        x4 = X4.intValue();
        Double Y4 = new Double(y_4);
        y4 = Y4.intValue();
        //画线
        canvas.drawLine(startX, startY, endX, endY, paint);
        Path triangle = new Path();
        triangle.moveTo(endX, endY);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.close();
        canvas.drawPath(triangle, paint);
    }
    public double[] rotateVec(float px, float py, double ang, boolean isChLen, double newLen, int index) {
        double mathstr[] = new double[2];
        //矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }


    /**
     * 绘制文本
     * */
    private void drawTextLine(Canvas canvas, LineInfo lineinfo, int index) {
        if (lineinfo.getPointList().size() <= 1) {
            return;
        }
        PointInfo startPoint  = lineinfo.getPointList().get(0);
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.parseColor(lineinfo.color));
        textPaint.setTextSize(lineinfo.fontSize * getResources().getDisplayMetrics().density);
        textPaint.setAntiAlias(true);
        String text = lineinfo.text;
        if (currentIndex == index) {
            textPaint.setFlags(Paint. UNDERLINE_TEXT_FLAG);
        }
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length() - 1, rect);
        float heightLine = rect.height();
        // 计算单行文本宽高
        float widthLine = getWidth() - startPoint.x - 20 * getResources().getDisplayMetrics().density;
        List<String> texts = new ArrayList<>();
        String textLine = "";
        char[] textArr = text.toCharArray();
        Log.d(Camera.TAG, "textarr=" + Arrays.toString(textArr));
        boolean hasNext = false;
        for (int i = 0; i < textArr.length; i++) {
            String textNext = textLine + String.valueOf(textArr[i]);
            textPaint.getTextBounds(textNext, 0, textNext.length() - 1, rect);
            if (rect.width() > widthLine) {
                hasNext = false;
                texts.add(textLine);
                textLine = "";
            } else {
                hasNext = true;
                textLine = textNext;
            }
        }
        if (hasNext) {
            texts.add(textLine);
        }
        for (int i = 0; i < texts.size(); i++) {
            canvas.drawText(texts.get(i), startPoint.x, startPoint.y + heightLine * i, textPaint);
        }
    }

    /**
     * 绘制马赛克线条
     * @param canvas
     * @param lineinfo
     */
    private void drawMosaicLine(Canvas canvas, LineInfo lineinfo) {
        if (null == bitmap) {
            init();
        }

        if (null == bitmap) {
            return;
        }

        for (PointInfo pointInfo : lineinfo.getPointList()) {
            // 对每一个点，填充所在的小格子以及上下两个格子（如果有上下格子）
            int currentRow = (int) ((pointInfo.y -1) / MOSAIC_CELL_LENGTH);
            int currentCol = (int) ((pointInfo.x -1) / MOSAIC_CELL_LENGTH);

            fillMosaicCell(canvas, currentRow, currentCol);
            if (lineinfo.percent <= 25) {
            }
            if (lineinfo.percent > 25) {
                fillMosaicCell(canvas, currentRow - 1, currentCol);
                fillMosaicCell(canvas, currentRow + 1, currentCol);
            }
            if (lineinfo.percent > 50) {
                fillMosaicCell(canvas, currentRow - 2, currentCol);
                fillMosaicCell(canvas, currentRow + 2, currentCol);
            }
            if (lineinfo.percent > 75) {
                fillMosaicCell(canvas, currentRow - 3, currentCol);
                fillMosaicCell(canvas, currentRow + 3, currentCol);
            }
        }
    }

    /**
     * 填充一个马赛克格子
     * @param cavas
     * @param row 马赛克格子行
     * @param col 马赛克格子列
     */
    private void fillMosaicCell(Canvas cavas, int row, int col) {
        if (row >= 0 && row < mosaicRows && col >= 0 && col < mosaicColumns) {
            if (!mosaics[row][col]) {
                mosaicPaint.setColor(bitmap.getPixel(col * MOSAIC_CELL_LENGTH, row * MOSAIC_CELL_LENGTH));

                cavas.drawRect(col * MOSAIC_CELL_LENGTH, row * MOSAIC_CELL_LENGTH, (col + 1) * MOSAIC_CELL_LENGTH, (row + 1) * MOSAIC_CELL_LENGTH, mosaicPaint);
                mosaics[row][col] = true;
            }
        }
    }

    /**
     * 初始化马赛克绘制相关
     */
    private void init() {
        drawable = getDrawable();

        try {
            bitmap = ((BitmapDrawable)drawable).getBitmap();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return;
        }

        mosaicColumns = (int)Math.ceil(bitmap.getWidth() / MOSAIC_CELL_LENGTH);
        mosaicRows = (int)Math.ceil(bitmap.getHeight() / MOSAIC_CELL_LENGTH);
        mosaics = new boolean[mosaicRows][mosaicColumns];
    }

    /**
     * 删除选中的线
     */
    public void cancelDrawLastLine() {
        if (canStillWithdraw()) {
            lineList.remove(currentIndex);
            currentIndex--;
            currentIndex = currentIndex < 0 ? 0 : currentIndex;
            invalidate();
        }
    }

    /**
     * 判断是否可以继续撤销
     * @return
     */
    public boolean canStillWithdraw() {
        return lineList.size() > 0;
    }
}