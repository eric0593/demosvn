package com.idea.jgw.logic.jgw;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.idea.jgw.App;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.eth.data.TransactionDisplay;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.MyLog;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import rx.functions.Action1;

public class JgwUtils {


    /**
     * 查询余额
     *
     * @param address
     * @param callback
     */
    public void queryEthBalance(final String address, final TLCallback callback) {


        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0) {
                    callback.onSuccess(msg.obj);
                } else {
                    callback.onFail(-1, null);
                }
            }
        };


        new Thread(new Runnable() {
            @Override
            public void run() {
                String tokenAddress = Common.Jgw.SMART_CONTRACT;//智能合约地址
                Web3j web3j = Web3jFactory.build(new HttpService(Common.Jgw.URL));
                String methodHex = "0x70a08231"; //查询余额
                String addressHex = address.replace("0x", "");
                String dataHex = methodHex + StringUtils.leftPad(addressHex, 64, '0');
                org.web3j.protocol.core.methods.request.Transaction etherTransaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(address, tokenAddress, dataHex);
                try {
                    org.web3j.protocol.core.methods.response.EthCall r = web3j.ethCall(etherTransaction, DefaultBlockParameterName.LATEST).send();
                    if (null == r.getResult()) {
                        handler.sendEmptyMessage(-1);
                        return;
                    }

                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = r.getResult();
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    handler.sendEmptyMessage(-1);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void sendCoin(final String toAddress, final String amont, final TLCallback callback) {
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    callback.onSuccess(msg.obj);
                } else {
                    callback.onFail(-1, null);
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                String tokenAddress = Common.Jgw.SMART_CONTRACT;//智能合约地址
                Web3j web3j = Web3jFactory.build(new HttpService(Common.Jgw.URL));
                try {
                    String password = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_PWD_KEY, "").toString();
                    String filePath = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.FILE_DIR, "").toString();
                    String fileName = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.FILE_NAME, "").toString();
                    File file = new File(filePath, fileName);
                    Credentials credentials = WalletUtils.loadCredentials(password, file.getPath());
                    OCEToken load = OCEToken.load(tokenAddress, web3j, credentials,
                            web3j.ethGasPrice().send().getGasPrice(), //price
                            new BigInteger("1000000") //limiet
                    );
                    Future<TransactionReceipt> transactionReceiptFuture =   load.transfer(toAddress, new BigInteger(amont)).sendAsync();
                    String hash = transactionReceiptFuture.get().getBlockHash();
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.obj = hash;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    handler.sendEmptyMessage(-1);
                    e.printStackTrace();
                }
            }
        }).start();


    }


    /**
     * 查询交易
     *
     * @param callback
     */
    public void queryTX(final TLCallback callback) {

        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    callback.onSuccess(msg.obj);
                } else {
                    callback.onFail(-1, null);
                }
            }
        };


        new Thread(new Runnable() {
            @Override
            public void run() {

                String tokenAddress = Common.Jgw.SMART_CONTRACT;//智能合约地址
                final List<TransactionDisplay> list = new ArrayList<>();
                Web3j build = Web3jFactory.build(new HttpService(Common.Jgw.URL));
                String password = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_PWD_KEY, "").toString();
                String filePath = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.FILE_DIR, "").toString();
                String fileName = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.FILE_NAME, "").toString();
                File file = new File(filePath, fileName);
                Credentials credentials = null;
                try {

                    credentials = WalletUtils.loadCredentials(password, file.getPath());
                    OCEToken load = OCEToken.load(tokenAddress, build, credentials, new BigInteger("18000000000"),
                            new BigInteger("1000000"));

//                    inal Action1<? super T> onNext
                    load.transferEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                            .forEach(new Action1<OCEToken.TransferEventResponse>() {
                                @Override
                                public void call(OCEToken.TransferEventResponse transferEventResponse) {
                                    TransactionDisplay td = new TransactionDisplay(transferEventResponse._from, transferEventResponse._to, transferEventResponse._value);
                                    list.add(td);
                                    Message msg = handler.obtainMessage();
                                    msg.obj = list;
                                    handler.sendMessage(msg);

                                    MyLog.e(transferEventResponse._value + "");
                                    MyLog.e(transferEventResponse._from + "");
                                    MyLog.e(transferEventResponse._to + "");
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
