package com.idea.jgw.ui.mining;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSON;
import com.google.gson.reflect.TypeToken;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.ServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.bean.CaculateRecord;
import com.idea.jgw.bean.CoinData;
import com.idea.jgw.bean.MiningCoinData;
import com.idea.jgw.bean.Nation;
import com.idea.jgw.bean.PageData;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.login.adapter.NationCodeAdapter;
import com.idea.jgw.ui.mining.adapter.HashrateRecordAdapter;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.ShareKey;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 算力提升列表
 */
@Route(path = RouterPath.HASHRATE_RECORD_ACTIVITY2)
public class HashrateRecordListActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener {

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.rv_of_nation_list)
    XRecyclerView rvOfNationList;

    private int page; //请求页码
    private int count; //总数量
    HashrateRecordAdapter hashrateRecordAdapter;
    private Subscription miningSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hashrateRecordAdapter = new HashrateRecordAdapter();
//        hashrateRecordAdapter.addDatas(getTestDatas(3));
        hashrateRecordAdapter.setOnItemClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvOfNationList.setLayoutManager(layoutManager);
        rvOfNationList.setAdapter(hashrateRecordAdapter);
    }

    public List getTestDatas(int size) {
        List<String> digitalCurrencys = new ArrayList<>();
        for (int i=0;i<size;i++) {
            digitalCurrencys.add("");
        }
        return digitalCurrencys;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_nation_list;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.hashrate_record);
        rvOfNationList.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                page = 0;
                count = 0;
                getMiningDetail(false);
            }

            @Override
            public void onLoadMore() {
                getMiningDetail(false);
            }
        });
        getMiningDetail(true);
    }

    private void getMiningDetail(boolean showDialog) {
        String token = SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_SESSION, "").toString();
        miningSubscription = ServiceApi.getInstance().getApiService()
                .calRecord(token, page)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(this, getResources().getString(R.string.loading), showDialog) {
                               @Override
                               protected void _onNext(BaseResponse baseResponse) {
                                   if (baseResponse.getCode() == BaseResponse.RESULT_OK) {
                                       Type type = new TypeToken<PageData<CaculateRecord>>(){}.getType();
                                       PageData<CaculateRecord> caculateRecordPageData = JSON.parseObject(baseResponse.getData().toString(), type);
                                       List<CaculateRecord> caculateRecords = caculateRecordPageData.getList();
                                       if(page == 0) {
                                           hashrateRecordAdapter.replaceDatas(caculateRecords);
                                           rvOfNationList.refreshComplete();
                                       } else {
                                           hashrateRecordAdapter.addDatas(caculateRecords);
                                           rvOfNationList.loadMoreComplete();
                                       }
                                       int totalCount = caculateRecordPageData.getCount();
                                       int size = caculateRecords.size();
                                       count += size;
                                       if(totalCount > count) {
                                           rvOfNationList.setNoMore(false);
                                       } else {
                                           rvOfNationList.setNoMore(true);
                                       }
                                       page++;
                                   } else if (baseResponse.getCode() == BaseResponse.INVALID_SESSION) {
                                       reLogin();
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

    @Override
    public void finish() {
        super.finish();
    }

    @OnClick(R.id.btn_of_back)
    public void onClick() {
        finish();
    }
}
