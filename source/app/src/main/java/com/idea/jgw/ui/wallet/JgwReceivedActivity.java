package com.idea.jgw.ui.wallet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.zxing.qrcode.CreateQRUtils;
import com.idea.jgw.App;
import com.idea.jgw.RouterPath;
import com.idea.jgw.common.Common;
import com.idea.jgw.utils.SPreferencesHelper;

import org.web3j.utils.Numeric;

/**
 * Created by vam on 2018\6\4 0004.
 */
@Route(path = RouterPath.RECEIVED_JGW_ACTIVITY)
public class JgwReceivedActivity extends WalletAddressActivity {


    String mCurAddress;
    String mCurAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mCurAddress = intent.getStringExtra(EXTRA_ADDRESS);
        mCurAmount = intent.getStringExtra(EXTRA_CUR_AMOUNT);

        String addressNoPrefix = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_ADDRESS_KEY, "").toString();
        if (addressNoPrefix.contains("0x")) {
            int index = addressNoPrefix.indexOf("0x");
            addressNoPrefix = addressNoPrefix.substring(index);
        }
        addressNoPrefix = addressNoPrefix + "&type=jgw";
        tvSendAddress.setText(addressNoPrefix);

        final int addressWeight = ivOfMyAddress.getMeasuredWidth();//图片的实际大小
        final int adressHeight = ivOfMyAddress.getMeasuredHeight();
        new Thread(new Runnable() {
            @Override
            public void run() {

                final Bitmap bitmap = CreateQRUtils.create2DCode(tvSendAddress.getText().toString(), addressWeight, adressHeight);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivOfMyAddress.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
    }
}
