package com.month.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.month.R;
import com.month.utils.SizeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * 进度指示器
 */
public class ProgressView extends View {

    //TAG
    private static final String TAG = "MyProgressView";

    //控件的高度
    private int mHeight;
    //控件的宽度
    private int mWidth;
    //控件的展开方向（默认水平方向）
    private int mOrientation;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    //流程的个数
    private int mProgressNum;
    private static final int DEFAULT_PROGRESS_NUM = 2;
    //绘制的起始点
    private float mStartY = DEFAULT_START_Y;
    private static final float DEFAULT_START_Y = 0;
    //实线的宽度
    private float mLineWidth;
    private static final float DEFAULT_LINE_WIDTH = SizeUtils.dip2px(2);
    //实线的颜色
    private int mLineColor;
    //圆环的半径
    private float mRadius;
    private static final float DEFAULT_RADIUS = SizeUtils.dip2px(5);
    //圆环的颜色
    private int mCircleColor;
    //圆环的位置（圆心）
    private float[] mCirclePositions;
    //圆环是否均匀绘制
    private boolean mIsAverage = false;
    //当前进度
    private int mProgress = -1;
    /*
    绑定的View列表
    绘制图形的位置取决于这些View
     */
    private List<View> mAttachedViewList = new ArrayList<>();

    //用户自定义图片
    private Drawable mDoneDrawable;
    private Drawable mDoingDrawable;
    private Drawable mUndoneDrawable;
    private boolean mIsDrawableAdded = false;
    private int mDoneDrawableHeight;
    private int mDoingDrawableHeight;
    private int mUndoneDrawableHeight;
    private int mDoneDrawableWidth;
    private int mDoingDrawableWidth;
    private int mUndoneDrawableWidth;
    private int mDrawableMaximumWidth;
    private int mDrawableMaximumHeight;

    //画笔
    private Paint mLinePaint;
    private Paint mInnerCirclePaint;
    private Paint mOuterCirclePaint;

    /*
     * =========================================================
     *                      构造器/初始化
     * =========================================================
     */
    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaints();
    }

    /**
     * 初始化属性
     * @param context
     * @param attrs
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        mProgressNum = a.getInt(R.styleable.ProgressView_progressNum, DEFAULT_PROGRESS_NUM);
        mOrientation = a.getInt(R.styleable.ProgressView_orientation, HORIZONTAL);
        mRadius = a.getDimension(R.styleable.ProgressView_circleRadius, DEFAULT_RADIUS);
        mCircleColor = a.getColor(R.styleable.ProgressView_circleColor, Color.parseColor("#D0021B"));
        mLineWidth = a.getDimension(R.styleable.ProgressView_lineWidth, DEFAULT_LINE_WIDTH);
        mLineColor = a.getColor(R.styleable.ProgressView_lineColor, Color.parseColor("#FF0000"));
        mIsAverage = a.getBoolean(R.styleable.ProgressView_isAverage, false);
        mDoneDrawable = a.getDrawable(R.styleable.ProgressView_doneSrc);
        mDoingDrawable = a.getDrawable(R.styleable.ProgressView_doingSrc);
        mUndoneDrawable = a.getDrawable(R.styleable.ProgressView_undoneSrc);
        computeFinalSize();
        //回收
        a.recycle();
    }

    /**
     * 计算 View 最终的宽度或高度 (根据方向决定)
     * 如果使用了自定义图片 则计算各图的宽度/高度最大值 (考虑图片大小不一的情况)
     * 如果是默认情况 则以直径为最终结果
     */
    private void computeFinalSize() {
        if (mDoneDrawable != null || mDoingDrawable != null || mUndoneDrawable != null) {
            mIsDrawableAdded = true;
            if (mDoneDrawable != null) {
                mDoneDrawableWidth = mDoneDrawable.getIntrinsicWidth();
                mDoneDrawableHeight = mDoneDrawable.getIntrinsicHeight();
            }
            if (mDoingDrawable != null) {
                mDoingDrawableWidth = mDoingDrawable.getIntrinsicWidth();
                mDoingDrawableHeight = mDoingDrawable.getIntrinsicHeight();
            }
            if (mUndoneDrawable != null) {
                mUndoneDrawableWidth = mUndoneDrawable.getIntrinsicWidth();
                mUndoneDrawableHeight = mUndoneDrawable.getIntrinsicHeight();
            }
            /*
            取得Drawable的最大宽度和高度
            用于控件的测量和绘制，确保最大的图片能被完整绘制
             */
            mDrawableMaximumWidth = Math.max(mDoneDrawableWidth, Math.max(mDoingDrawableWidth, mUndoneDrawableWidth));
            mDrawableMaximumHeight = Math.max(mDoneDrawableWidth, Math.max(mDoingDrawableWidth, mUndoneDrawableWidth));
            if (mOrientation == HORIZONTAL) {
                mHeight = mDrawableMaximumHeight;
            } else if (mOrientation == VERTICAL) {
                mWidth = mDrawableMaximumWidth;
            }

        } else {
            if (mOrientation == HORIZONTAL) {
                mHeight = (int) (2 * mRadius);
            } else if (mOrientation == VERTICAL) {
                mWidth = (int) (2 * mRadius);
            }
        }
    }

    /**
     * 初始化画笔
     */
    private void initPaints() {
        //初始化实线画笔
        mLinePaint = new Paint();
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setAntiAlias(true);
        //初始化圆形画笔
        mInnerCirclePaint = new Paint();
        mOuterCirclePaint = new Paint();
        mInnerCirclePaint.setColor(Color.WHITE);
        mOuterCirclePaint.setColor(mCircleColor);
        mInnerCirclePaint.setAntiAlias(true);
        mOuterCirclePaint.setAntiAlias(true);
    }

    /*
     * =========================================================
     *                          测量
     * =========================================================
     */
    private int mMeasureMode;
    private static final int MODE_EXACTLY = 101;
    private static final int MODE_OTHERS = 102;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        Log.i(TAG, "heightSize --> " + heightSize);
        Log.i(TAG, "widthSize --> "+ widthSize);
        Log.i(TAG, "heightMode --> " + heightMode);
        Log.i(TAG, "widthMode --> " + widthMode);
        Log.i(TAG, "----------------------");
        //水平展开
        if (mOrientation == HORIZONTAL) {
            //精确模式
            if (widthMode == MeasureSpec.EXACTLY) {
                mWidth = widthSize;
                mMeasureMode = MODE_EXACTLY;
                setMeasuredDimension(mWidth, mHeight);
            }
            //其他模式
            else if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
                mMeasureMode = MODE_OTHERS;
                setMeasuredDimension(0, mHeight);
            }
        }
        //垂直展开
        else if (mOrientation == VERTICAL) {
            //精确模式
            if (heightMode == MeasureSpec.EXACTLY) {
                mHeight = heightSize;
                mMeasureMode = MODE_EXACTLY;
                setMeasuredDimension(mWidth, mHeight);
            }
            //其他模式
            else if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
                mMeasureMode = MODE_OTHERS;
                setMeasuredDimension(mWidth, 0);
            }
        }
    }

    /*
     * =========================================================
     *                          绘制
     * =========================================================
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG,"-- onDraw --");
        super.onDraw(canvas);
        //判断是否均匀绘制
        if(mIsAverage) {
            drawAverage(canvas);
        } else {
            drawUnlimited(canvas);
        }
    }

    /**
     * 均匀绘制圆环
     * @param canvas
     */
    private void drawAverage(Canvas canvas) {
        if (mProgressNum <= 1) return;
        if (mOrientation == HORIZONTAL) {
            //自定义流程图绘制
            if (mIsDrawableAdded) {
                //绘制实线
                int dy = mDrawableMaximumHeight >> 1;
                canvas.drawLine(0, dy, mWidth, dy, mLinePaint);
                //绘制图片
                int between = (mWidth - mDrawableMaximumWidth) / (mProgressNum - 1);
                for (int i = 0; i < mProgressNum; i++) {
                    int left = between * i;
                    if (i + 1 < mProgress) {
                        mDoneDrawable.setBounds(left, 0, left + mDoneDrawableWidth, mDoneDrawableHeight);
                        mDoneDrawable.draw(canvas);
                    } else if (i + 1 == mProgress) {
                        mDoingDrawable.setBounds(left, 0, left + mDoingDrawableWidth, mDoingDrawableHeight);
                        mDoingDrawable.draw(canvas);
                    } else {
                        mUndoneDrawable.setBounds(left, 0, left + mUndoneDrawableWidth, mUndoneDrawableHeight);
                        mUndoneDrawable.draw(canvas);
                    }
                }
            }
            //默认绘制
            else {
                //绘制实线
                canvas.drawLine(0, mRadius, mWidth, mRadius, mLinePaint);
                //绘制图片
                float between = (mWidth - 2 * mRadius) / (mProgressNum - 1);
                for (int i = 0; i < mProgressNum; i++) {
                    float pos = between * i + mRadius;
                    canvas.drawCircle(pos, mRadius, mRadius, mOuterCirclePaint);
                    if (i + 1 != mProgress) {
                        canvas.drawCircle(pos, mRadius, mRadius / 2, mInnerCirclePaint);
                    }
                }
            }
        }
        else if (mOrientation == VERTICAL) {
            //自定义流程图绘制
            if (mIsDrawableAdded) {
                //绘制实线
                int dx = mDrawableMaximumWidth >> 1;
                canvas.drawLine(dx, 0, dx, mHeight, mLinePaint);
                //绘制图片
                int between = (mHeight - mDrawableMaximumHeight) / (mProgressNum - 1);
                for (int i = 0; i < mProgressNum; i++) {
                    int top = (int) (between * i);
                    if (i + 1 < mProgress) {
                        mDoneDrawable.setBounds(0, top, mDoneDrawableWidth, top + mDoneDrawableHeight);
                        mDoneDrawable.draw(canvas);
                    } else if (i + 1 == mProgress) {
                        mDoingDrawable.setBounds(0, top, mDoingDrawableWidth, top + mDoingDrawableHeight);
                        mDoingDrawable.draw(canvas);
                    } else {
                        mUndoneDrawable.setBounds(0, top, mUndoneDrawableWidth, top + mUndoneDrawableHeight);
                        mUndoneDrawable.draw(canvas);
                    }
                }
            }
            //默认绘制
            else {
                //绘制实线
                canvas.drawLine(mRadius, mStartY, mRadius, mHeight, mLinePaint);
                //绘制图片
                float between = (mHeight - 2 * mRadius) / (mProgressNum - 1);
                for (int i = 0; i < mProgressNum; i++) {
                    float pos = between * i + mRadius;
                    canvas.drawCircle(mRadius, pos, mRadius, mOuterCirclePaint);
                    if (i + 1 != mProgress) {
                        canvas.drawCircle(mRadius, pos, mRadius / 2, mInnerCirclePaint);
                    }
                }
            }
        }
    }

    /**
     * 自由绘制圆环
     * @param canvas
     */
    private void drawUnlimited(Canvas canvas) {
        if (mProgressNum == 0) return;
        //自定义流程图绘制
        if (mIsDrawableAdded) {
            //绘制实线
            int dx = mWidth >> 1;
            canvas.drawLine(dx, mStartY, dx, mHeight, mLinePaint);
            //绘制图片
            for (int i = 0; i < mProgressNum; i++) {
                float pos = mCirclePositions[i];
                if (i + 1 < mProgress) {
                    int x = mDoneDrawableHeight / 2;
                    mDoneDrawable.setBounds(0, (int)(pos - x), mDoneDrawableWidth, (int)(pos + x));
                    mDoneDrawable.draw(canvas);
                } else if (i + 1 == mProgress) {
                    int x = mDoingDrawableHeight / 2;
                    mDoingDrawable.setBounds(0, (int)(pos - x), mDoingDrawableWidth, (int)(pos + x));
                    mDoingDrawable.draw(canvas);
                } else {
                    int x = mUndoneDrawableHeight / 2;
                    mUndoneDrawable.setBounds(0, (int)(pos - x), mUndoneDrawableWidth, (int)(pos + x));
                    mUndoneDrawable.draw(canvas);
                }
            }
        }
        //默认绘制
        else {
            //绘制实线
            canvas.drawLine(mRadius, mStartY, mRadius, mHeight, mLinePaint);
            //绘制图片
            for (int i = 0; i < mProgressNum; i++) {
                float pos = mCirclePositions[i];
                canvas.drawCircle(mRadius, pos, mRadius, mOuterCirclePaint);
                if (i + 1 != mProgress) {
                    canvas.drawCircle(mRadius, pos, mRadius / 2, mInnerCirclePaint);
                }
            }
        }
    }

    /*
     * =========================================================
     *                      public method
     * =========================================================
     */

    /**
     * 设置当前进度
     * @param num
     */
    public void setProgress(int num) {
        if (num < 1 || num > mProgressNum) return;
        mProgress = num;
        invalidate();
    }

    /**
     * 设置View的高度
     * @param height
     */
    public void setHeight(int height) {
        mHeight = height;
    }

    /**
     * 设置流程数量
     * @param progressNum
     */
    public void setProgressNum(int progressNum) {
        this.mProgressNum = progressNum;
    }

    /**
     * 设置绘制的Y轴起始点
     * @param y
     */
    public void setStartY(float y) {
        mStartY = y;
    }

    /**
     * 设置圆环的半径
     * @param radius
     */
    public void setRadius(int radius) {
        mRadius = radius;
        mWidth = 2 * radius;
    }

    public float getRadius() {
        return mRadius;
    }

    /**
     * 设置圆环的颜色
     * @param color
     */
    public void setCircleColor(int color) {
        mCircleColor = color;
        mOuterCirclePaint.setColor(mCircleColor);
    }

    /**
     * 设置实线的宽度
     * @param width
     */
    public void setLineWidth(float width) {
        mLineWidth = width;
        mLinePaint.setStrokeWidth(mLineWidth);
    }

    /**
     * 设置实线的颜色
     * @param color
     */
    public void setLineColor(int color) {
        mLineColor = color;
        mLinePaint.setColor(mLineColor);
    }

    /**
     * 设置圆环的位置
     * @param floats
     */
    public void setCirclePositions(float[] floats) {
        this.mCirclePositions = floats;
    }

    /**
     * 绑定View，圆环的绘制位置取决于此View
     * @param views
     */
    public void attachViews (View... views) {
        int len = views.length;
        mAttachedViewList.clear();
        mAttachedViewList.addAll(Arrays.asList(views));
        //流程个数等于绑定的View数
        mProgressNum = len;
        mCirclePositions = new float[len];
    }

    /*
     * =========================================================
     *                    生命周期相关方法
     * =========================================================
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.post(new Runnable() {
            @Override
            public void run() {
                computePosition();
                if (mMeasureMode == MODE_OTHERS) {
                    measureDelay();
                }
            }
        });
    }

    /**
     * 根据绑定的View计算绘制的位置
     * 计算方式：(顶部位置 + 底部位置) / 2
     */
    private void computePosition() {
        int count = mAttachedViewList.size();
        if (count == 0 || mIsAverage) return;   //未attach或平均分配的情况下无需计算
        for (int i = 0; i < count; i++) {
            View v = mAttachedViewList.get(i);
            int padding = ((ViewGroup) v.getParent()).getPaddingTop();
            float pos = (v.getTop() + v.getBottom() - 2 * padding) >> 1;
            mCirclePositions[i] = pos;
        }
        mStartY = mCirclePositions[0];
        this.invalidate();
    }

    /**
     *  AT_MOST模式 和 UNSPECIFIED模式下的 View 测量属性
     *  默认为父容器的最终测量属性
     *  即高度为父容器高，宽度为父容器宽
     */
    private void measureDelay() {
        Log.i(TAG,"--> measureDelay <--");
        if (mOrientation == HORIZONTAL) {
            int finalWidth = ((ViewGroup) getParent()).getMeasuredWidth();
            Log.i(TAG, "finalWidth --> " + finalWidth);
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = finalWidth;
            setLayoutParams(layoutParams);
            mWidth = finalWidth;
        }
        else if (mOrientation == VERTICAL) {
            int finalHeight = ((ViewGroup) getParent()).getMeasuredHeight();
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.height = finalHeight;
            setLayoutParams(layoutParams);
            mHeight = finalHeight;
        }
    }
}
