package com.idea.jgw.ui.wallet;

import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSONObject;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.OceApi;
import com.idea.jgw.api.retrofit.OceServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.btc.model.TLCoin;
import com.idea.jgw.logic.eth.data.TransactionDisplay;
import com.idea.jgw.logic.jgw.JgwUtils;
import com.idea.jgw.service.GetSendStatusService;
import com.idea.jgw.service.MessageEvent;
import com.idea.jgw.ui.main.adapter.JgwTransferRecordListAdapter;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import jnr.ffi.annotations.In;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.idea.jgw.ui.main.fragment.WalletFragment.OCE_ADDRESS;
import static com.idea.jgw.ui.main.fragment.WalletFragment.OCE_PRIVATE_KEY;

/**
 * Created by vam on 2018\6\4 0004.
 * <p>
 * com.idea.jgw.ui.wallet.OceBalanceActivity
 */

@Route(path = RouterPath.BALANCE_OCE_ACTIITY)
public class OceBalanceActivity extends BalanceActivity {

    String address;
    String balance;
    String usable;

    JgwTransferRecordListAdapter transferRecordListAdapter;
    protected List<TransactionDisplay> wallets = new ArrayList<>();

    public static final String EXTRA_AMOUNT = "OceBalanceActivity_EXTRA_AMOUNT";
    public static final String EXTRA_USABLE = "OceBalanceActivity_EXTRA_USABLE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferRecordListAdapter = new JgwTransferRecordListAdapter();
        transferRecordListAdapter.addDatas(wallets);
        transferRecordListAdapter.setOnItemClickListener(this);
        rvOfTransferRecord.setAdapter(transferRecordListAdapter);
    }

    @Override
    public void sendCoinState(MessageEvent messageEvent) {
        if (isDestroyed() || isFinishing()) return;
        if (messageEvent.getCoinType() == Common.CoinTypeEnum.OCE && messageEvent.getState() == MessageEvent.STAE_SUCCES) {
            if (!isFinishing()) {
                getTX(address);
                getBalance(address);
            }
        }
    }

    @Override
    public void initView() {
        super.initView();

        tvOfTitle.setText(R.string.jgw);
        ivOfLogo.setImageResource(R.mipmap.oce);
        balance = getIntent().getStringExtra(OceBalanceActivity.EXTRA_AMOUNT);
        usable = getIntent().getStringExtra(OceBalanceActivity.EXTRA_USABLE);
        address = (String) SPreferencesHelper.getInstance(this).getData(OCE_ADDRESS, "");
        String privateKey = (String) SPreferencesHelper.getInstance(this).getData(OCE_PRIVATE_KEY, "");

        if (TextUtils.isEmpty(balance)) {
            getBalance(address);
        } else {
            tvOfUsableBalanceValue.setText(balance);

            try {
                int b = Integer.valueOf(balance);
                int u = Integer.valueOf(usable);
                if (b > u && b > 0) {
                    tvOfFrozenBalanceValue.setText(String.valueOf(b - u));
                }
            } catch (Exception e) {

            }
        }


        if (TextUtils.isEmpty(address)) {
            MToast.showLongToast("获取OCE地址失败");
            finish();
            return;
        }

        getTX(address);
        getBalance(address);
    }


    private void getTX(final String address) {

        OceServiceApi.getInstance(OceApi.URL).getApiService().gettranslist(address).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(OceBalanceActivity.this) {
                    @Override
                    protected void _onNext(BaseResponse response) {
                        wallets.clear();
                        com.alibaba.fastjson.JSONArray arr = (com.alibaba.fastjson.JSONArray) response.getInfo();
                        TransactionDisplay td;
                        int len = arr.size();
                        for (int i = 0; i < len; i++) {
                            td = new TransactionDisplay();
                            JSONObject obj = arr.getJSONObject(i);
                            String a = String.valueOf(obj.getIntValue("number"));
                            td.setToAddress(obj.getString("to_address"));
                            td.setFromAddress(obj.getString("from_address"));
                            td.setAmount(new BigInteger(String.valueOf(obj.getIntValue("number"))));
                            td.setDate(obj.getLong("trans_time") * 1000);
                            td.setTxHash(obj.getString("tx_id"));
                            td.setConfirmationStatus(13);
                            td.setAddress(address);
                            td.setBlock(obj.getLong("block"));
                            td.setCoinType(Common.CoinTypeEnum.OCE);
                            td.setBrokerage(obj.getString("brokerage"));
                            wallets.add(td);
                        }

                        transferRecordListAdapter.replaceData(wallets);
                    }

                    @Override
                    protected void _onError(String message) {
                        MyLog.e("xxx", message);
                    }
                });
    }


    private void getBalance(String address) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        OceServiceApi.getInstance(OceApi.URL).getApiService().getinfo(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(OceBalanceActivity.this) {
                    @Override
                    protected void _onNext(BaseResponse response) {
                        com.alibaba.fastjson.JSONObject obj = (com.alibaba.fastjson.JSONObject) response.getInfo();
                        Integer num = obj.getInteger("Number");
                        Integer usa = obj.getInteger("Usable");
                        balance = String.valueOf(num);
                        usable = String.valueOf(usa);

                        tvOfUsableBalanceValue.setText(balance);
                        if (num > usa && usa > 0) {
                            tvOfFrozenBalanceValue.setText(String.valueOf(num - usa));
                        }
                    }

                    @Override
                    protected void _onError(String message) {
                        MyLog.e("xxx", message);
                    }
                });
    }

    @Override
    public void onSendCoin() {
        ARouter.getInstance()
                .build(RouterPath.SEND_OCE_ACTIVITY)
                .withString(JgwSendActivity.EXTRA_BALANCE_KEY, balance)
                .withString(JgwReceivedActivity.EXTRA_ADDRESS, address)
                .navigation(OceBalanceActivity.this, EthSendActivity.REQ_CODE);
    }

    @Override
    public void onReceivedCoin() {
        ARouter.getInstance().build(RouterPath.RECEIVED_OCE_ACTIVITY)
                .withString(WalletAddressActivity.EXTRA_ADDRESS, address)
                .navigation(OceBalanceActivity.this, EthReceivedActivity.REQ_CODE);
    }


    @Override
    public void onItemClick(int position, Object data) {
        TransactionDisplay td = (TransactionDisplay) data;
        ARouter.getInstance().build(RouterPath.TRANSACTION_DETAIL_ACTIVITY)
//                .withObject(EXTRA_DETAIL_OBJECT,data)
                .withSerializable(TransactionDetailActivity.EXTRA_DETAIL_OBJECT, td)
                .withInt(TransactionDetailActivity.EXTRA_COIN_TYPE, Common.CoinTypeEnum.OCE.getIndex())
                .navigation();
    }

}
