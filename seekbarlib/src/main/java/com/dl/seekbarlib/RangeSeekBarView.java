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
 * 范围选择seekbar
 */
public class RangeSeekBarView extends View {
    private static final float SEEK_BG_SCALE = 0.8F / 2;//设置线位置在总高度的比例
    private static final float SEEK_TEXT_SCALE = 3.2F / 3.5F;//设置文字位置在总高度的比例
    private static final int DEF_HEIGHT = 70; // 总高度
    private static final int DEF_PADDING = 20;//左右padding
    private static final int BG_HEIGHT = 10;
    private static final int SEEK_STROKE_SIZE = 2;
    private static final String TAG = RangeSeekBarView.class.getSimpleName();

    //配置信息
    private int viewWidth;
    private int viewHeight;
    private int seekBgColor;
    private int seekPbColor;
    private int seekBallSolidColor;
    private int seekBallStrokeColor;
    private int seekTextColor;
    private int seekTextSize;
    private boolean isShowVerticalLine;
    private boolean isStepMove;

    //所有画笔
    private Paint seekBgPaint;
    private Paint seekLinePaint;
    private Paint seekBallPaint;
    private Paint seekBallEndPaint;
    private Paint seekBallStrokePaint;
    private Paint seekPbPaint;
    private Paint seekTextPaint;
    private RectF seekBGRectF;
    private RectF seekPbRectF;

    //左右球的位置及大小信息
    private int seekBallRadio;
    private int seekBallY;
    private int leftSeekBallX;
    private int rightSeekBallX;

    private List<String> data;
    private int seekTextY;
    private int currentMovingType;
    private OnDragFinishedListener dragFinishedListener;
    private OnLayoutLoadCompleteListener onLayoutLoadCompleteListener;
    private int leftPosition, rightPosition;
    private int downX;
    private Context mContext;
    private boolean isFirst = true;
    //单选模式
    public final static int SEEKBAR_MODE_SINGLE = 1;
    //范围模式
    public final static int SEEKBAR_MODE_RANGE = 2;
    //模式 默认范围模式
    private int seekBarMode = SEEKBAR_MODE_RANGE;
    //最大值
    private int maxValue;

    private float stepLenght = 1;

    public RangeSeekBarView(Context context) {
        this(context, null);
    }

    public RangeSeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
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

            } else if (attr == R.styleable.RangeSeekBarView_seek_step_move) {
                isStepMove = typedArray.getBoolean(attr, false);

            } else if (attr == R.styleable.RangeSeekBarView_seek_mode) {
                seekBarMode = typedArray.getInt(attr, SEEKBAR_MODE_RANGE);

            } else if (attr == R.styleable.RangeSeekBarView_seek_step_length) {
                stepLenght = typedArray.getFloat(attr, 1.0f);
            }
        }
        typedArray.recycle();
        init();
    }

    private void init() {
        currentMovingType = BallType.LEFT;
        seekTextPaint = creatPaint(seekTextColor, seekTextSize, Paint.Style.FILL, 5);
        seekBgPaint = creatPaint(seekBgColor, 0, Paint.Style.FILL, 0);
        seekLinePaint = creatPaint(seekBgColor, 0, Paint.Style.FILL, 8);
        seekBallPaint = creatPaint(seekBallSolidColor, 0, Paint.Style.FILL, 0);
        seekPbPaint = creatPaint(seekPbColor, 0, Paint.Style.FILL, 0);
        seekBallEndPaint = creatPaint(seekBallSolidColor, 0, Paint.Style.FILL, 0);
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
        leftSeekBallX = seekBallRadio + DEF_PADDING;
        rightSeekBallX = viewWidth - seekBallRadio - DEF_PADDING;

        seekBGRectF = new RectF(seekBallRadio + DEF_PADDING, viewHeight * SEEK_BG_SCALE, viewWidth - seekBallRadio - DEF_PADDING, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);

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
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            drawLeftCircle(canvas);
        }
        drawRightCircle(canvas);
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
     * 设置左面球的位置 -必须在view加载完成在设置有效   当是单选不是范围模式的时候设置无效
     *
     * @param pos
     */
    public void setLeftSeekBallStepPos(int pos) {
//        if (data == null) return;
        if (seekBarMode != SEEKBAR_MODE_RANGE) return;
        if (pos < 0) pos = 0;
        if (pos > data.size() - 1) pos = data.size() - 1;
        int size = data.size();
        int unitWidth = getUnitWidth(size - 1);
        //设置左面小球的位置
        leftSeekBallX = DEF_PADDING + seekBallRadio + unitWidth * pos;
        //设置背景线的样式
        seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        invalidate();
    }

    /**
     * 设置右面球的位置-必须在view加载完成再设置有效
     *
     * @param pos
     */
    public void setRightSeekBallStepPos(int pos) {
//        if (data == null) return;
        if (pos < 0) pos = 0;
        if (pos > data.size() - 1) pos = data.size() - 1;
        int size = data.size();
        int unitWidth = getUnitWidth(size - 1);
        //设置左面小球的位置
        rightSeekBallX = DEF_PADDING + seekBallRadio + unitWidth * pos;
        //设置背景线的样式
        seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        postInvalidate();
    }


    /**
     * 单选模式设置有效
     * 设置seekbar位置
     *
     * @param value
     */
    public void setSeekBarPos(int value) {
        if (seekBarMode != SEEKBAR_MODE_SINGLE) return;
        if (value < 0) value = 0;
        if (value > maxValue) value = maxValue;
        int totalLenght = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
        rightSeekBallX = (int) (DEF_PADDING + seekBallRadio + (value * 1.0f / maxValue) * totalLenght);
        seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        postInvalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                if (seekBarMode == SEEKBAR_MODE_RANGE) {//范围模式
                    // 根据当前坐标,确定要移动哪个球,因为我们这个是有两个球的,唯一的一个技巧点就是这个地方,
                    // 根据手指按下的坐标找到距离哪个球位置最近就移动哪个球,这里注意下.
                    currentMovingType = getMovingLeftOrRight(downX);
                    if (BallType.LEFT == currentMovingType) {
                        leftSeekBallX = downX;
                    } else if (BallType.RIGHT == currentMovingType) {
                        rightSeekBallX = downX;
                    }
                } else {//单选模式
                    rightSeekBallX = downX;
                }
                seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
                break;
            case MotionEvent.ACTION_MOVE:
                //移动的时候根据计算出来的位置以及方向改变两个小球的位置以及举行进度条的RectF的范围
                int moveX = (int) event.getX();
                //重新计算x值
                if (isStepMove) {
                    moveX = getCurrentX(moveX);
                }
                if (seekBarMode == SEEKBAR_MODE_RANGE) {//范围模式
                    // 特殊情况处理,两个球重合应该怎么办,
                    if (leftSeekBallX == rightSeekBallX) {
                        if (moveX - downX > 0) {//向右滑动
                            currentMovingType = BallType.RIGHT;
                            downX = leftSeekBallX;
                            rightSeekBallX = moveX;
                        } else {// 向左滑动
                            currentMovingType = BallType.LEFT;
                            downX = rightSeekBallX;
                            leftSeekBallX = moveX;
                        }
                    } else {
                        if (BallType.LEFT == currentMovingType) {
                            leftSeekBallX = leftSeekBallX - rightSeekBallX >= 0 ? rightSeekBallX : moveX;
                        } else if (BallType.RIGHT == currentMovingType) {
                            rightSeekBallX = rightSeekBallX - leftSeekBallX <= 0 ? leftSeekBallX : moveX;
                        }
                    }
                } else {
                    rightSeekBallX = moveX;
                }
                seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //手指离开的时候,确定返回给UI的数据集 回弹到制定分割位置
                if (!isStepMove) {//是否按步移动
                    if (seekBarMode == SEEKBAR_MODE_RANGE) {//范围模式
                        if (BallType.LEFT == currentMovingType) {
                            leftSeekBallX = leftSeekBallX - rightSeekBallX >= 0 ? rightSeekBallX : getCurrentX((int) event.getX());
                        } else if (BallType.RIGHT == currentMovingType) {
                            rightSeekBallX = rightSeekBallX - leftSeekBallX <= 0 ? leftSeekBallX : getCurrentX((int) event.getX());
                        }
                    } else {
                        rightSeekBallX = getCurrentX((int) event.getX());
                    }
                }
                break;
        }

        if (seekBarMode == SEEKBAR_MODE_RANGE) {//范围模式
            // 边界处理,确保左边的球不会超过右边的,右边的不会超过左边的
            if (BallType.LEFT == currentMovingType) {
                if (leftSeekBallX < seekBallRadio + DEF_PADDING) {
                    leftSeekBallX = seekBallRadio + DEF_PADDING;
                }
                if (leftSeekBallX > viewWidth - seekBallRadio - DEF_PADDING) {
                    leftSeekBallX = viewWidth - seekBallRadio - DEF_PADDING;
                }
            } else if (BallType.RIGHT == currentMovingType) {
                if (rightSeekBallX < seekBallRadio + DEF_PADDING) {
                    rightSeekBallX = seekBallRadio + DEF_PADDING;
                }
                if (rightSeekBallX > viewWidth - seekBallRadio - DEF_PADDING) {
                    rightSeekBallX = viewWidth - seekBallRadio - DEF_PADDING;
                }
            }
        } else {
            // 边界处理
            if (rightSeekBallX < seekBallRadio + DEF_PADDING) {
                rightSeekBallX = seekBallRadio + DEF_PADDING;
            }
            if (rightSeekBallX > viewWidth - seekBallRadio - DEF_PADDING) {
                rightSeekBallX = viewWidth - seekBallRadio - DEF_PADDING;
            }
        }

        seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);

        if (null != dragFinishedListener) {
            dragFinishedListener.dragFinished(getCurrentValue(leftSeekBallX), getCurrentValue(rightSeekBallX));
        }
        postInvalidate();
        return true;
    }

    /**
     * 设置当前值
     *
     * @param moveX
     * @return
     */
    public int getCurrentX(int moveX) {
        int totalLength = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
        //当前手指x占总长的百分比
        float x = 1.0f * (moveX - DEF_PADDING - seekBallRadio) / totalLength;
        if (x < 0) {
            x = 0;
        } else if (x > 1) {
            x = 1;
        }
        //手指x所在的步数
        float pos = 1.0f * maxValue * x / stepLenght;
        return ((int) (((Math.round(pos) * stepLenght) * 1.0f / maxValue) * totalLength) + DEF_PADDING + seekBallRadio);
    }

    /**
     * 获取当前值
     *
     * @param moveX
     * @return
     */
    public float getCurrentValue(int moveX) {
        int totalLength = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
        //当前手指x占总长的百分比
        float x = 1.0f * (moveX - DEF_PADDING - seekBallRadio) / totalLength;
        if (x < 0) {
            x = 0;
        } else if (x > 1) {
            x = 1;
        }
        //手指x所在的步数
        float pos = 1.0f * maxValue * x / stepLenght;
        //手指所在的值
        float index = Math.round(pos) * stepLenght;
        if (index < 0) {
            index = 0;
        } else if (index > maxValue) {
            index = maxValue;
        }
        return index;
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
     * 绘制右面的圆圈
     *
     * @param canvas
     */
    private void drawRightCircle(Canvas canvas) {
        canvas.drawCircle(rightSeekBallX, seekBallY, seekBallRadio, seekBallStrokePaint);
        canvas.drawCircle(rightSeekBallX, seekBallY, seekBallRadio - SEEK_STROKE_SIZE, seekBallEndPaint);
    }

    /**
     * 绘制左面的圆圈
     *
     * @param canvas
     */
    private void drawLeftCircle(Canvas canvas) {
        canvas.drawCircle(leftSeekBallX, seekBallY, seekBallRadio, seekBallStrokePaint);
        canvas.drawCircle(leftSeekBallX, seekBallY, seekBallRadio - SEEK_STROKE_SIZE, seekBallPaint);
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
        return Math.round((viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio) * 1.0f / count);
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
    public RangeSeekBarView setRangeData(List<String> data) {
        this.data = data;
        leftPosition = 0;
        if (null != data && data.size() != 0) {
            rightPosition = data.size() - 1;
        }
        return this;
    }

    /**
     * 设置最大值
     *
     * @param maxValue
     * @return
     */
    public RangeSeekBarView setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    /**
     * 设置步数
     *
     * @param stepLenght
     * @return
     */
    public RangeSeekBarView setStepLenght(float stepLenght) {
        this.stepLenght = stepLenght;
        return this;
    }

    /**
     * 设置拖动回调
     *
     * @param dragFinishedListener
     * @return
     */
    public RangeSeekBarView setOnDragFinishedListener(OnDragFinishedListener dragFinishedListener) {
        this.dragFinishedListener = dragFinishedListener;
        return this;
    }

    /**
     * 设置布局加载完成回调
     *
     * @param onLayoutLoadCompleteListener
     * @return
     */
    public RangeSeekBarView setOnLayoutLoadCompleteListener(OnLayoutLoadCompleteListener onLayoutLoadCompleteListener) {
        this.onLayoutLoadCompleteListener = onLayoutLoadCompleteListener;
        return this;
    }


    private int getMovingLeftOrRight(int actionX) {
        return Math.abs(leftSeekBallX - actionX) - Math.abs(rightSeekBallX - actionX) > 0 ? BallType.RIGHT : BallType.LEFT;
    }


    public int dp2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    private static class BallType {
        private static final int LEFT = 99;
        private static final int RIGHT = 98;
    }

    /**
     * 拖动接口
     */
    public interface OnDragFinishedListener {
        /**
         * 拖拽回调
         *
         * @param leftValue  左面球滑动位置
         * @param rightValue 右面球滑动位置
         */
        void dragFinished(float leftValue, float rightValue);

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
