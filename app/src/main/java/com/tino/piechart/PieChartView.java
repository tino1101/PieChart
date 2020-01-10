package com.tino.piechart;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    private int textAlpha;
    private float cell = getResources().getDimension(R.dimen.pie_space);
    private float innerRadius = 0.6f;
    private float offRadius = 0, offLine;
    private int backGroundColor = 0xffffffff;
    private float topItemTextSize = getResources().getDimension(R.dimen.top_text_size);
    private float bottomItemTextSize = getResources().getDimension(R.dimen.bottom_text_size);
    private float textPadding = getResources().getDimension(R.dimen.text_padding);
    private int defaultStartAngle = -90;
    private long animDuration = 800;
    private float startPointDistance = getResources().getDimension(R.dimen.start_point_distance);
    private boolean drawStartPoint = true;
    private float brokenLineLength = getResources().getDimension(R.dimen.broken_line_length);
    private int topTextColor = Color.parseColor("#171717");
    private int bottomTextColor = Color.parseColor("#999999");
    private float startPointAlpha = 0.2f;
    private float startPointStrokeWidth = getResources().getDimension(R.dimen.start_point_stroke_width);
    private float startPointAlphaStrokeWidth = getResources().getDimension(R.dimen.start_point_alpha_stroke_width);
    private float lineStrokeWidth = getResources().getDimension(R.dimen.line_stroke_width);
    private int width, height;
    private int radius;
    private Paint mPaint;
    private Path mPath, drawLinePath = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private Canvas mCanvas;
    private RectF pieRectF;
    private Point firstPoint;
    private Point startPoint = new Point();
    private Point centerPoint = new Point();
    private Point endPoint = new Point();
    private Point tempPoint = new Point();
    private List<Item> itemTypeList, leftTypeList, rightTypeList;
    private List<Point> itemPoints;
    private ValueAnimator animator;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPath = new Path();
        pieRectF = new RectF();
        itemTypeList = new ArrayList<>();
        leftTypeList = new ArrayList<>();
        rightTypeList = new ArrayList<>();
        itemPoints = new ArrayList<>();
    }

    private float drawTitle(boolean isRight, List<Item> list, float originRadius) {
        float startRadius = originRadius;
        int count = list.size();
        for (int i = 0; i < count; i++) {
            mPath.reset();
            Item item = list.get(i);
            if (!item.showLine) continue;

            double angle = 2 * Math.PI * ((startRadius + item.radius / 2) / 360d);
            int x = (int) (width / 2 + (radius + startPointDistance) * Math.cos(angle));
            int y = (int) (height / 2 + (radius + startPointDistance) * Math.sin(angle));
            startPoint.set(x, y);
            centerPoint.set((int) (width / 2 + (radius + startPointDistance + brokenLineLength) * Math.cos(angle)), (int) (height / 2 + (radius + startPointDistance + brokenLineLength) * Math.sin(angle)));
            if (isRight) {
                endPoint.set((int) (width * 0.95f), centerPoint.y);
            } else {
                endPoint.set((int) (width * 0.05f), centerPoint.y);
            }
            if (drawStartPoint) {
                mPaint.setStrokeWidth(startPointAlphaStrokeWidth);
                mPaint.setColor(item.color);
                mPaint.setAlpha((int) (255 * startPointAlpha));
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mCanvas.drawPoint(startPoint.x, startPoint.y, mPaint);
                mPaint.setStrokeWidth(startPointStrokeWidth);
                mPaint.setAlpha(255);
                mCanvas.drawPoint(startPoint.x, startPoint.y, mPaint);
            }
            mPath.moveTo(startPoint.x, startPoint.y);
            mPath.lineTo(centerPoint.x, centerPoint.y);
            mPath.lineTo(endPoint.x, endPoint.y);
            resetPaint();
            mPaint.setStrokeWidth(lineStrokeWidth);
            mPaint.setColor(item.color);
            mPaint.setStyle(Paint.Style.STROKE);
            mPathMeasure.setPath(mPath, false);
            drawLinePath.reset();
            mPathMeasure.getSegment(0, mPathMeasure.getLength() * offLine, drawLinePath, true);
            mCanvas.drawPath(drawLinePath, mPaint);

            startRadius += item.radius;

            if (textAlpha > 0) {
                mPaint.setTextSize(topItemTextSize);
                mPaint.setStyle(Paint.Style.FILL);
                if (isRight) mPaint.setTextAlign(Paint.Align.RIGHT);
                mPaint.setAlpha(textAlpha);
                mPaint.setColor(topTextColor);
                mCanvas.drawText("¥" + item.count, endPoint.x,
                        centerPoint.y - textPadding, mPaint);
                mPaint.setTextSize(bottomItemTextSize);
                mPaint.setColor(bottomTextColor);
                mCanvas.drawText(item.label, endPoint.x, centerPoint.y + (bottomItemTextSize + textPadding) * 4 / 5, mPaint);
            }

        }
        return startRadius;
    }

    private void drawPie() {
        if (mCanvas == null) {
            return;
        }
        mCanvas.drawColor(backGroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        float sum = 0;
        for (Item item : itemTypeList) {
            sum += item.weight;
        }
        float a = 360f / sum;
        float startRadius = defaultStartAngle;
        float sumRadius = 0;
        leftTypeList.clear();
        rightTypeList.clear();
        itemPoints.clear();
        for (Item item : itemTypeList) {
            item.radius = item.weight * a;
            double al = 2 * Math.PI * ((startRadius + 90) / 360d);
            tempPoint.set((int) (width / 2 + radius * Math.sin(al)), (int) (height / 2 - radius * Math.cos(al)));
            if (cell > 0 && startRadius == defaultStartAngle) {
                firstPoint = tempPoint;
            }

            double angle = 2 * Math.PI * ((startRadius + item.radius / 2) / 360d);
            double cos = -Math.cos(angle);
            if (cos > 0) {
                leftTypeList.add(item);
            } else {
                rightTypeList.add(item);
            }
            sumRadius += Math.abs(item.radius);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(item.color);
            if (sumRadius <= offRadius) {
                mCanvas.drawArc(pieRectF, startRadius, item.radius, true, mPaint);
            } else {
                mCanvas.drawArc(pieRectF, startRadius, item.radius - (Math.abs(offRadius - sumRadius)), true, mPaint);
                break;
            }
            startRadius += item.radius;
            if (cell > 0) {
                mPaint.setColor(backGroundColor);
                mPaint.setStrokeWidth(cell);
                mCanvas.drawLine(getWidth() / 2, getHeight() / 2, tempPoint.x, tempPoint.y, mPaint);
            }
        }
        if (cell > 0 && firstPoint != null) {
            mPaint.setColor(backGroundColor);
            mPaint.setStrokeWidth(cell);
            mCanvas.drawLine(getWidth() / 2, getHeight() / 2, firstPoint.x, firstPoint.y, mPaint);
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(backGroundColor);
        if (innerRadius > 0) {
            mCanvas.drawCircle(width / 2, height / 2, radius * innerRadius, mPaint);
        }
    }

    public void resetPaint() {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setAlpha(255);
    }

    private void startAnim() {
        animator = ValueAnimator.ofFloat(0, 360f * 2);
        animator.setDuration(animDuration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (value < 360f) {
                    offRadius = value;
                    offLine = 0;
                    textAlpha = 0;
                } else if (value >= 360f) {
                    offRadius = 360f;
                    offLine = (value - 360f) / 360f;
                    if (offLine > 0.5f) {
                        textAlpha = (int) (255 * ((offLine - 0.5f) / 0.5f));
                    } else {
                        textAlpha = 0;
                    }
                } else if (value == 360f * 2) {
                    offRadius = 360f;
                    offLine = 1.0f;
                    textAlpha = 255;
                }
                postInvalidate();
            }
        });
        animator.start();
    }

    public void setCell(float cell) {
        this.cell = cell;
    }

    public void setBackGroundColor(int backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    public void setTopItemTextSize(float itemTextSize) {
        this.topItemTextSize = itemTextSize;
    }

    public void setBottomItemTextSize(float itemTextSize) {
        this.bottomItemTextSize = itemTextSize;
    }

    public void setTextPadding(float textPadding) {
        this.textPadding = textPadding;
    }

    public void setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
    }

    public void setStartPointDistance(float startPointDistance) {
        this.startPointDistance = startPointDistance;
    }

    public void setDrawStartPoint(boolean drawStartPoint) {
        this.drawStartPoint = drawStartPoint;
    }

    public void setBrokenLineLength(float brokenLineLength) {
        this.brokenLineLength = brokenLineLength;
    }

    public void setStartPointAlpha(float startPointAlpha) {
        this.startPointAlpha = startPointAlpha;
    }

    public void setStartPointStrokeWidth(float startPointStrokeWidth) {
        this.startPointStrokeWidth = startPointStrokeWidth;
    }

    public void setStartPointAlphaStrokeWidth(float startPointAlphaStrokeWidth) {
        this.startPointAlphaStrokeWidth = startPointAlphaStrokeWidth;
    }

    public void setLineStrokeWidth(float lineStrokeWidth) {
        this.lineStrokeWidth = lineStrokeWidth;
    }

    public void setInnerRadius(float innerRadius) {
        if (innerRadius > 1.0f) {
            innerRadius = 1.0f;
        } else if (innerRadius < 0) {
            innerRadius = 0;
        }
        this.innerRadius = innerRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            mCanvas = canvas;
            drawPie();
            if (offRadius == 360f) {
                resetPaint();
                drawTitle(false, leftTypeList, drawTitle(true, rightTypeList, defaultStartAngle));
                if (textAlpha == 1f) {
                    itemTypeList.clear();
                    leftTypeList.clear();
                    rightTypeList.clear();
                    itemPoints.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        this.width = w;
        this.height = h;
        radius = Math.min(width, height) / 5;
        pieRectF.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnim();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }

    public void addItem(Item item) {
        if (itemTypeList != null) {
            itemTypeList.add(item);
        }
    }

    public static class Item {
        String label;
        float weight;
        int color;
        float radius;
        int count;
        boolean showLine;

        public Item(String label, float weight, int color, int count) {
            this.label = label;
            this.weight = weight;
            this.color = color;
            this.count = count;
        }

        public Item(String label, float weight, int color, int count, boolean showLine) {
            this(label, weight, color, count);
            this.showLine = showLine;
        }
    }
}