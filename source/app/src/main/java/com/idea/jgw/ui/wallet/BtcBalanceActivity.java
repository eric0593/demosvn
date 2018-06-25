package com.idea.jgw.ui.wallet;

import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.RouterPath;

/**
 * Created by vam on 2018\6\4 0004.
 */
@Route(path = RouterPath.BALANCE_BTC_ACTIITY)
public class BtcBalanceActivity extends BalanceActivity {

    @Override
    public void onSendCoin() {

    }

    @Override
    public void onReceivedCoin() {
        ARouter.getInstance().build(RouterPath.RECEIVED_BTC_ACTIVITY)
                .navigation(BtcBalanceActivity.this, EthReceivedActivity.REQ_CODE);
    }
}
