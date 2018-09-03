
package com.james.library.utils;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Dip和Pixel之间转化
 */
public class DipPixelUtil
{
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    @SuppressLint("DefaultLocale")
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
    * @param spValue
    * @param fontScale
    *            (DisplayMetrics类中的scaledDensity属性)
    * @return
    */
   public static int sp2pix(float spValue, float fontScale) {
       return (int) (spValue * fontScale + 0.5f);
   }
   
   /**
    * sp转px
    * 
    * @param context
    * @param spValue
    * @return
    */
   public static int sp2px(Context context,float spValue){
       final float scale = context.getResources().getDisplayMetrics().scaledDensity;
       return (int) (spValue * scale);
   }
    
}
