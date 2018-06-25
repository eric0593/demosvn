package com.idea.jgw.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.ui.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 关于我们
 */
@Route(path = RouterPath.ABOUT_ACTIVITY)
public class AboutUsActivity extends BaseActivity {

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.ll_version_log)
    LinearLayout llVersionLog;
    @BindView(R.id.ll_update)
    LinearLayout llUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_about_us;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.about_us);
    }

    @OnClick({R.id.btn_of_back, R.id.ll_version_log, R.id.ll_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.ll_version_log:
                ARouter.getInstance().build(RouterPath.SHOW_ACTIVITY).withInt("contentType", InfoActivity.UPDATE_LOG).navigation();
                break;
            case R.id.ll_update:
                break;
        }
    }
}
