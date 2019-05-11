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

import java.util.List;

/**
 * 范围选择seekbar
 */
public class RangeSeekBarView_old extends View {
    private static final float SEEK_BG_SCALE = 0.8F / 2;//设置线位置在总高度的比例
    private static final float SEEK_TEXT_SCALE = 3.2F / 3.5F;//设置文字位置在总高度的比例
    private static final int DEF_HEIGHT = 70; // 总高度
    private static final int DEF_PADDING = 20;//左右padding
    private static final int BG_HEIGHT = 10;
    private static final int SEEK_STROKE_SIZE = 2;
    private static final String TAG = RangeSeekBarView_old.class.getSimpleName();

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
    private float maxValue, minValue;

    private float stepLenght = 1;

    //是否启用
    private boolean isCanEnabled = true;

    private OnLayoutLoadCompleteListener mOnLayoutLoadCompleteListener;

    public RangeSeekBarView_old(Context context) {
        this(context, null);
    }

    public RangeSeekBarView_old(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSeekBarView_old(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RangeSeekBarView_old, defStyleAttr, R.style.RangeSeekBarViewStyle);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.RangeSeekBarView_old_seek_bg_color) {
                seekBgColor = typedArray.getColor(attr, Color.parseColor("#D9D9D9"));

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_pb_color) {
                seekPbColor = typedArray.getColor(attr, Color.parseColor("#FED854"));

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_ball_solid_color) {
                seekBallSolidColor = typedArray.getColor(attr, Color.parseColor("#FFFFFF"));

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_ball_stroke_color) {
                seekBallStrokeColor = typedArray.getColor(attr, Color.parseColor("#FED854"));

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_text_color) {
                seekTextColor = typedArray.getColor(attr, Color.parseColor("#959595"));

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_text_size) {
                seekTextSize = typedArray.getDimensionPixelSize(attr, 14);

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_vertical_line) {
                isShowVerticalLine = typedArray.getBoolean(attr, false);

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_step_move) {
                isStepMove = typedArray.getBoolean(attr, false);

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_mode) {
                seekBarMode = typedArray.getInt(attr, SEEKBAR_MODE_RANGE);

            } else if (attr == R.styleable.RangeSeekBarView_old_seek_step_length) {
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

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        updateRangeSeekBarXY();
    }

    /**
     * 更新滑动控件的xy位置
     */
    public void updateRangeSeekBarXY() {
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
     * 设置默认值(范围模式）
     *
     * @param leftValue
     * @param rightValue
     */
    public RangeSeekBarView_old setRangeSeekBallValue(final float leftValue, final float rightValue) {
        if (seekBarMode != SEEKBAR_MODE_RANGE) return this;
        setOnLayoutLoadCompleteListener(new OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                setLeftSeekBallValue(leftValue);
                setRightSeekBallValue(rightValue);
            }
        });
        return this;
    }

    /**
     * 设置左面球的位置 -必须在view加载完成在设置有效
     *
     * @param leftValue
     */
    private RangeSeekBarView_old setLeftSeekBallValue(final float leftValue) {
        float value = leftValue;
        if (value <= minValue) value = minValue;
        if (value >= maxValue) value = maxValue;

        float pos = 1.0f * (value - minValue) / stepLenght;
        int totalLength = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
        leftSeekBallX = ((int) (((Math.round(pos) * stepLenght) * 1.0f / maxValue()) * totalLength) + DEF_PADDING + seekBallRadio);
        if (value == maxValue) {
            leftSeekBallX = DEF_PADDING + seekBallRadio + totalLength;
        }
        if (value == minValue) {
            leftSeekBallX = DEF_PADDING + seekBallRadio;
        }
        //设置背景线的样式
        seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        postInvalidate();
        return this;
    }

    /**
     * 设置右面球的位置-必须在view加载完成再设置有效
     *
     * @param rightValue
     */
    private RangeSeekBarView_old setRightSeekBallValue(final float rightValue) {
        float value = rightValue;
        if (value <= minValue) value = minValue;
        if (value >= maxValue) value = maxValue;
        // value占maxvlaue的百分比
        float pos = 1.0f * (value - minValue) / stepLenght;
        int totalLength = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
        rightSeekBallX = ((int) (((Math.round(pos) * stepLenght) * 1.0f / maxValue()) * totalLength) + DEF_PADDING + seekBallRadio);
        if (value == maxValue) {
            rightSeekBallX = DEF_PADDING + seekBallRadio + totalLength;
        }
        if (value == minValue) {
            rightSeekBallX = DEF_PADDING + seekBallRadio;
        }
        //设置背景线的样式
        seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
        postInvalidate();
        return this;
    }


    /**
     * 单选模式设置有效
     * 设置seekbar位置
     *
     * @param rightValue
     */
    public RangeSeekBarView_old setSingleSeekBallValue(final float rightValue) {
        setOnLayoutLoadCompleteListener(new OnLayoutLoadCompleteListener() {
            @Override
            public void loadComplete() {
                float value = rightValue;
                if (seekBarMode != SEEKBAR_MODE_SINGLE) return;
                if (value < minValue) value = minValue;
                if (value > maxValue) value = maxValue;
                int totalLenght = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
                rightSeekBallX = (int) (DEF_PADDING + seekBallRadio + ((value - minValue) * 1.0f / maxValue()) * totalLenght);
                if (value == maxValue) {
                    rightSeekBallX = DEF_PADDING + seekBallRadio + totalLenght;
                }
                if (value == minValue) {
                    rightSeekBallX = DEF_PADDING + seekBallRadio;
                }
                seekPbRectF = new RectF(leftSeekBallX, viewHeight * SEEK_BG_SCALE, rightSeekBallX, viewHeight * SEEK_BG_SCALE + BG_HEIGHT);
                postInvalidate();
            }
        });
        return this;
    }


    /**
     * 获取当前左边值  控件加载完成后调用
     *
     * @return
     */
    public float getCurrentLeftValue() {
        return getCurrentValue(leftSeekBallX);
    }

    /**
     * 获取当前右边值  控件加载完成后调用
     *
     * @return
     */
    public float getCurrentRightValue() {
        return getCurrentValue(rightSeekBallX);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanEnabled) return false;
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //当触摸点在两球的位置时才可以操作滑动，如果不在就直接返回
                if (!isTouchPointInCircle(event.getX(), event.getY())) {
                    return false;
                }
                downX = (int) event.getX();
                //重新计算x值
                if (isStepMove) {
                    downX = getCurrentX(downX);
                }
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

        //球的位置 边界处理
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
//        int totalLength = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
//        //当前手指x占总长的百分比
//        float x = 1.0f * (moveX - DEF_PADDING - seekBallRadio) / totalLength;
//        if (x < 0) {
//            x = 0;
//        } else if (x > 1) {
//            x = 1;
//        }
//        //手指x所在的步数
//        float pos = 1.0f * maxValue() * x / stepLenght;
//
//        //如果最大值和步数取余不是0  则要考虑最后一步的问题
//        if (maxValue() % stepLenght != 0) {
//            //如果所在步数对上取整等于所有步数
//            if (Math.ceil(pos) == Math.floor(maxValue() / stepLenght) + 1) {
//                return totalLength + DEF_PADDING + seekBallRadio;
//            }
//        } else {
//            if (Math.round(pos) == Math.floor(maxValue() / stepLenght)) {
//                return totalLength + DEF_PADDING + seekBallRadio;
//            }
//        }
//        return ((int) ((((Math.round(pos) * stepLenght)) * 1.0f / maxValue()) * totalLength) + DEF_PADDING + seekBallRadio);


        int totalLength = viewWidth - 2 * DEF_PADDING - 2 * seekBallRadio;
        float value = getCurrentValue(moveX);
        return (int) ((value - minValue) * 1.0f / maxValue() * totalLength) + DEF_PADDING + seekBallRadio;
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
        float pos = 1.0f * maxValue() * x / stepLenght;
        //手指所在的值
        float index = minValue + Math.round(pos) * stepLenght;
        if (index <= minValue) {
            index = minValue;
        } else if (index >= maxValue) {
            index = maxValue;
        }
        //如果最大值和步数取余不是0  则要考虑最后一步的问题
        if (maxValue() % stepLenght != 0) {
            //如果所在步数对上取整等于所有步数
            if (Math.ceil(pos) == Math.floor(maxValue() / stepLenght) + 1) {
                return maxValue;
            }
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
     * 判断触摸点是否在圆的上面
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchPointInCircle(float x, float y) {
        if (isTouchPointInLeftCircle(x, y) || isTouchPointInRightCircle(x, y)) {
            return true;
        }
        return false;
    }


    /**
     * 是否触摸在左面球上
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isTouchPointInLeftCircle(float x, float y) {
        //点击位置x坐标与圆心的x坐标的距离
        float distanceX = Math.abs(leftSeekBallX - x);
        //点击位置y坐标与圆心的y坐标的距离
        float distanceY = Math.abs(seekBallY - y);
        //点击位置与圆心的直线距离
        int distanceZ = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        //如果点击位置与圆心的距离大于圆的半径，证明点击位置没有在圆内
        if (distanceZ > seekBallRadio) {
            return false;
        }
        return true;
    }

    /**
     * 是否触摸在右面球上
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isTouchPointInRightCircle(float x, float y) {
        //点击位置x坐标与圆心的x坐标的距离
        float distanceX = Math.abs(rightSeekBallX - x);
        //点击位置y坐标与圆心的y坐标的距离
        float distanceY = Math.abs(seekBallY - y);
        //点击位置与圆心的直线距离
        int distanceZ = (int) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
        //如果点击位置与圆心的距离大于圆的半径，证明点击位置没有在圆内
        if (distanceZ > seekBallRadio) {
            return false;
        }
        return true;
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
    public RangeSeekBarView_old setRangeData(List<String> data) {
        this.data = data;
        leftPosition = 0;
        if (null != data && data.size() != 0) {
            rightPosition = data.size() - 1;
        }
        return this;
    }

    /**
     * 最大值-最小值
     *
     * @return
     */
    public float maxValue() {
        return maxValue - minValue;
    }

    /**
     * 设置最大值
     *
     * @param maxValue
     * @return
     */
    public RangeSeekBarView_old setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        return this;
    }


    /**
     * 设置最小值
     *
     * @param minValue
     * @return
     */
    public RangeSeekBarView_old setMinValue(float minValue) {
        this.minValue = minValue;
        return this;
    }

    /**
     * 设置步数
     *
     * @param stepLenght
     * @return
     */
    public RangeSeekBarView_old setStepLenght(float stepLenght) {
        this.stepLenght = stepLenght;
        return this;
    }

    /**
     * 设置是否启用  默认启用
     *
     * @param isCanEnabled
     * @return
     */
    public RangeSeekBarView_old setCanEnabled(boolean isCanEnabled) {
        this.isCanEnabled = isCanEnabled;
        return this;
    }

    /**
     * 设置拖动回调
     *
     * @param dragFinishedListener
     * @return
     */
    public RangeSeekBarView_old setOnDragFinishedListener(OnDragFinishedListener dragFinishedListener) {
        this.dragFinishedListener = dragFinishedListener;
        return this;
    }


    /**
     * 设置布局加载完成回调
     *
     * @param onLayoutLoadCompleteListener
     * @return
     */
    private RangeSeekBarView_old setOnLayoutLoadCompleteListener(final OnLayoutLoadCompleteListener onLayoutLoadCompleteListener) {
        this.mOnLayoutLoadCompleteListener = onLayoutLoadCompleteListener;
        if (!isFirst) {
            if (null != mOnLayoutLoadCompleteListener) {
                mOnLayoutLoadCompleteListener.loadComplete();
            }
        }
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        viewHeight = getMeasuredHeight();
        viewWidth = getMeasuredWidth();
        if (null != mOnLayoutLoadCompleteListener && isFirst) {
            mOnLayoutLoadCompleteListener.loadComplete();
        }
        isFirst = false;
    }

}
