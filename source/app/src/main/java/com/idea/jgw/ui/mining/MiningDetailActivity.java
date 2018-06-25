package com.idea.jgw.ui.mining;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.google.gson.reflect.TypeToken;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.ServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.bean.DigitalCurrency;
import com.idea.jgw.bean.MiningCoinData;
import com.idea.jgw.bean.PageData;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.mining.adapter.MiningDetailAdapter;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.ShareKey;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 挖矿详情页面
 */
@Route(path = RouterPath.MINING_DETAIL_ACTIVITY)
public class MiningDetailActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener {

    MiningDetailAdapter miningDetailAdapter;
    @BindView(R.id.iv_of_banner)
    ImageView ivOfBanner;
    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iv_digital_logo)
    ImageView ivDigitalLogo;
    @BindView(R.id.tv_balance_label)
    TextView tvBalanceLabel;
    @BindView(R.id.tv_digital_value)
    TextView tvDigitalValue;
    @BindView(R.id.tv_cny_value)
    TextView tvCnyValue;
    @BindView(R.id.btn_digital_description)
    Button btnDigitalDescription;
    @BindView(R.id.btn_of_send)
    Button btnOfSend;
    @BindView(R.id.rv_of_detail_asset)
    RecyclerView rvOfDetailAsset;

    float balance;//余额
    int coinType = 1;//0币种类型，1:btc ,2:eth ,3:8phc
    private Subscription miningSubscription;
    private Subscription transferMiningSubscription;
    private int page; //请求页码
    private int count; //总数量
    private int limit; //单页数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        miningDetailAdapter = new MiningDetailAdapter();
//        miningDetailAdapter.addDatas(getTestDatas(3));
        miningDetailAdapter.setOnItemClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvOfDetailAsset.setLayoutManager(layoutManager);
        rvOfDetailAsset.setAdapter(miningDetailAdapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_mining_detail;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.mining_detail);

        if (getIntent().hasExtra("coinType")) {
            coinType = getIntent().getIntExtra("coinType", 1);
        }
        if (getIntent().hasExtra("balance")) {
            balance = getIntent().getFloatExtra("balance", 0);
        }
        tvDigitalValue.setText(String.valueOf(balance));

        getMiningDetail();
    }

    private void getMiningDetail() {
        String token = SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_SESSION, "").toString();
        miningSubscription = ServiceApi.getInstance().getApiService()
                .miningList(coinType, token, page)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(this, getResources().getString(R.string.loading), true) {
                               @Override
                               protected void _onNext(BaseResponse baseResponse) {
                                   if (baseResponse.getCode() == 200) {
                                       Type type = new TypeToken<PageData<MiningCoinData>>(){}.getType();
                                       PageData<MiningCoinData> miningCoinDatas = JSON.parseObject(baseResponse.getData().toString(), type);
                                       miningCoinDatas.getCount();
                                       miningCoinDatas.getLimit();
                                       miningDetailAdapter.replaceDatas(miningCoinDatas.getList());
                                   } else if (baseResponse.getCode() == 0) {
                                       MToast.showToast(baseResponse.getData().toString());
                                   }
                               }

                               @Override
                               protected void _onError(String message) {
                                   MToast.showToast(message);
                               }
                           }
                );
    }

    @Override
    public void onItemClick(int position, Object data) {
    }

    public List getTestDatas(int size) {
        List<DigitalCurrency> digitalCurrencys = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            digitalCurrencys.add(new DigitalCurrency());
        }
        return digitalCurrencys;
    }

    @OnClick({R.id.btn_of_back, R.id.btn_digital_description, R.id.btn_of_send})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.btn_digital_description:
                break;
            case R.id.btn_of_send:
                ARouter.getInstance().build(RouterPath.SEND_MINING_COIN_ACTIVITY).navigation();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribe(miningSubscription);
    }
}
