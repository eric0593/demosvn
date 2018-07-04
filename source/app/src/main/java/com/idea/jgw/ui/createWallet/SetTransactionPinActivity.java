package com.idea.jgw.ui.createWallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.btc.BtcWalltUtils;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.btc.model.TLAppDelegate;
import com.idea.jgw.logic.btc.model.TLCoin;
import com.idea.jgw.logic.btc.model.TLHDWalletWrapper;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.main.MainActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.view.PayPsdInputView;

import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.MnemonicCode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.idea.jgw.ui.login.LoginActivity.EXTRA_USER;
import static org.bitcoinj.core.Utils.HEX;

//生成钱包，设置密码
@Route(path = RouterPath.SET_TRANSACTION_PIN_ACTIVITY)
public class SetTransactionPinActivity extends BaseActivity implements PayPsdInputView.OnPasswordListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pivOfPassword.setOnPasswordListener(this);
        userPhone = getIntent().getStringExtra(EXTRA_USER);


//        if (App.isWalletDebug)
//            if (BtcWalltUtils.hasSetupHDWallet()) {
//                MToast.showLongToast("有钱包");
//            } else {
//                MToast.showLongToast("mei 钱包");
//            }
//

//        List<String> code = mc.toMnemonic(HEX.decode(entropy));

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_set_transcation_pin;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.create_wallet);
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
        //判断2次密码是否一致，如果一致，就创建钱包信息
        if (TextUtils.isEmpty(pwd)) {
            pwd = inputPsd;
            tvOfCreateStep.setText(R.string.ensure_transaction_pin);
            pivOfPassword.cleanPsd();
        } else if (pwd.equals(inputPsd)) {
//            testCreateBtcWallet();
            cretaeEthWallet();
        } else {
            MToast.showToast(R.string.input_not_equal);
            tvOfCreateStep.setText(R.string.set_transaction_pin);
            pivOfPassword.cleanPsd();
            pwd = "";
        }
    }


    //创建比特钱包（必须在以太创建成功后才能调用）
    private void testCreateBtcWallet() {

        String getPassphrase = BtcWalltUtils.getPassphrase();
    }


    private void cretaeEthWallet() {

        ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(this).get());
        if (storedwallets.size() > 0) {
            MToast.showLongToast("已经有钱包了");
        }

        String  s = BtcWalltUtils.getPassphrase();
        EthWalltUtils.createEthWallet2(SetTransactionPinActivity.this,s, new EthWalltUtils.CreateUalletCallback() {
            @Override
            public void onSuccess(String address) {
                ARouter.getInstance().build(RouterPath.WALLET_CREATE_SUCCESS_ACTIVITY).navigation();
                setResult(RESULT_OK);
                finish();
//                //存储pwd
//                SPreferencesHelper.getInstance(App.getInstance()).saveData(ShareKey.KEY_OF_GESTURE_PWD, pwd);
            }

            @Override
            public void onFaild() {
                MToast.showLongToast("创建钱包失败");
            }
        });
    }
}
