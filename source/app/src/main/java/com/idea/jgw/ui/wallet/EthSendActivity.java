package com.idea.jgw.ui.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.MToast;

import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;

/**
 * Created by vam on 2018\6\4 0004.
 */
@Route(path = RouterPath.SEND_ETH_ACTIVITY)
public class EthSendActivity extends SendActivity {


    String mCurAmount;
    String mWalletAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mWalletAddress = intent.getStringExtra(EXTRA_ADDRESS);
        mCurAmount = intent.getStringExtra(EXTRA_CUR_AMOUNT);

        tvOfBalance.setText(TextUtils.isEmpty(mCurAmount) ? "0.00" : mCurAmount);
        tvLight.setText("wei");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case QrSanActivity.REQ_CODE:
                    String qrString = data.getExtras().getString(QrSanActivity.EXTRA_RESULT_QR);
                    if (validAddress(qrString)) {
                        String addressNoPrefix = Numeric.cleanHexPrefix(qrString);
                        if (addressNoPrefix.contains("0x")) {
                            int index = addressNoPrefix.indexOf("0x") + 2;
                            addressNoPrefix = addressNoPrefix.substring(index);
                        }
                        etReceivedAddress.setText(addressNoPrefix);
                    }
                    break;
            }
        }
    }

    @Override
    public void onQrScanCodeClick() {
        super.onQrScanCodeClick();
        ARouter.getInstance()
                .build(RouterPath.QR_SCAN_ACTIVITY)
                .navigation(EthSendActivity.this, QrSanActivity.REQ_CODE);
    }

    @Override
    public boolean onSendClick() {
        //地址验证
        String address = etReceivedAddress.getText().toString();
        if (TextUtils.isEmpty(address)) {
            MToast.showLongToast(getResources().getString(R.string.address_empty));
            return false;
        }

        if (!validAddress(address)) {
            return false;
        }

        //金额验证
        String sendAmount = etSendAmount.getText().toString();
        if (TextUtils.isEmpty(sendAmount)) {
            MToast.showLongToast(getResources().getString(R.string.send_amount_empty));
            return false;
        }

        return true;
    }

    private boolean validAddress(String address) {
        if (!EthWalltUtils.isValidAddress(address)) {
            MToast.showLongToast(getResources().getString(R.string.address_wrong));
            return false;
        }
        return true;
    }

    @Override
    public void onPaswordInputFinished(String inputPsd) {
        super.onPaswordInputFinished(inputPsd);

        if (App.debug) {
            if (inputPsd.equals("123456")) {
//                String privateKey = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_PRIVET_KEY,"").toString();
                String pwd = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_PWD_KEY, "").toString();

                try {
                    final Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(getApplicationContext(), pwd, mWalletAddress);
                    EthWalltUtils.sendCoin(EthSendActivity.this, mWalletAddress, etReceivedAddress.getText().toString(), pwd, etSendAmount.getText().toString(), 7000000000l, 0);
                } catch (Exception e) {
                    MToast.showLongToast(getResources().getString(R.string.password_wrong));
                }
            } else {
                MToast.showLongToast(getResources().getString(R.string.password_wrong));
            }
        }
    }


    LocalRceiver mLocalReceiver;
    IntentFilter mIntentFileter;
    private LocalBroadcastManager mLocalBroadcastManager;


    @Override
    protected void onResume() {
        register();
        super.onResume();

    }

    private void register() {
        if (null == mLocalBroadcastManager)
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        if (null == mIntentFileter) {
            mIntentFileter = new IntentFilter();
            mIntentFileter.addAction(Common.Broadcast.SEND_ETH_RESULT); //发送以太的广播
        }

        if (null == mLocalReceiver)
            mLocalReceiver = new LocalRceiver();
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, mIntentFileter);
    }

    public class LocalRceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.Broadcast.SEND_ETH_RESULT.equals(intent.getAction())) {
                boolean result = intent.getBooleanExtra(Common.Broadcast.SEND_ETH_RESULT_DATA, false);
                MToast.showLongToast(getResources().getString(result ? R.string.send_eth_result_success : R.string.send_eth_result_fail));
            }
        }
    }

    @Override
    protected void onPause() {
        if (null != mLocalBroadcastManager && null != mLocalReceiver) {
            mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
        }

        super.onPause();
    }
}
