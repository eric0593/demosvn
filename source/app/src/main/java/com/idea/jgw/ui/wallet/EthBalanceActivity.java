package com.idea.jgw.ui.wallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.btc.model.TLCoin;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.data.TransactionDisplay;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.network.EtherscanAPI;
import com.idea.jgw.logic.eth.utils.RequestCache;
import com.idea.jgw.logic.eth.utils.ResponseParser;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.ui.main.adapter.TransferRecordListAdapter;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jnr.ffi.annotations.TypeDefinition;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.view.View.GONE;
import static com.idea.jgw.ui.wallet.TransactionDetailActivity.EXTRA_DETAIL_OBJECT;

/**
 * Created by vam on 2018\6\4 0004.
 */
@Route(path = RouterPath.BALANCE_ETH_ACTIITY)
public class EthBalanceActivity extends BalanceActivity {

    public BigDecimal mCurAvailable;
    public String mCurAddress;

    protected List<TransactionDisplay> wallets = new ArrayList<>();
    protected int requestCount = 0;  // used to count to two (since internal and normal transactions are each one request). Gets icnreased once one request is finished. If it is two, notifyDataChange is called (display transactions)
    private long unconfirmed_addedTime;
    protected TransactionDisplay unconfirmed;

    public static final String EXTRA_AMOUNT = "EthBalanceActivity_EXTRA_AMOUNT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transferRecordListAdapter = new TransferRecordListAdapter();
        transferRecordListAdapter.addDatas(wallets);
        transferRecordListAdapter.setOnItemClickListener(this);
        rvOfTransferRecord.setAdapter(transferRecordListAdapter);

        ivOfLogo.setImageResource(R.mipmap.icon_eth);



        String balance = getIntent().getStringExtra(EXTRA_AMOUNT);
        if (!TextUtils.isEmpty(balance))
            tvOfUsableBalanceValue.setText(balance);

        String ethAddress = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_ADDRESS_KEY, "").toString();
        //钱包为空
        if (TextUtils.isEmpty(ethAddress)) {

        } else {

            //获取交易记录
            getTransactionRecord(false);

            //
            //这里需要优化下
            //当 String balance = getIntent().getStringExtra(EXTRA_AMOUNT);不为空时，不需要再去请求数据
            //获取姨太的金额
            getEthBanlance(ethAddress);
        }
    }

    @Override
    public void initView() {
        super.initView();
        tvOfTitle.setText(R.string.eth);
        ivOfLogo.setImageResource(R.mipmap.icon_eth);
    }

    private void getEthBanlance(String address) {
        mCurAddress = address;
        EthWalltUtils.getCurAvailable(EthBalanceActivity.this, mCurAddress,
                new TLCallback() {
                    @Override
                    public void onSuccess(Object obj) {
                        if (null == obj) return;
                        String str = obj.toString();
//                    BigInteger bi = new BigInteger(new BigInteger(str,16).toString(10));
                        BigInteger bi = new BigInteger(str);
                        BigDecimal bd = new BigDecimal(10).pow(18);
                        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
//                    df.setMaximumFractionDigits(18);
                        BigDecimal amount = new BigDecimal(bi.toString(10)).divide(bd);
//                        String balance = df.format(amount.doubleValue());

                        mCurAvailable = amount;

                        tvOfUsableBalanceValue.setText(String.valueOf(amount.doubleValue()));
                    }

                    @Override
                    public void onFail(Integer status, String error) {

                    }

                    @Override
                    public void onSetHex(String hex) {

                    }

                    @Override
                    public void onAmountMoveFromAccount(TLCoin amountMovedFromAccount) {

                    }
                });


//                new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Toast.makeText(EthBalanceActivity.this, "获取不到钱包信息", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) throws IOException {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            mCurAvailable = new BigDecimal(ResponseParser.parseBalance(response.body().string(), 18));
//                            tvOfUsableBalanceValue.setText(mCurAvailable.toString());
//                        } catch (Exception e) {
//                            Toast.makeText(EthBalanceActivity.this, "获取不到钱包信息", Toast.LENGTH_LONG).show();
////                                        ac.snackError("Cant fetch your account balance");
//                            e.printStackTrace();
//                        }
//                    }
//                });
//    }
//});
    }


    @Override
    public void onSendCoin() {
        ARouter.getInstance()
                .build(RouterPath.SEND_ETH_ACTIVITY)
                .withString(EthSendActivity.EXTRA_ADDRESS, (TextUtils.isEmpty(mCurAddress) ? "" : mCurAddress))
                .withString(EthSendActivity.EXTRA_CUR_AMOUNT, (null == mCurAvailable ? "" : mCurAvailable.toString()))
                .navigation(EthBalanceActivity.this, EthSendActivity.REQ_CODE);
    }

    @Override
    public void onReceivedCoin() {
        ARouter.getInstance().build(RouterPath.RECEIVED_ETH_ACTIVITY)
                .withString(EthReceivedActivity.EXTRA_ADDRESS, (TextUtils.isEmpty(mCurAddress) ? "" : mCurAddress))
                .withString(EthReceivedActivity.EXTRA_CUR_AMOUNT, (null == mCurAvailable ? "" : mCurAvailable.toString()))
                .navigation(EthBalanceActivity.this, EthReceivedActivity.REQ_CODE);
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


    /**
     * 获取交易信息
     *
     * @param force true：刷新（有加载过数据的时候），false 默认加载
     */
    private void getTransactionRecord(boolean force) {
        final ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(EthBalanceActivity.this).get());
        if (storedwallets.size() == 0) {
//            nothingToShow.setVisibility(View.VISIBLE);
//            onItemsLoadComplete();
        } else {
            for (int i = 0; i < storedwallets.size(); i++) {
                try {
                    final StorableWallet currentWallet = storedwallets.get(i);
                    String key = currentWallet.getPubKey();
                    EtherscanAPI.getInstance().getNormalTransactions(key, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onItemsLoadComplete();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            MyLog.e("response--->>" + restring);
                            if (restring != null && restring.length() > 2)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_NORMAL, currentWallet.getPubKey(), restring);
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.NORMAL));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onComplete(w, storedwallets);
                                }
                            });
                        }
                    }, force);
                    EtherscanAPI.getInstance().getInternalTransactions(currentWallet.getPubKey(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onItemsLoadComplete();
                                }
                            });

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            if (restring != null && restring.length() > 2)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_INTERNAL, currentWallet.getPubKey(), restring);
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.CONTRACT));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onComplete(w, storedwallets);
                                }
                            });

                        }
                    }, force);
                } catch (IOException e) {
                    // So "if(getRequestCount() >= storedwallets.size()*2)" limit can be reached even if there are expetions for certain addresses (2x because of internal and normal)
                    addRequestCount();
                    addRequestCount();
                    onItemsLoadComplete();
                    e.printStackTrace();
                }


            }
        }
    }

    private void onComplete(ArrayList<TransactionDisplay> w, ArrayList<StorableWallet> storedwallets) {
        addToWallets(w);
        addRequestCount();
        if (getRequestCount() >= storedwallets.size() * 2) {
            onItemsLoadComplete();

            // If transaction was send via App and has no confirmations yet (Still show it when users refreshes for 10 minutes)
            if (unconfirmed_addedTime + 10 * 60 * 1000 < System.currentTimeMillis()) // After 10 minutes remove unconfirmed (should now have at least 1 confirmation anyway)
                unconfirmed = null;
            if (unconfirmed != null && wallets.size() > 0) {
                if (wallets.get(0).getAmount() == unconfirmed.getAmount()) {
                    unconfirmed = null;
                } else {
                    wallets.add(0, unconfirmed);
                }
            }


            if (null != transferRecordListAdapter) {
                transferRecordListAdapter.replaceData(wallets);
            }
        }
    }

    public synchronized List<TransactionDisplay> getWallets() {
        return wallets;
    }

    public synchronized void addToWallets(List<TransactionDisplay> w) {
        wallets.addAll(w);
        Collections.sort(getWallets(), new Comparator<TransactionDisplay>() {
            @Override
            public int compare(TransactionDisplay o1, TransactionDisplay o2) {
                return o1.compareTo(o2);
            }
        });
    }

    public synchronized void addRequestCount() {
        requestCount++;
    }

    public synchronized int getRequestCount() {
        return requestCount;
    }


    void onItemsLoadComplete() {
//        if (swipeLayout == null) return;
//        swipeLayout.setRefreshing(false);
    }


    /**
     * 增加交易记录
     *
     * @param from
     * @param to
     * @param amount
     */
    public void addUnconfirmedTransaction(String from, String to, BigInteger amount) {
        unconfirmed = new TransactionDisplay(from, to, amount, 0, System.currentTimeMillis(), "", TransactionDisplay.NORMAL, "", "0", 0, 1, 1, false);
        unconfirmed.setCoinType(Common.CoinTypeEnum.ETH);
        unconfirmed_addedTime = System.currentTimeMillis();
        wallets.add(0, unconfirmed);
        transferRecordListAdapter.notifyDataSetChanged();
//        notifyDataSetChanged();
    }

    @Override
    public void onItemClick(int position, Object data) {

        TransactionDisplay td = (TransactionDisplay) data;
        ARouter.getInstance().build(RouterPath.TRANSACTION_DETAIL_ACTIVITY)
//                .withObject(EXTRA_DETAIL_OBJECT,data)
                .withSerializable(TransactionDetailActivity.EXTRA_DETAIL_OBJECT, td)
                .withInt(TransactionDetailActivity.EXTRA_COIN_TYPE, Common.CoinTypeEnum.ETH.getIndex())
                .navigation();
    }
}
