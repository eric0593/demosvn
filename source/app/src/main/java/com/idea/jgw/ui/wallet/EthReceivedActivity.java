package com.idea.jgw.ui.wallet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.zxing.qrcode.QRCodeUtil;
import com.idea.jgw.RouterPath;
import com.idea.jgw.utils.DisplayUtils;

import org.web3j.utils.Numeric;

/**
 * 接受以太
 *
 * Created by vam on 2018\6\4 0004.
 */
@Route(path = RouterPath.RECEIVED_ETH_ACTIVITY)
public class EthReceivedActivity extends WalletAddressActivity {


    String mCurAddress;
    String mCurAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mCurAddress = intent.getStringExtra(EXTRA_ADDRESS);
        mCurAmount = intent.getStringExtra(EXTRA_CUR_AMOUNT);

        String addressNoPrefix = Numeric.cleanHexPrefix(mCurAddress);
        if (addressNoPrefix.contains("0x")) {
            int index = addressNoPrefix.indexOf("0x");
            addressNoPrefix = addressNoPrefix.substring(index);
        }

        final String address = "iban:"+addressNoPrefix+"?amount=0&token=eth";

        tvSendAddress.setText(addressNoPrefix);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int height = DisplayUtils.dp2px(EthReceivedActivity.this,162);
                try{
                    final Bitmap bitmap = QRCodeUtil.createQRImage(address, height, height,null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivOfMyAddress.setImageBitmap(bitmap);
                        }
                    });
                }catch (Exception e ){

                }
            }
        }).start();
    }
}
