package com.idea.jgw.utils.common;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


/**
 * Created by idea on 2018/6/13.
 */

public class DialogUtils {

    public static void showAlertDialog(Context context, int resId, DialogInterface.OnClickListener positiveListener) {
        showAlertDialog(context, context.getString(resId), positiveListener);
    }

    public static void showAlertDialog(Context context, String msg, DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton("确定", positiveListener)
                .show();
    }

    public static void showAlertDialog(Context context, String msg, DialogInterface.OnClickListener positiveListner, DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton("确定", positiveListner)
                .setNegativeButton("取消", positiveListener)
                .show();
    }
}
