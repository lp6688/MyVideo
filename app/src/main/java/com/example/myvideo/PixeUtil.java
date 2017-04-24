package com.example.myvideo;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by lp66 on 2017/4/18.
 */

public class PixeUtil {
    /**
     * context
     */
    private static Context mContext;
    public static void initContext(Context context){
        mContext=context;
    }


    /*
    public static int dp2px(float value){
        final float scale= mContext.getResources().getDisplayMetrics().densityDpi;
        return (int)(value*(scale/160)+0.5f);
    }
    */
    public static int dp2px(Context context , float dp)
    {
        //获取设备屏幕密度
        float density = context.getResources().getDisplayMetrics().density;
        //加0.5是为了四舍五入
        int px = (int) (dp * density + 0.5f);
        return px;
    }

    public static float px2dp(Context context , float px)
    {
        float density = context.getResources().getDisplayMetrics().density;
        float dp = px / density;
        return dp;
    }

    public static int sp2px(Context context , int sp)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP , sp , context.getResources().getDisplayMetrics());
    }

    public static int dp2Px(Context context , float dp)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP , dp , context.getResources().getDisplayMetrics());
    }
}

