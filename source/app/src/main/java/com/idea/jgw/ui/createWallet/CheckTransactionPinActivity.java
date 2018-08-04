package com.idea.jgw.ui.createWallet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.logic.btc.BtcWalltUtils;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.SharedPreferenceManager;
import com.idea.jgw.view.PayPsdInputView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static com.idea.jgw.ui.createWallet.InputKeyWordActivity.PASSPHRASE;
import static com.idea.jgw.ui.login.LoginActivity.EXTRA_USER;

//生成钱包，设置密码
@Route(path = RouterPath.CHECK_TRANSACTION_PIN_ACTIVITY)
public class CheckTransactionPinActivity extends TransactionPinActivity {
    public static final String CHECK_PWD = "check_pwd";

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.tv_of_create_step)
    TextView tvOfCreateStep;
    @BindView(R.id.piv_of_password)
    PayPsdInputView pivOfPassword;

    String pwd;

    String userPhone = "";
    private String passphrase;
    boolean checkPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra(CHECK_PWD)) {
            checkPwd = getIntent().getBooleanExtra(CHECK_PWD, false);
        }
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.hint_input_transaction_pin);
        tvOfCreateStep.setText(R.string.hint_input_transaction_pin);
        btnOfBack.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_of_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
        }
    }

    @Override
    public void inputFinished(String inputPsd) {
        if(SharedPreferenceManager.getInstance().getPaymentPwd().equals(inputPsd)) {
            ARouter.getInstance().build(RouterPath.MAIN_ACTIVITY).navigation();
            finish();
        } else {
            MToast.showToast(R.string.payment_pwd_invalid);
        }
    }
}
