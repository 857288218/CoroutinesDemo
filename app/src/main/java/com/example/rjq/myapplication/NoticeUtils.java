package com.example.rjq.myapplication;

import android.support.annotation.StringRes;
import android.widget.Toast;

public class NoticeUtils {
    private static Toast toast;

    public static void showToast(final @StringRes int resId) {
        showToast(BaseApplication.context.getString(resId));
    }

    public static void showToast(final String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(BaseApplication.context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

}
