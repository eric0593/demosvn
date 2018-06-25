package com.idea.jgw.ui.main.fragment;

import android.content.Context;
import android.os.Bundle;
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
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.btc.model.TLCoin;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.jgw.JgwUtils;
import com.idea.jgw.ui.main.MainActivity;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.main.adapter.DigitalCurrencysAdapter;
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


//    List<CoinData> initList = new ArrayList<>();

    HashMap<String, CoinData> map = new HashMap<>();

    MainActivity mContext;

    @Override
    public void onAttach(Context context) {
        mContext = (MainActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        digitalCurrencysAdapter = new DigitalCurrencysAdapter();
        digitalCurrencysAdapter.addDatas(getInitData());
        digitalCurrencysAdapter.setOnItemClickListener(this);

        //根据姨太的地址获取记录
        String ethAddress = SPreferencesHelper.getInstance(App.getInstance()).getData(Common.Eth.PREFERENCES_ADDRESS_KEY, "").toString();
        if (!TextUtils.isEmpty(ethAddress) && EthWalltUtils.isValidAddress(ethAddress)) {
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
//                    .withSerializable(BalanceActivity.EXTRA_COIN_TYPE, Coin.CoinType.ETH)
                        .navigation();
                break;
            case JGW:
                ARouter.getInstance().build(RouterPath.BALANCE_JGW_ACTIITY)
//                    .withSerializable(BalanceActivity.EXTRA_COIN_TYPE, Coin.CoinType.JGW)
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
//                    BigInteger bi = new BigInteger(new BigInteger(str,16).toString(10));
                BigInteger bi = new BigInteger(str);
                BigDecimal bd = new BigDecimal(10).pow(18);
                DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
//                    df.setMaximumFractionDigits(18);
                BigDecimal amount = new BigDecimal(bi.toString(10)).divide(bd);
                String balance = df.format(amount.doubleValue());

                CoinData cd = new CoinData();
                cd.setAddress(address);
                cd.setCoinTypeEnum(Common.CoinTypeEnum.ETH);
                cd.setCount(balance);
                addData(cd);
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
//                MyLog.e("____" + e.toString());
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) throws IOException {
//                MyLog.e("getEthBalance____1");
//                // MyLog.e("getEthBalance____" + response.body().string());
//                if(null != mContext)
//                mContext.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//
//                            if (response == null) {
//                                MyLog.e("response--->response null");
//                                return;
//                            }
//
//                            ResponseBody body = response.body();
//                            if (null == body) {
//                                MyLog.e("response--->body null");
//                                return;
//                            }
//                            BigDecimal mCurAvailable = new BigDecimal(ResponseParser.parseBalance(body.string(), 18));
//                            CoinData cd = new CoinData();
//                            cd.setAddress(address);
//                            cd.setCoinTypeEnum(Common.CoinTypeEnum.ETH);
//                            cd.setCount(mCurAvailable.toString());
//                            addData(cd);
//                        } catch (Exception e) {
//
//                            e.printStackTrace();
//                        }
//                    }
//                }
//        );
//            }
//        });
    }

    private void addData(CoinData cd) {
        map.replace(cd.getCoinTypeEnum().getName(), cd);
        List<CoinData> list = new ArrayList<>();
        for (CoinData data : map.values()) {
            list.add(data);
        }
        digitalCurrencysAdapter.replaceDatas(list);
    }
}
