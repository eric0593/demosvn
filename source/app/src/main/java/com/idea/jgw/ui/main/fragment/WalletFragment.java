package com.idea.jgw.ui.main.fragment;

import android.content.Intent;
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
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.bean.CoinData;
import com.idea.jgw.common.Common;
import com.idea.jgw.logic.btc.interfaces.TLCallback;
import com.idea.jgw.logic.btc.model.TLCoin;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.utils.ResponseParser;
import com.idea.jgw.logic.jgw.JgwUtils;
import com.idea.jgw.ui.wallet.BalanceActivity;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.main.adapter.DigitalCurrencysAdapter;
import com.idea.jgw.ui.wallet.EthBalanceActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.MyLog;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * <p>钱包tab</p>
 * Created by idea on 2018/5/16.
 */

public class WalletFragment extends Fragment implements BaseRecyclerAdapter.OnItemClickListener<CoinData> {
    DigitalCurrencysAdapter digitalCurrencysAdapter;

    @BindView(R.id.tv_of_total_asset)
    TextView tvOfTotalAsset;
    @BindView(R.id.rv_of_detail_asset)
    RecyclerView rvOfDetailAsset;


    List<CoinData> initList = new ArrayList<>();

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
        CoinData cd = new CoinData();
        cd.setCoinTypeEnum(Common.CoinTypeEnum.ETH);
        cd.setCount("0.0");
        initList.add(cd);
        CoinData cd2 = new CoinData();
        cd2.setCoinTypeEnum(Common.CoinTypeEnum.JGW);
        cd2.setCount("0.0");
        initList.add(cd2);
        return initList;
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
        ju.queryEthBalance(address, new TLCallback() {
            @Override
            public void onSuccess(Object obj) {
                MyLog.e("getJgwBalance___" + (obj == null));
                if (null != obj) {
                    String balance = obj.toString();
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

        EthWalltUtils.getCurAvailable(tempAddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MyLog.e("____" + e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                MyLog.e("getEthBalance____1");
                // MyLog.e("getEthBalance____" + response.body().string());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BigDecimal mCurAvailable = new BigDecimal(ResponseParser.parseBalance(response.body().string(), 18));
                            CoinData cd = new CoinData();
                            cd.setAddress(address);
                            cd.setCoinTypeEnum(Common.CoinTypeEnum.ETH);
                            cd.setCount(mCurAvailable.toString());
                            addData(cd);
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void addData(CoinData cd) {

        int index = -1;
        CoinData data;
        tagFor:
        for (int i = 0; i < initList.size(); i++) {
            data = initList.get(i);
            if (data.getCoinTypeEnum() == Common.CoinTypeEnum.ETH) {
                index = i;
                break tagFor;
            }
        }

        if (index == -1) return;

        initList.remove(index);
        initList.add(index, cd);
        digitalCurrencysAdapter.replaceDatas(initList);
    }
}
