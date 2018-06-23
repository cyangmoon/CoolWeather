package com.ismael.weather.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ismael.weather.R;

public class MyToast {
    public static void toastMessage(Context context, String messages, int duration,int color) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toast_style, null);
        ((TextView)view.findViewById(R.id.toast_textView)).setText(messages);
        ((TextView)view.findViewById(R.id.toast_textView)).setTextColor(color);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 12, 20);
        toast.setDuration(duration);
        toast.setView(view);
        toast.show();
    }
}
