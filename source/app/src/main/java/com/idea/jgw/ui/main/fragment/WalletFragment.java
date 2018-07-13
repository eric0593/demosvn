package com.idea.jgw.ui.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.bean.CoinData;
import com.idea.jgw.bean.CoinPrice;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.btc.model.TLCoin;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.jgw.JgwUtils;
import com.idea.jgw.ui.main.MainActivity;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.main.adapter.DigitalCurrencysAdapter;
import com.idea.jgw.ui.wallet.EthBalanceActivity;
import com.idea.jgw.ui.wallet.JgwBalanceActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.MyLog;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <p>钱包tab</p>
 * Created by idea on 2018/5/16.
 */

public class WalletFragment extends Fragment implements BaseRecyclerAdapter.OnItemClickListener<CoinData> {
    DigitalCurrencysAdapter digitalCurrencysAdapter;

    @BindView(R.id.tv_of_total_asset)
    TextView tvOfTotalAsset;
    @BindView(R.id.rv_of_detail_mining)
    RecyclerView rvOfDetailAsset;
    @BindView(R.id.tx_sum_money)
    TextView tvSumMoney;


    CoinPrice ethCoinPrice; //姨太的单价
    BigDecimal ethSumMoney = new BigDecimal("0"); //eth币的总价值
    //    BigDecimal sumMoney = new BigDecimal("0"); //所有币的重甲
    public static final String MONEY_TYPE = "CNY"; //法币符号

    HashMap<String, CoinData> map = new HashMap<>();

    MainActivity mContext;

    @Override
    public void onAttach(Context context) {
        mContext = (MainActivity) context;
        super.onAttach(context);
    }

    static final long DEALY_GET_COIN = 60 * 60 * 5;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getEthOfCnyPrice();

        digitalCurrencysAdapter = new DigitalCurrencysAdapter();
        digitalCurrencysAdapter.addDatas(getInitData());
        digitalCurrencysAdapter.setOnItemClickListener(this);

        //根据姨太的地址获取记录
        String ethAddress = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_ADDRESS_KEY, "").toString();
        if (!TextUtils.isEmpty(ethAddress) && EthWalltUtils.isValidAddress(ethAddress)) {

            //后期再优化--->>>需要持久化


//            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
//            {
//                CoinData cd = new CoinData();
//                cd.setAddress(ethAddress);
//                cd.setCoinTypeEnum(Common.CoinTypeEnum.JGW);
//                cd.setCount(df.format(MainActivity.jgwCount.doubleValue()));
//                addData(cd);
//            }
//
//            {
//                CoinData cd = new CoinData();
//                cd.setAddress(ethAddress);
//                cd.setCoinTypeEnum(Common.CoinTypeEnum.ETH);
//                cd.setCount(df.format(MainActivity.ethCount.doubleValue()));
//                addData(cd);
//            }


            getEthBalance(ethAddress);
            getJgwBalance(ethAddress);


        }
    }


    /**
     * 初始化数据
     *
     * @return
     */
    private List<CoinData> getInitData() {
        List<CoinData> list = new ArrayList<>();
        CoinData cd = new CoinData();
        cd.setCoinTypeEnum(Common.CoinTypeEnum.ETH);
        cd.setCount("0.0");
        CoinData cd2 = new CoinData();
        cd2.setCoinTypeEnum(Common.CoinTypeEnum.JGW);
        cd2.setCount("0.0");
        list.add(cd);
        list.add(cd2);
        map.put(cd.getCoinTypeEnum().getName(), cd);
        map.put(cd2.getCoinTypeEnum().getName(), cd2);
        return list;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_wallet, null);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvOfDetailAsset.setLayoutManager(layoutManager);
        rvOfDetailAsset.setAdapter(digitalCurrencysAdapter);
        return view;
    }

    @Override
    public void onItemClick(int position, CoinData data) {


        switch (data.getCoinTypeEnum()) {
            case BTC:
                ARouter.getInstance().build(RouterPath.BALANCE_BTC_ACTIITY)
//                    .withSerializable(BalanceActivity.EXTRA_COIN_TYPE, Coin.CoinType.BTC)
                        .navigation();
                break;
            case ETH:
                ARouter.getInstance().build(RouterPath.BALANCE_ETH_ACTIITY)
                        .withSerializable(EthBalanceActivity.EXTRA_AMOUNT, data.getCount())
                        .navigation();
                break;
            case JGW:
                ARouter.getInstance().build(RouterPath.BALANCE_JGW_ACTIITY)
                        .withSerializable(JgwBalanceActivity.EXTRA_AMOUNT, data.getCount())
                        .navigation();
                break;
        }
    }

    private void getJgwBalance(final String address) {
        String tempAddress;
        if (!address.startsWith("0x"))
            tempAddress = "0x" + address;
        else
            tempAddress = address;

        JgwUtils ju = new JgwUtils();
        ju.queryBalance(address, new TLCallback() {
            @Override
            public void onSuccess(Object obj) {
                MyLog.e("getJgwBalance___" + (obj == null));
                if (null != obj) {
                    String str = obj.toString();
                    BigInteger bi = new BigInteger(str);
                    BigDecimal bd = new BigDecimal(10).pow(18);
                    DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
                    BigDecimal amount = new BigDecimal(bi.toString(10)).divide(bd);
                    String balance = df.format(amount.doubleValue());
                    CoinData cd = new CoinData();
                    cd.setAddress(address);
                    cd.setCoinTypeEnum(Common.CoinTypeEnum.JGW);
                    cd.setCount(balance);
                    addData(cd);

                    MainActivity.jgwCount = amount;
                }
            }

            @Override
            public void onFail(Integer status, String error) {
                MyLog.e(status + "____" + error);
            }

            @Override
            public void onSetHex(String hex) {

            }

            @Override
            public void onAmountMoveFromAccount(TLCoin amountMovedFromAccount) {

            }
        });
    }

    private void getEthBalance(final String address) {
        String tempAddress;
        if (!address.startsWith("0x"))
            tempAddress = "0x" + address;
        else
            tempAddress = address;

        EthWalltUtils.getCurAvailable(mContext, tempAddress, new TLCallback() {
            @Override
            public void onSuccess(Object obj) {
                if (null == obj) return;
                String str = obj.toString();
                BigInteger bi = new BigInteger(str);
                BigDecimal bd = new BigDecimal(10).pow(18);
                DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
                BigDecimal amount = new BigDecimal(bi.toString(10)).divide(bd);
                String balance = String.valueOf(amount.doubleValue());
                CoinData cd = new CoinData();
                cd.setAddress(address);
                cd.setCoinTypeEnum(Common.CoinTypeEnum.ETH);
                cd.setCount(balance);
                addData(cd);

                MainActivity.ethCount = amount;
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

    }

    private void addData(CoinData cd) {

        if (ethCoinPrice != null) {
            if (cd.getCoinTypeEnum().equals(Common.CoinTypeEnum.ETH)) {
                cd.setPrice(ethCoinPrice);
//                sumEth = sumEth.subtract(sumEth);
                BigDecimal sumEth = getEthSumPrice(cd, ethCoinPrice);
//                sumMoney = sumMoney.add(sumEth);
                tvSumMoney.setText(sumEth.doubleValue() + MONEY_TYPE);
            }
        }

        map.replace(cd.getCoinTypeEnum().getName(), cd);
        List<CoinData> list = new ArrayList<>();
        for (CoinData data : map.values()) {
            list.add(data);
        }
        digitalCurrencysAdapter.replaceDatas(list);
    }


    private void getEthOfCnyPrice() {
        CoinPrice cp = new CoinPrice();
        cp.getCoinPrice("ETH", MONEY_TYPE, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what > 0) {
                    ethCoinPrice = (CoinPrice) msg.obj;
                    CoinData cd = map.get(Common.CoinTypeEnum.ETH.getName());
                    cd.setPrice(ethCoinPrice);
                    addData(cd);

                    BigDecimal bd = getEthSumPrice(cd, ethCoinPrice);
//                    sumMoney = sumMoney.add(bd);
                    tvSumMoney.setText(bd.doubleValue() + MONEY_TYPE);

                }
            }
        });

    }

    private BigDecimal getEthSumPrice(CoinData cd, CoinPrice cp) {
        String amount = cd.getCount();
        BigDecimal bd = new BigDecimal(amount);
        return bd.multiply(new BigDecimal(cp.getLast())).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
