package com.oguzhandongul.floatinglogcat;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class Utils {

    public static int convertDpiToPixel(Context ctx, int dpi) {
        float pixel = 0;
        try {
            Resources r = ctx.getResources();
            pixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi,
                    r.getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (int) pixel;
    }

    public static boolean isMyServiceRunning(Activity activity, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
