package com.idea.jgw.ui.mining;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.bean.CoinData;
import com.idea.jgw.bean.Nation;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.login.adapter.NationCodeAdapter;
import com.idea.jgw.ui.mining.adapter.HashrateRecordAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

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
    RecyclerView rvOfNationList;

    HashrateRecordAdapter hashrateRecordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hashrateRecordAdapter = new HashrateRecordAdapter();
        hashrateRecordAdapter.addDatas(getTestDatas(3));
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
