package com.idea.jgw.ui.wallet;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.bean.Coin;
import com.idea.jgw.bean.TransferRecord;
import com.idea.jgw.logic.eth.EthWalltUtils;
import com.idea.jgw.logic.eth.interfaces.StorableWallet;
import com.idea.jgw.logic.eth.utils.ResponseParser;
import com.idea.jgw.logic.eth.utils.WalletStorage;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.main.adapter.TransferRecordListAdapter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;

@Route(path = RouterPath.BALANCE_ACTIVITY)
/**
 * 钱包信息
 */
public abstract class BalanceActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener {

    TransferRecordListAdapter transferRecordListAdapter;
    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iv_of_logo)
    ImageView ivOfLogo;
    @BindView(R.id.tv_of_usable_balance_tag)
    TextView tvOfUsableBalanceTag;
    @BindView(R.id.tv_of_usable_balance_value)
    TextView tvOfUsableBalanceValue; //币数量
    @BindView(R.id.tv_of_usable_cny_value)
    TextView tvOfUsableCnyValue;//币兑换成法币
    @BindView(R.id.tv_of_frozen_balance_tag)
    TextView tvOfFrozenBalanceTag;
    @BindView(R.id.tv_of_frozen_balance_value)
    TextView tvOfFrozenBalanceValue;//冻结的币
    @BindView(R.id.rv_of_transfer_record)
    RecyclerView rvOfTransferRecord; //
    @BindView(R.id.iv_of_divide_line)
    ImageView ivOfDivideLine;
    @BindView(R.id.btn_of_send)
    Button btnOfSend; //发送币
    @BindView(R.id.btn_of_received)
    Button btnOfReceived; //接收币

    public static final String EXTRA_COIN_TYPE = "BalanceActivity.EXTRA_COIN_TYPE"; //币类型

    //    @Autowired(name = EXTRA_COIN_TYPE)
    public Coin.CoinType mCoinType;

    int coinType;//0币种类型，1:btc ,2:eth ,3:8phc
    private Subscription miningSubscription;
    private int page; //请求页码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this);

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_balance;
    }

    @Override
    public void initView() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvOfTransferRecord.setLayoutManager(layoutManager);

        if (getIntent().hasExtra("coinType")) {
            coinType = getIntent().getIntExtra("coinType", 1);
        }

    }

    public List getTestDatas(int size) {
        List<TransferRecord> digitalCurrencys = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            digitalCurrencys.add(new TransferRecord());
        }
        return digitalCurrencys;
    }

    @OnClick({R.id.btn_of_back, R.id.btn_of_send, R.id.btn_of_received})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                this.finish();
                break;
            case R.id.btn_of_send: //发送
                onSendCoin();
                break;
            case R.id.btn_of_received://接受
                onReceivedCoin();
                break;
        }
    }


    /**
     * 发送币
     */
    public abstract void onSendCoin();

    /**
     * 接受币
     */
    public abstract void onReceivedCoin();


    @Override
    public void onItemClick(int position, Object data) {
//        ARouter.getInstance().build(RouterPath.TRANSACTION_DETAIL_ACTIVITY).navigation();
    }

}
