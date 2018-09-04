package com.idea.jgw.ui.user;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.BaseAdapter;
import com.idea.jgw.ui.user.adapter.CrodAdapter;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by idea on 2018/8/28.
 */

@Route(path = RouterPath.CROWD_INFO_ACTIVITY)
public class CrowdInfoActivity extends BaseActivity {
    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;

    @Override
    public int getLayoutId() {
        return R.layout.activity_crowd_info;
    }

    @Override
    public void initView() {

    }

    public List getTestDatas(int size) {
        List<String> digitalCurrencys = new ArrayList<>();
        for (int i=0;i<size;i++) {
            digitalCurrencys.add("");
        }
        return digitalCurrencys;
    }

    @OnClick({R.id.btn_of_back,R.id.btn_support})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.btn_support:
                ARouter.getInstance().build(RouterPath.ENSURE_CROWD_ACTIVITY).navigation();
                break;
        }
    }

}
