package com.idea.jgw.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.FileProvider;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.ui.service.ScreenListenerService;
import com.idea.jgw.utils.common.MyLog;
import com.joker.api.Permissions4M;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import rx.Subscription;

/**
 * Created by idea on 2018/5/16.
 */

public abstract class BaseActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreenFlags();
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        initView();
        App.pushOneActivity(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void setFullScreenFlags() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public abstract int getLayoutId();
    public abstract void initView();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.popOneActivity(this);
    }

    public void unSubscribe(Subscription subscription) {
        if(subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
    long lastTouchTime;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastTouchTime > 1000) {
            lastTouchTime = currentTime;
            sendBroadcast(new Intent(ScreenListenerService.ACTION_TOUCH_SCREEN));
            MyLog.e("dispatchTouchEvent ======");
        }
        return super.dispatchTouchEvent(ev);
    }

    public void share(Context context) {
        /** * 分享图片 */
        String filePath = SnapShort(this);
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("image/*");  //设置分享内容的类型
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, "com.idea.jgw.fileprovider", new File(filePath));
            share_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        } else {
            uri = Uri.fromFile(new File(filePath));
        }
        share_intent.putExtra(Intent.EXTRA_STREAM, uri);
        //创建分享的Dialog
        share_intent = Intent.createChooser(share_intent, context.getString(R.string.share_to));
        context.startActivity(share_intent);
    }

    public String SnapShort(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.buildDrawingCache();

        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = activity.getWindowManager().getDefaultDisplay();

        int widths = display.getWidth();
        int heights = display.getHeight();

        view.setDrawingCacheEnabled(true);

        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0, statusBarHeights, widths,
                heights - statusBarHeights);

        view.destroyDrawingCache();

        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = picDir.getPath() + File.separator + "IMAGE_" + timeStamp + ".jpg";
        File file = new File(filename);

        if (!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bmp.recycle();

        return filename;
    }

    public void reLogin() {
        ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
        App.finishAllActivity();
        finish();
    }

    public void requestPermission(int requestCode, String permission) {
        Permissions4M.get(this)
                // 是否强制弹出权限申请对话框，建议设置为 true，默认为 true
                .requestForce(true)
                // 是否支持 5.0 权限申请，默认为 false
                .requestUnderM(true)
                // 权限，单权限申请仅只能填入一个
//                .requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .requestPermissions(permission)
                // 权限码
                .requestCodes(requestCode)
                // 如果需要使用 @PermissionNonRationale 注解的话，建议添加如下一行
                // 返回的 intent 是跳转至**系统设置页面**
                .requestPageType(Permissions4M.PageType.MANAGER_PAGE)
                // 返回的 intent 是跳转至**手机管家页面**
                // .requestPageType(Permissions4M.PageType.ANDROID_SETTING_PAGE)
                .request();
    }

}
