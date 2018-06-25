package com.idea.jgw.ui.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.RouterPath;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.utils.common.SharedPreferenceManager;

/**
 * Created by idea on 2018/6/5.
 */

public class ScreenListenerService extends Service {
    public static final String ACTION_TOUCH_SCREEN = "action_touch_screen";
    static final int LOCK_SECOND = 1 * 60;
    int lockScreenSecond = LOCK_SECOND;
    Thread lockScreenThread;

    BroadcastReceiver screenBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                verifyGesturePwd();
            } else if(intent.getAction().equals(ACTION_TOUCH_SCREEN)) {
                lockScreenSecond = LOCK_SECOND;
                startLockCountDown();
            }
        }
    };

    private void verifyGesturePwd() {
        if(!TextUtils.isEmpty((String) SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_GESTURE_PWD, ""))) {
            ARouter.getInstance().build(RouterPath.VERIFY_GESTURE_PASSWORD_ACTIVITY).navigation();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(ACTION_TOUCH_SCREEN);
        registerReceiver(screenBroadcastReceiver, filter);

        startLockCountDown();
    }

    private void startLockCountDown() {
        if(lockScreenThread == null || !lockScreenThread.isAlive()) {
            lockScreenThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (lockScreenSecond > 0) {
                        try {
                            Thread.sleep(1* 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lockScreenSecond--;
                    }
                    verifyGesturePwd();
                }
            });
            lockScreenThread.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenBroadcastReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
