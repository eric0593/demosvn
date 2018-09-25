package com.idea.jgw;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.logic.btc.BtcWalltUtils;
import com.idea.jgw.utils.common.MyLog;
import com.idea.jgw.utils.common.SharedPreferenceManager;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Stack;

//import cn.smssdk.SMSSDK;

/**
 * 当前Application
 * Created by 9 on 2016/5/21.
 */

public class App extends Application {
    public static App mApp;

    public static App getInstance() {
        return mApp;
    }

    public static Stack<Activity> activityStack;//App所有页面堆栈

    //开关类
    public static boolean debug = false;  //是否测试
    public static String APP_KEY = "1ac3660a73e00";
    public static String APP_SECRET = "28051a43a5283acea68e0e13b0b4e76c";


    //是否已登录状态，默认false，即app被杀死后需要重新验证登录
    public static boolean login = false;
    //测试IP
    public static boolean testIP = true;
    //钱包测试
    public static boolean isWalletDebug = false;

    //钱包测试，直接跳转到首页
    public static boolean isIsWalletDebug2 = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        MyLog.setDebug(debug);

        //bug收集
//        CrashReport.initCrashReport(this, "5c69a04d04", debug);

        //初始化自定义全局异常捕捉器
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());

        BtcWalltUtils.init();

        if (debug) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
            initLeakcanary(this);
        }
        ARouter.init(this); // 尽可能早，推荐在Application中初始化
    }

    // 把一个activity压入栈中
    public static void pushOneActivity(Activity actvity) {
        if (App.activityStack == null) {
            App.activityStack = new Stack<Activity>();
        }
        App.activityStack.add(actvity);
    }

    // 移除一个activity
    public static void popOneActivity(Activity activity) {
        if (App.activityStack != null && App.activityStack.size() > 0) {
            if (activity != null) {
                activity.finish();
                App.activityStack.remove(activity);
                activity = null;
            }
        }
    }

    /**
     * 查看内存溢出的
     * @param application
     */
    public void initLeakcanary(Application application){
        if (LeakCanary.isInAnalyzerProcess(application)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(application);
    }

    // 获取栈顶的activity，先进后出原则
    public static Activity getLastActivity() {
        return App.activityStack.lastElement();
    }

    // 退出所有activity
    public static void finishAllActivity() {
        if (App.activityStack != null) {
            while (App.activityStack.size() > 0) {
                Activity activity = getLastActivity();
                if (activity == null)
                    break;
                popOneActivity(activity);
            }
        }
    }


    public void logout() {
        // 关闭所有Activity
        if (activityStack != null) {
            for (Activity activity : activityStack) {
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }
            activityStack.clear();
        }
    }

}
