package com.ismael.weather.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ismael.weather.R;

/**
 * Providing a SurfaceView that has wave effect on its head
 *
 * @author Ismael Simon
 * @
 */
public class TopWaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Context mContext;
    private Canvas mCanvas;
    private Paint mPaint;
    private Path mPath;
    private SurfaceHolder mHolder;
    private boolean mIsDrawing;
    private int width;
    private int waveLength;//波长
    private int baseLine;//平衡点距离上端部距离
    private int waveHeight = 50;// 波幅
    private int waveNumber = 1;//周期数
    private int waveColor = Color.BLUE;
    private float offset = 0f;//水平移动偏移量

    public TopWaveSurfaceView(Context context) {
        this(context, null);
    }

    public TopWaveSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public TopWaveSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TopWaveSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPath = new Path();
        mHolder = getHolder();
        this.setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    public void setWaveColor(int color) {
        waveColor = color;
    }

    public void setWaveHeight(int waveHeight) {
        this.waveHeight = waveHeight;
    }

    public void setBaseLine(int baseLine) {
        this.baseLine = baseLine;
    }

    public void setWaveNumber(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    /**
     * 调用此方法前应应保证各项配置已经完成
     */
    public void startDrawingThread() {
        new Thread(this).start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        waveLength = width / waveNumber;
        if (baseLine == 0) baseLine = getMeasuredHeight() / 2;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        mPaint.setColor(mContext.getColor(R.color.purple));
        while (mIsDrawing) {
            synchronized (this) {
                try {
                    mCanvas = mHolder.lockCanvas();
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mCanvas.drawPath(getPath(), mPaint);
                    if (offset > width) {
                        offset = 0;
                    } else {
                        offset += 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != mCanvas) {
                        mHolder.unlockCanvasAndPost(mCanvas);
                    }
                }
            }
        }
    }

    private Path getPath() {
        int halfWaveLength = waveLength / 2;
        Path mPath = new Path();
        // mPath.reset();
        mPath.moveTo(-halfWaveLength * 2 + offset, baseLine);
        for (int i = -2; i < 2 * waveNumber; i++) {
            mPath.quadTo(halfWaveLength * i + halfWaveLength / 2 + offset, baseLine + (i % 2 == 0 ? -waveHeight : waveHeight),
                    halfWaveLength * i + halfWaveLength + offset, baseLine);
        }
        mPath.lineTo(width, 0);
        mPath.lineTo(-width, 0);
        mPath.close();
        return mPath;
    }

}