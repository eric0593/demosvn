package com.idea.jgw.ui.wallet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.dialog.InputTransactionPwdDialog;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.view.PayPsdInputView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 发送转账页面
 */
@Route(path = RouterPath.SEND_ACTIVITY)
public class SendActivity extends BaseActivity {


    /***
     * 地址
     */
    public static final String EXTRA_ADDRESS = "EthSendActivity.EXTRA_ADDRESS";
    /***
     * 金额
     */
    public static final String EXTRA_CUR_AMOUNT = "EthSendActivity.EXTRA_CUR_AMOUNT";
    /***
     * 请求code
     */
    public static final int REQ_CODE = 234 * 43;


    private static final int INPUT_PASSWORD = 22;
    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iv_digital_logo)
    ImageView ivDigitalLogo;
    @BindView(R.id.tv_balance_label)
    TextView tvBalanceLabel; //余额
    @BindView(R.id.et_received_address)
    EditText etReceivedAddress; //地址
    @BindView(R.id.iv_of_delete)
    ImageView ivOfDelete;//清空输入孔
    @BindView(R.id.iv_of_scan_code)
    ImageView ivOfScanCode; //扫描二维码
    @BindView(R.id.et_send_amount)
    EditText etSendAmount; //金额
    @BindView(R.id.btn_of_send)
    Button btnOfSend;//确认发送
    //    @BindView(R.id.txt_balance_amount)
//    TextView txtBalanceAmount; //余额
    @BindView(R.id.tv_light)
    TextView tvLight;
    @BindView(R.id.tv_of_balance)
    TextView tvOfBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_send;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.send);
    }

    @OnClick({R.id.btn_of_back, R.id.iv_of_delete, R.id.iv_of_scan_code, R.id.btn_of_send})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.iv_of_delete: //清空输入孔
                etReceivedAddress.setText("");
                break;
            case R.id.iv_of_scan_code: //扫描二维码
                onQrScanCodeClick();
                break;
            case R.id.btn_of_send: //确认发送
                if (onSendClick()) {
                    showPwdInputDialog();
                }
                break;
        }
    }

    InputTransactionPwdDialog inputTransactionPwdDialog;

    //密码输入框
    protected void showPwdInputDialog() {
        inputTransactionPwdDialog = new InputTransactionPwdDialog(this, new PayPsdInputView.OnPasswordListener() {
            @Override
            public void inputFinished(String inputPsd) {
                //密码输入完成的操作
                onPaswordInputFinished(inputPsd);
                if (null != inputTransactionPwdDialog)
                    inputTransactionPwdDialog.dismiss();
//                if (inputPsd.equals("123456")) {
//                    MToast.showToast(inputPsd);
//                } else {
//                    inputTransactionPwdDialog.setErrorMsg(getResources().getString(R.string.transaction_pin_error));
//                }
            }
        });
        inputTransactionPwdDialog.show();
    }

    /**
     * 扫描按钮的点击事件
     */
    public void onQrScanCodeClick() {

    }

    /**
     * 发送按钮点击的事件
     *
     * @return true 弹出密码框 false 不弹出密码框
     */
    public boolean onSendClick() {
        return false;
    }

    /**
     * 密码输入完成的操作
     *
     * @param inputPsd
     */
    public void onPaswordInputFinished(String inputPsd) {

    }
}
