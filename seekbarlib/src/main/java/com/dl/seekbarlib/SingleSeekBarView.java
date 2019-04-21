package com.dl.seekbarlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.List;

/**
 * 单选选择seekbar
 */
public class SingleSeekBarView extends View {
    private static final float SEEK_BG_SCALE = 0.8F / 2;//设置线位置在总高度的比例
    private static final float SEEK_TEXT_SCALE = 3.2F / 3.5F;//设置文字位置在总高度的比例
    private static final int DEF_HEIGHT = 70; // 总高度
    private static final int DEF_PADDING = 20;//左右padding
    private static final int BG_HEIGHT = 10;
    private static final int SEEK_STROKE_SIZE = 2;
    private static final String TAG = SingleSeekBarView.class.getSimpleName();

    private int viewWidth;
    private int viewHeight;
    private int seekBgColor;
    private int seekPbColor;
    private int seekBallSolidColor;
    private int seekBallStrokeColor;
    private int seekTextColor;
    private int seekTextSize;
    private boolean isShowVerticalLine;

    private Paint seekBgPaint;
    private Paint seekLinePaint;
    private Paint seekBallPaint;
    private Paint seekBallStrokePaint;
    private Paint seekPbPaint;
    private Paint seekTextPaint;
    private RectF seekBGRectF;
    private RectF seekPbRectF;

    private int seekBallRadio;
    private int seekBallY;
    private int seekBallX;

    private List<String> data;
    private int seekTextY;
    private int currentMovingType;
    private OnDragFinishedListener dragFinishedListener;
    private OnLayoutLoadCompleteListener onLayoutLoadCompleteListener;
    private int position;
    private int downX;
    private Context mContext;
    private boolean isFirst = true;

    public SingleSeekBarView(Context context) {
        this(context, null);
    }

    public SingleSeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RangeSeekBarView, defStyleAttr, R.style.RangeSeekBarViewStyle);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.RangeSeekBarView_seek_bg_color) {
                seekBgColor = typedArray.getColor(attr, Color.parseColor("#D9D9D9"));

            } else if (attr == R.styleable.RangeSeekBarView_seek_pb_color) {
                seekPbColor = typedArray.getColor(attr, Color.parseColor("#FED854"));

            } else if (attr == R.styleable.RangeSeekBarView_seek_ball_solid_color) {
                seekBallSolidColor = typedArray.getColor(attr, Color.parseColor("#FFFFFF"));

            } else if (attr == R.styleable.RangeSeekBarView_seek_ball_stroke_color) {
                seekBallStrokeColor = typedArray.getColor(attr, Color.parseColor("#FED854"));

            } else if (attr == R.styleable.RangeSeekBarView_seek_text_color) {
                seekTextColor = typedArray.getColor(attr, Color.parseColor("#959595"));

            } else if (attr == R.styleable.RangeSeekBarView_seek_text_size) {
                seekTextSize = typedArray.getDimensionPixelSize(attr, 14);

            } else if (attr == R.styleable.RangeSeekBarView_seek_vertical_line) {
                isShowVerticalLine = typedArray.getBoolean(attr, false);
            }
        }
        typedArray.recycle();
        init();
    }

    private void init() {
        seekTextPaint = creatPaint(seekTextColor, seekTextSize, Paint.Style.FILL, 5);
        seekBgPaint = creatPaint(seekBgColor, 0, Paint.Style.FILL, 0);
        seekLinePaint = creatPaint(seekBgColor, 0, Paint.Style.FILL, 8);
        seekBallPaint = creatPaint(seekBallSolidColor, 0, Paint.Style.FILL, 0);
        seekPbPaint = creatPaint(seekPbColor, 0, Paint.Style.FILL, 0);
        seekBallStrokePaint = creatPaint(seekBallStrokeColor, 0, Paint.Style.FILL, 0);
        seekBallStrokePaint.setShadowLayer(5, 2, 2, seekBallStrokeColor);

        //view加载完成回调
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (null != onLayoutLoadCompleteListener && isFirst) {
                    onLayoutLoadCompleteListener.loadComplete();
                    isFirst = false;
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
        viewWidth = w;
        seekBallRadio = dp2px(13);
        seekBallY = (int) (viewHeight * SEEK_BG_SCALE + BG_HEIGHT / 2.F);
        seekTextY = (int) (viewHeight * SEEK_TEXT_SCALE);
        seekBallX = seekBallRadio + DEF_PADDING;

        seekBGRectF = new RectF(seekBallRadio + DEF_PADDING, viewHeight * SEEK_BG_SCALE, viewWidth - seekBallRadio - DEF_PADDING, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        seekPbRectF = new RectF(seekBallRadio + DEF_PADDING, viewHeight * SEEK_BG_SCALE, seekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureHeight;
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            measureHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEF_HEIGHT, getContext().getResources().getDisplayMetrics());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTexts(canvas);
        drawVerticalLine(canvas);
        drawSeekBG(canvas);
        drawSeekPB(canvas);
        drawCircle(canvas);
    }


    /**
     * 绘制文字
     *
     * @param canvas
     */
    private void drawTexts(Canvas canvas) {
        if (null == data) return;
        int size = data.size();
        int unitWidth = getUnitWidth(size - 1);
        for (int i = 0; i < size; i++) {
            String tempDesc = data.get(i);
            float measureTextWidth = seekTextPaint.measureText(tempDesc);
            if ((DEF_PADDING + unitWidth * i + measureTextWidth) > viewWidth - measureTextWidth) {
                canvas.drawText(tempDesc, viewWidth - measureTextWidth - DEF_PADDING, seekTextY, seekTextPaint);
            } else {
                canvas.drawText(tempDesc, DEF_PADDING + unitWidth * i + seekBallRadio - measureTextWidth / 2, seekTextY, seekTextPaint);
            }
        }
    }

    /**
     * 绘制竖线
     *
     * @param canvas
     */
    private void drawVerticalLine(Canvas canvas) {
        if (null == data) return;
        if (!isShowVerticalLine) return;
        int size = data.size();
        int unitWidth = getUnitWidth(size - 1);
        for (int i = 0; i < size; i++) {
            //设置左面小球的位置
            int x = (int) (DEF_PADDING + seekBallRadio + unitWidth * i - seekLinePaint.getStrokeWidth() / 2);
            canvas.drawLine(x, seekBallY - 10, x, seekBallY + 10, seekLinePaint);
        }
    }


    /**
     * 设置左面球的位置 -必须在view加载完成在设置有效
     *
     * @param pos
     */
    public void setSeekBallPos(int pos) {
        if (data == null) return;
        if (pos < 0) pos = 0;
        if (pos > data.size() - 1) pos = data.size() - 1;
        int size = data.size();
        int unitWidth = getUnitWidth(size - 1);
        //设置左面小球的位置
        seekBallX = DEF_PADDING + seekBallRadio + unitWidth * pos;
        //设置背景线的样式
        seekPbRectF = new RectF(seekBallRadio + DEF_PADDING, viewHeight * SEEK_BG_SCALE, seekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                seekBallX = downX;
                seekPbRectF = new RectF(seekBallRadio + DEF_PADDING, viewHeight * SEEK_BG_SCALE, seekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);

                break;
            case MotionEvent.ACTION_MOVE:
                //移动的时候根据计算出来的位置以及方向改变两个小球的位置以及举行进度条的RectF的范围
                int moveX = (int) event.getX();
                seekBallX = moveX;
                seekPbRectF = new RectF(seekBallRadio + DEF_PADDING, viewHeight * SEEK_BG_SCALE, seekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //手指离开的时候,确定返回给UI的数据集 回弹到制定分割位置
                seekBallX = getCurrentSeekX((int) event.getX()) + DEF_PADDING + seekBallRadio;
                break;
        }

        // 边界处理
        if (seekBallX < seekBallRadio + DEF_PADDING) {
            seekBallX = seekBallRadio + DEF_PADDING;
        }
        if (seekBallX > viewWidth - seekBallRadio - DEF_PADDING) {
            seekBallX = viewWidth - seekBallRadio - DEF_PADDING;
        }
        seekPbRectF = new RectF(seekBallRadio + DEF_PADDING, viewHeight * SEEK_BG_SCALE, seekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        position = getDataPosition(seekBallX);
        if (null != dragFinishedListener) {
            dragFinishedListener.dragFinished(position);
        }
        postInvalidate();
        return true;
    }

    /**
     * 两球之间线
     *
     * @param canvas
     */
    private void drawSeekPB(Canvas canvas) {
        canvas.drawRect(seekPbRectF, seekPbPaint);
    }


    /**
     * 绘制左面的圆圈
     *
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(seekBallX, seekBallY, seekBallRadio, seekBallStrokePaint);
        canvas.drawCircle(seekBallX, seekBallY, seekBallRadio - SEEK_STROKE_SIZE, seekBallPaint);
    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    private void drawSeekBG(Canvas canvas) {
        canvas.drawRoundRect(seekBGRectF, BG_HEIGHT / 2, BG_HEIGHT / 2, seekBgPaint);
    }


    /**
     * 公共创建paint画笔
     *
     * @param paintColor
     * @param textSize
     * @param style
     * @param lineWidth
     * @return
     */
    private Paint creatPaint(int paintColor, int textSize, Paint.Style style, int lineWidth) {
        Paint paint = new Paint();
        paint.setColor(paintColor);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineWidth);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));//字体加粗
        paint.setDither(true);
        paint.setTextSize(textSize);
        paint.setStyle(style);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        return paint;
    }

    private int getUnitWidth(int count) {
        return (viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio) / count;
    }

    private int getCurrentSeekX(int upX) {
        if (null == data) {
            return 0;
        }
        int unitWidth = getUnitWidth(data.size() - 1);
        return unitWidth * (upX / unitWidth);
    }

    private int getDataPosition(int upX) {
        if (null == data) {
            return 0;
        }
        int unitWidth = getUnitWidth(data.size() - 1);
        return upX / unitWidth;
    }

    /**
     * 设置范围数据
     *
     * @param data
     * @return
     */
    public SingleSeekBarView setRangeData(List<String> data) {
        this.data = data;
        position = 0;
        if (null != data && data.size() != 0) {
            position = data.size() - 1;
        }
        return this;
    }

    /**
     * 设置拖动回调
     *
     * @param dragFinishedListener
     * @return
     */
    public SingleSeekBarView setOnDragFinishedListener(OnDragFinishedListener dragFinishedListener) {
        this.dragFinishedListener = dragFinishedListener;
        return this;
    }

    /**
     * 设置布局加载完成回调
     *
     * @param onLayoutLoadCompleteListener
     * @return
     */
    public SingleSeekBarView setOnLayoutLoadCompleteListener(OnLayoutLoadCompleteListener onLayoutLoadCompleteListener) {
        this.onLayoutLoadCompleteListener = onLayoutLoadCompleteListener;
        return this;
    }


    public int dp2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * 拖动接口
     */
    public interface OnDragFinishedListener {
        void dragFinished(int position);
    }

    /**
     * 布局加载完成接口
     */
    public interface OnLayoutLoadCompleteListener {
        void loadComplete();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        invalidate();
    }

}
