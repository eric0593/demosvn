package com.idea.jgw.ui.mining;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.ServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.dialog.InputTransactionPwdDialog;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.view.PayPsdInputView;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 发送挖矿所得页面
 */
@Route(path = RouterPath.SEND_MINING_COIN_ACTIVITY)
public class SendMiningCoinActivity extends BaseActivity {

    private static final int INPUT_PASSWORD = 22;
    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iv_digital_logo)
    ImageView ivDigitalLogo;
    @BindView(R.id.tv_balance_label)
    TextView tvBalanceLabel;
    @BindView(R.id.et_received_address)
    EditText etReceivedAddress;
    @BindView(R.id.iv_of_delete)
    ImageView ivOfDelete;
    @BindView(R.id.iv_of_scan_code)
    ImageView ivOfScanCode;
    @BindView(R.id.et_send_amount)
    EditText etSendAmount;
    @BindView(R.id.btn_of_send)
    Button btnOfSend;
    @BindView(R.id.tv_of_balance)
    TextView tvOfBalance;

    private int coinType;
    private Subscription transferMiningSubscription;
    private double balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_send_mining_coin;
    }

    @Override
    public void initView() {

        if (getIntent().hasExtra("coinType")) {
            coinType = getIntent().getIntExtra("coinType", 1);
        }
        if (getIntent().hasExtra("balance")) {
            balance = getIntent().getDoubleExtra("balance", 0);
        }

        tvOfTitle.setText(R.string.send);
        if(coinType == 1) {
            ivDigitalLogo.setImageResource(R.mipmap.icon_btc);
        } else if(coinType == 2) {
            ivDigitalLogo.setImageResource(R.mipmap.icon_eth);
        } else if(coinType == 3) {
            ivDigitalLogo.setImageResource(R.mipmap.icon_oce);
        }
    }

    @OnClick({R.id.btn_of_back, R.id.iv_of_delete, R.id.iv_of_scan_code, R.id.btn_of_send})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.iv_of_delete:
                break;
            case R.id.iv_of_scan_code:
                break;
            case R.id.btn_of_send:
                showPwdInputDialog();
                break;
        }
    }

    InputTransactionPwdDialog inputTransactionPwdDialog;

    private void showPwdInputDialog() {
        inputTransactionPwdDialog = new InputTransactionPwdDialog(this, new PayPsdInputView.OnPasswordListener() {
            @Override
            public void inputFinished(String inputPsd) {
                inputTransactionPwdDialog.dismiss();
                if(inputPsd.equals("123456")) {
                    transferMining(inputPsd, Double.parseDouble(etSendAmount.getText().toString()), etReceivedAddress.getText().toString());
                } else {
                    inputTransactionPwdDialog.setErrorMsg(getResources().getString(R.string.transaction_pin_error));
                }
            }
        });
        inputTransactionPwdDialog.show();
    }

    private void transferMining(String pwd, double num, String addr) {
        transferMiningSubscription = ServiceApi.getInstance().getApiService()
                .transferMiningData(coinType, pwd, num, addr)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(this, getResources().getString(R.string.loading), true) {
                               @Override
                               protected void _onNext(BaseResponse baseResponse) {
                                   if (baseResponse.getCode() == BaseResponse.RESULT_OK) {
                                       MToast.showToast(baseResponse.getData().toString());
                                   } else if (baseResponse.getCode() == BaseResponse.INVALID_SESSION) {
                                       reLogin();
                                       MToast.showToast(baseResponse.getData().toString());
                                   }
                               }

                               @Override
                               protected void _onError(String message) {
                                   MToast.showToast(message);
                               }
                           }
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribe(transferMiningSubscription);
    }
}
