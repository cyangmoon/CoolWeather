package com.ismael.weather.util;

public class BasicTool {

    public static String firstUpperCaseString(String s){
        char[] ch = s.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }
}
