package com.idea.jgw.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.logic.ic.EncryptUtils;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.service.ScreenListenerService;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.SharedPreferenceManager;

import java.util.List;

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
                boolean isLogin = SharedPreferenceManager.getInstance().isLogin();
                List<StorableWallet> list = WalletStorage.getInstance(App.getInstance()).get();
                boolean hasEthWallet;
                if(EncryptUtils.isJGWBrand()) {
                    hasEthWallet = !TextUtils.isEmpty(SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_PUBLIC_KEY, "").toString());
                } else {
                    hasEthWallet = list.size() > 0;
                }
                if(!isLogin || !App.login) {
                    ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
                } else if(!hasEthWallet) {
                    ARouter.getInstance().build(RouterPath.LOAD_OR_CREATE_WALLET_ACTIVITY).navigation();
                } else {
                    ARouter.getInstance().build(RouterPath.CHECK_TRANSACTION_PIN_ACTIVITY).navigation();
                }

//                boolean takeOnGesturePwd = SharedPreferenceManager.getInstance().isTakeOnGesturePwd();
//                if(takeOnGesturePwd) {
//                    startService(new Intent(StartActivity.this, ScreenListenerService.class));
//                }
                finish();
            }
        }).start();
    }
}
