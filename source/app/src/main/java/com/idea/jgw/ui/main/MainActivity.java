package com.idea.jgw.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.idea.jgw.api.retrofit.OceApi;
import com.idea.jgw.api.retrofit.OceServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.bean.OceBaseResponse;
import com.idea.jgw.logic.btc.model.TLAppDelegate;
import com.idea.jgw.logic.btc.model.TLHDWalletWrapper;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.createWallet.SetTransactionPinActivity;
import com.idea.jgw.ui.createWallet.WalletCreateSuccessActivity;
import com.idea.jgw.ui.login.StartActivity;
import com.idea.jgw.ui.main.fragment.DiscoverFragment;
import com.idea.jgw.ui.main.fragment.MineFragment;
import com.idea.jgw.ui.main.fragment.WalletFragment;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.ui.service.ScreenListenerService;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;
import com.idea.jgw.utils.common.SharedPreferenceManager;

import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    static final String MINE_FRAGMENT_TAG = "mine";
    static final String WALLET_FRAGMENT_TAG = "wallet";
    static final String DISCOVER_FRAGMENT_TAG = "discover";


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
        startService(new Intent(this, ScreenListenerService.class));
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
//        currentFragment = discoverFragment;

        radioGroupButton.setOnCheckedChangeListener(this);
        rbOfDiscovery.setChecked(true);
//        radioGroupButton.check(R.id.btn_of_discovery);
//        showFragment(DISCOVER_FRAGMENT_TAG);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.btn_of_wallet:
                showFragment(WALLET_FRAGMENT_TAG);
//                currentFragment = walletFragment;
//                rbOfWallet.setSelected(true);
//                getFragmentManager().beginTransaction().replace(R.id.home_container, currentFragment).commit();
                break;
            case R.id.btn_of_discovery:
                showFragment(DISCOVER_FRAGMENT_TAG);
//                currentFragment = discoverFragment;
//                rbOfDiscovery.setSelected(true);
//                getFragmentManager().beginTransaction().replace(R.id.home_container, currentFragment).commit();
                break;
            case R.id.rb_of_mine:
                showFragment(MINE_FRAGMENT_TAG);
//                currentFragment = mineFragment;
//                rbOfMine.setSelected(true);
//                getFragmentManager().beginTransaction().replace(R.id.home_container, currentFragment).commit();
                break;
        }
    }

    private void showFragment(String tag) {
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentByTag(tag);
        FragmentTransaction transaction = manager.beginTransaction();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        if (fragment == null) {
            if (tag.equals(WALLET_FRAGMENT_TAG)) {
                fragment = walletFragment;
//                rbOfWallet.setChecked(true);
            } else if (tag.equals(DISCOVER_FRAGMENT_TAG)) {
                fragment = discoverFragment;
//                rbOfDiscovery.setChecked(true);
            } else if (tag.equals(MINE_FRAGMENT_TAG)) {
                fragment = mineFragment;
//                rbOfMine.setChecked(true);
            }
            transaction.add(R.id.home_container, fragment, tag).commit();
        } else {
//            if (tag.equals(CLASSIFY_FRAGMENT_TAG)) {
//                btnOfClassify.setSelected(true);
//            } else if (tag.equals(RANKING_FRAGMENT_TAG)) {
//                btnOfRanking.setSelected(true);
//            } else if (tag.equals(ALERT_FRAGMENT_TAG)) {
//                btnOfAlert.setSelected(true);
//            } else if (tag.equals(MINE_FRAGMENT_TAG)) {
//                rbOfUser.setSelected(true);
//            } else {
//                btnOfHome.setSelected(true);
//            }
            transaction.show(fragment).commit();
        }
        currentFragment = fragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, ScreenListenerService.class));
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

