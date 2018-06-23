package com.ismael.weather.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import java.lang.reflect.Field;

public class BasicTool {

    public static String firstUpperCaseString(String s) {
        char[] ch = s.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x, sbar;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {
            sbar = 0;
            Log.d("PopupListView", "getStatusBarHeight error =" + e1);
            e1.printStackTrace();
        }
        return sbar;
    }

    public static int getActionBarHeight(Context context){
        @SuppressLint("Recycle") TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[] {
                android.R.attr.actionBarSize
        });

        return (int)actionbarSizeTypedArray.getDimension(0, 0);
    }
}
