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
 * 帮助中心
 */
@Route(path = RouterPath.HELP_ACTIVITY)
public class HelpActivity extends BaseActivity {

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.tv_of_right)
    TextView tvOfRight;
    @BindView(R.id.ll_how_to_load)
    LinearLayout llHowToLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_help;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.help);
    }

    @OnClick({R.id.btn_of_back, R.id.tv_of_right, R.id.ll_how_to_load})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.tv_of_right:
                break;
            case R.id.ll_how_to_load:
                ARouter.getInstance().build(RouterPath.SHOW_ACTIVITY).withInt("contentType", InfoActivity.HOW_TO_LOAD).navigation();
                break;
        }
    }
}
