package com.idea.jgw.ui.createWallet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.logic.btc.BtcWalltUtils;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.btc.model.TLAppDelegate;
import com.idea.jgw.logic.btc.model.TLCoin;
import com.idea.jgw.logic.btc.model.TLHDWalletWrapper;
import com.idea.jgw.logic.btc.model.TLNotificationEvents;
import com.idea.jgw.logic.btc.utils.TLWalletUtils;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;
import com.joker.annotation.PermissionsDenied;
import com.joker.annotation.PermissionsGranted;
import com.joker.annotation.PermissionsRationale;
import com.joker.annotation.PermissionsRequestSync;

import org.bitcoinj.core.Base58;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static com.idea.jgw.ui.main.MainActivity.ACCESS_NETWORK_STATE_CODE;
import static com.idea.jgw.ui.main.MainActivity.CAMERA_CODE;
import static com.idea.jgw.ui.main.MainActivity.CHANGE_NETWORK_STATE_CODE;
import static com.idea.jgw.ui.main.MainActivity.CHANGE_WIFI_STATE_CODE;
import static com.idea.jgw.ui.main.MainActivity.READ_EXTERNAL_STORAGE_CODE;
import static com.idea.jgw.ui.main.MainActivity.READ_LOGS_CDOE;
import static com.idea.jgw.ui.main.MainActivity.WRITE_EXTERNAL_STORAGE_CODE;

@Route(path = RouterPath.INPUT_KEY_WORDS_ACTIVITY)
@PermissionsRequestSync(
        permission = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_LOGS
        },
        value = {READ_EXTERNAL_STORAGE_CODE,
                WRITE_EXTERNAL_STORAGE_CODE,
                READ_LOGS_CDOE
        })
public class InputKeyWordActivity extends BaseActivity {

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.tv_of_create_step)
    TextView tvOfCreateStep;
    @BindView(R.id.et_input_key_words)
    EditText etInputKeyWords;
    @BindView(R.id.btn_load)
    Button btnLoad;

//    TLAppDelegate appDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        appDelegate = TLAppDelegate.instance(App.getInstance());
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_input_key_word;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.create_wallet);
    }

    @OnClick({R.id.btn_of_back, R.id.btn_load})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                this.finish();
                break;
            case R.id.btn_load:

//                initWallet();

                final String passphrase = etInputKeyWords.getText().toString().trim();
                if (TextUtils.isEmpty(passphrase)) {
                    MToast.showLongToast(R.string.passphrase_err);
                    return;
                } else {

                    if (!BtcWalltUtils.phraseIsValid(InputKeyWordActivity.this,passphrase)) {
                        MToast.showLongToast(R.string.passphrase_err);
                    } else {

                        final ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(InputKeyWordActivity.this).get());
                        if (storedwallets.size() > 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(InputKeyWordActivity.this);
                            builder.setIcon(R.mipmap.icon_logo);
                            builder.setMessage(R.string.replace_wallet);
                            builder.setPositiveButton(getResources().getString(R.string.load),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            try{
                                                EthWalltUtils.delWallet(InputKeyWordActivity.this,storedwallets.get(0).getPubKey());
                                            }catch (Exception e){

                                            }
                                            MToast.showLongToast(R.string.recover_wallet_wait);
                                            recoverWallet(passphrase);
                                            dialog.cancel();
                                        }
                                    });
                            builder.setNeutralButton(getResources().getString(R.string.string_of_cancel),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                            builder.create().show();
                        } else {
                            recoverWallet(passphrase);
                            MToast.showLongToast(R.string.recover_wallet_wait);
                        }
                    }
                }


                break;
        }
    }


    private void recoverWallet(final String mnemonicPassphrase) {
        EthWalltUtils.createEthWallet2(InputKeyWordActivity.this, mnemonicPassphrase, new EthWalltUtils.CreateUalletCallback() {
            @Override
            public void onSuccess(String address) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ARouter.getInstance().build(RouterPath.MAIN_ACTIVITY).navigation();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFaild() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MToast.showLongToast(R.string.recover_faild);
                    }
                });
            }
        });
    }


    void handleAfterRecoverWallet() {
//        appDelegate.updateGodSend(TLWalletUtils.TLSendFromType.HDWallet, 0);
//        appDelegate.updateReceiveSelectedObject(TLWalletUtils.TLSendFromType.HDWallet, 0);
//        appDelegate.updateHistorySelectedObject(TLWalletUtils.TLSendFromType.HDWallet, 0);
//
//        appDelegate.saveWalletJson();
//
//        LocalBroadcastManager.getInstance(appDelegate.context).sendBroadcast(new Intent(TLNotificationEvents.EVENT_RESTORE_WALLET));
    }


    int hasPermissions = 0;

    @PermissionsGranted({READ_EXTERNAL_STORAGE_CODE, WRITE_EXTERNAL_STORAGE_CODE
    })
    @Override
    public void granted(int code) {
        switch (code) {
            case READ_EXTERNAL_STORAGE_CODE:
                hasPermissions++;
                break;
            case WRITE_EXTERNAL_STORAGE_CODE:
                hasPermissions++;
                break;
            default:
                break;
        }
        //6 是请求的权限总数
        if (hasPermissions != 2) {

        }
    }

    @PermissionsDenied({READ_EXTERNAL_STORAGE_CODE, WRITE_EXTERNAL_STORAGE_CODE
    })
    @Override
    public void denied(int code) {
        switch (code) {
            case READ_EXTERNAL_STORAGE_CODE:
                MToast.showLongToast(R.string.rquest_permission_read_storage);
                break;
            case WRITE_EXTERNAL_STORAGE_CODE:
                MToast.showLongToast(R.string.rquest_permission_write_storage);
                break;
            case READ_LOGS_CDOE:
                MToast.showLongToast(R.string.rquest_permission_write_storage);
                break;
            default:
                break;
        }
    }

    @PermissionsRationale({READ_EXTERNAL_STORAGE_CODE, WRITE_EXTERNAL_STORAGE_CODE
            , ACCESS_NETWORK_STATE_CODE, CHANGE_NETWORK_STATE_CODE, CHANGE_WIFI_STATE_CODE, CAMERA_CODE, READ_LOGS_CDOE
    })
    public void rationale(int code) {
        switch (code) {
            case READ_EXTERNAL_STORAGE_CODE:
                MToast.showLongToast(R.string.rquest_permission_read_storage);
                break;
            case WRITE_EXTERNAL_STORAGE_CODE:
                MToast.showLongToast(R.string.rquest_permission_write_storage);
                break;
            case ACCESS_NETWORK_STATE_CODE:
                MToast.showLongToast(R.string.rquest_permission_access_network_state);
                break;
            case CHANGE_NETWORK_STATE_CODE:
                MToast.showLongToast(R.string.rquest_permission_change_network_state);
                break;
            case CHANGE_WIFI_STATE_CODE:
                MToast.showLongToast(R.string.rquest_permission_change_wifi_state);
                break;
            case CAMERA_CODE:
                MToast.showLongToast(R.string.rquest_permission_camera);
                break;
            case READ_LOGS_CDOE:
                MToast.showLongToast(R.string.rquest_permission_log);
                break;
            default:
                break;
        }
    }




}
