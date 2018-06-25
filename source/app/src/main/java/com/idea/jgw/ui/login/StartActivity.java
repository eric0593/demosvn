package com.idea.jgw.ui.login;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.service.ScreenListenerService;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.utils.common.SharedPreferenceManager;

/**
 * 启动页
 */
public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_start;
    }

    @Override
    public void initView() {


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String address = "0xaf2f883250e837f8b5e77afdb68519404b8fab82";
//                EthWalltUtils.queryEthBalance(address);
//            }
//        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean isLogin = (boolean) SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_LOGIN, false);
                if(!isLogin || !App.login) {
                    ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
                } else {
                    ARouter.getInstance().build(RouterPath.MAIN_ACTIVITY).navigation();
                }

                boolean takeOnGesturePwd = (boolean) SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_TAKE_ON_GESTURE_PWD, false);
                if(takeOnGesturePwd) {
                    startService(new Intent(StartActivity.this, ScreenListenerService.class));
                }
                finish();
            }
        }).start();
    }
}
