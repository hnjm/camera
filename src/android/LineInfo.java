package com.chinamobile.gdwy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dliang.wang on 2017/4/12.
 */

public class LineInfo {
    private List<PointInfo> pointList;
    private LineType lineType;
    public String color;
    public String text = "";
    public float fontSize;
    public float lineWidth;
    public float percent;

    // 0---线条；1---圆；2---矩形；3---箭头；4---文字; 5---马赛克
    public enum LineType {
        NormalLine,
        CircleLine,
        RingLine,
        ArrowLine,
        TextLine,
        MosaicLine,
    }

    public LineInfo(LineType type) {
        pointList = new ArrayList<>();
        lineType = type;
    }

    public void addPoint(PointInfo point) {
        pointList.add(point);
    }

    public List<PointInfo> getPointList() {
        return pointList;
    }

    public LineType getLineType() {
        return lineType;
    }
}
