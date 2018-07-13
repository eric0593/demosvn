package com.idea.jgw.ui.main;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.logic.btc.model.TLAppDelegate;
import com.idea.jgw.logic.btc.model.TLHDWalletWrapper;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.createWallet.SetTransactionPinActivity;
import com.idea.jgw.ui.createWallet.WalletCreateSuccessActivity;
import com.idea.jgw.ui.main.fragment.DiscoverFragment;
import com.idea.jgw.ui.main.fragment.MineFragment;
import com.idea.jgw.ui.main.fragment.WalletFragment;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;
import com.idea.jgw.utils.common.SharedPreferenceManager;

import org.bitcoinj.core.Base58;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 主页面
 */
@Route(path = RouterPath.MAIN_ACTIVITY)
public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    public static final int READ_EXTERNAL_STORAGE_CODE = 123;//硬盘读
    public static final int WRITE_EXTERNAL_STORAGE_CODE = 234;//硬盘写
    public static final int ACCESS_NETWORK_STATE_CODE = 345;//
    public static final int CHANGE_NETWORK_STATE_CODE = 456;
    public static final int CAMERA_CODE = 678;
    public static final int CHANGE_WIFI_STATE_CODE = 789;
    public static final int READ_LOGS_CDOE = 890;


    @BindView(R.id.home_container)
    FrameLayout homeContainer;
    @BindView(R.id.btn_of_wallet)
    RadioButton rbOfWallet;
    @BindView(R.id.btn_of_discovery)
    RadioButton rbOfDiscovery;
    @BindView(R.id.rb_of_mine)
    RadioButton rbOfMine;
    @BindView(R.id.radio_group_button)
    RadioGroup radioGroupButton;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;

    WalletFragment walletFragment;
    DiscoverFragment discoverFragment;
    MineFragment mineFragment;
    Fragment currentFragment;


    public static BigDecimal ethCount = new BigDecimal("0"); //
    public static BigDecimal jgwCount = new BigDecimal("0");//
    public static long lastGetCoinTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        cretaeEthWallet();

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        walletFragment = new WalletFragment();
        discoverFragment = new DiscoverFragment();
        mineFragment = new MineFragment();
        currentFragment = walletFragment;

        radioGroupButton.setOnCheckedChangeListener(this);
        rbOfWallet.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.btn_of_wallet:
                currentFragment = walletFragment;
                rbOfWallet.setSelected(true);
                getFragmentManager().beginTransaction().replace(R.id.home_container, currentFragment).commit();
                break;
            case R.id.btn_of_discovery:
                currentFragment = discoverFragment;
                rbOfDiscovery.setSelected(true);
                getFragmentManager().beginTransaction().replace(R.id.home_container, currentFragment).commit();
                break;
            case R.id.rb_of_mine:
                currentFragment = mineFragment;
                rbOfMine.setSelected(true);
                getFragmentManager().beginTransaction().replace(R.id.home_container, currentFragment).commit();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.e("main onDestroy");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    long lastBackTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBackTime < 2 * 1000) {
                finish();
            } else {
                MToast.showToast(R.string.quit_notice);
                lastBackTime = currentTime;
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}

