package com.ismael.weather.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.ismael.weather.gson.Weather;
import com.ismael.weather.util.BasicTool;

/**
 * Description:
 * Date: 7/1/2018
 * Time: 12:21 AM
 * Author: Ismael Simon
 */
public class DrawDailyTmpChart extends View {

    private int height;
    private int width;
    Context mContext;
    private Paint mPaint;
    private Path mPath;
    private int[] tmpHigher;
    private int[] tmpLower;
    private Weather weather;

    public DrawDailyTmpChart(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        initData();
    }

    void initData() {
        weather = Weather.getInstance();
        tmpHigher = new int[6];
        tmpLower = new int[6];
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // mPaint.setColor(mContext.getColor(R.color.white_trans));
        for (int i = 0; i < 6; i++) {
            tmpLower[i] = Integer.parseInt(weather.daily.dailyForecastList.get(i).tmp_min);
            tmpHigher[i] = Integer.parseInt(weather.daily.dailyForecastList.get(i).tmp_max);
        }
        int tmpLowest = BasicTool.minInArry(tmpLower);
        int tmpDelta = BasicTool.maxInArry(tmpHigher) - tmpLowest;
        int deltaY = (height / 2) / 10;
        int deltaX = width / 6;
        mPaint.setColor(Color.parseColor("#30ffffff"));
        for (int i = 1; i < 6; i++) {
            canvas.drawLine(deltaX * i, 0, deltaX * i, height, mPaint);
        }
        mPath.reset();
        float pointX = 0, pointY = 0;
        for (int i = 1; i < 7; i++) {
            pointX = deltaX * i - deltaX / 2;
            pointY = height - (tmpLower[i - 1] - tmpLowest) * deltaY - 50;
            if(i == 1) mPath.moveTo(pointX - deltaX,pointY);
            mPaint.setColor(Color.parseColor("#eeffffff"));
            canvas.drawCircle(pointX, pointY, 8, mPaint);
            mPath.lineTo(pointX, pointY);
            mPaint.setColor(Color.parseColor("#80ffffff"));
            mPaint.setTextSize(35);
            canvas.drawText(tmpLower[i - 1] + "°",pointX-20,pointY + 50,mPaint);
        }
        mPath.lineTo(pointX + deltaX, pointY);
        for (int i = 6; i > 0; i--) {
            pointX = deltaX * i - deltaX / 2;
            pointY = height - (tmpHigher[i - 1] - tmpLowest) * deltaY - 20;
            if (i == 6) mPath.lineTo(pointX + deltaX, pointY);
            mPaint.setColor(Color.parseColor("#eeffffff"));
            canvas.drawCircle(pointX, pointY, 8, mPaint);
            mPath.lineTo(pointX, pointY);
            mPaint.setColor(Color.parseColor("#bbffffff"));
            canvas.drawText(tmpHigher[i - 1] + "°",pointX - 20,pointY - 30,mPaint);
        }
        mPath.lineTo(pointX-deltaX,pointY);
        mPath.close();
        mPaint.setColor(Color.parseColor("#50ffffff"));
        canvas.drawPath(mPath,mPaint);
    }

}

