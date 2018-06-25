package com.idea.jgw.ui.mining;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.ShareKey;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 当前算力
 */
@Route(path = RouterPath.MINING_HASHRATE_ACTIVITY)
public class MiningHashrateActivity extends BaseActivity {

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iv_hashrate_logo)
    ImageView ivHashrateLogo;
    @BindView(R.id.tv_of_hashrate_record)
    TextView tvOfHashrateRecord;
    @BindView(R.id.tv_hashrate_label)
    TextView tvHashrateLabel;
    @BindView(R.id.tv_current_hashrate)
    TextView tvCurrentHashrate;
    @BindView(R.id.tv_finish_task_label)
    TextView tvFinishTaskLabel;
    @BindView(R.id.tv_base_task)
    TextView tvBaseTask;
    @BindView(R.id.tv_auth)
    TextView tvAuth;
    @BindView(R.id.tv_share_to_friends)
    TextView tvShareToFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_mining_hashrate;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.add_hashrate);
        String shareToFriends = getString(R.string.share_to_friends) + "+" + SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_INVITE_NUM, 0);
        setSpannableString(shareToFriends, tvShareToFriends);
        tvCurrentHashrate.setText(String.valueOf(SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_HASHRATE, 0)));
    }

    private void setSpannableString(String shareToFriends, TextView textView) {
        SpannableString spannableString = new SpannableString(shareToFriends);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#193fc3"));
        spannableString.setSpan(colorSpan, 4, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    @OnClick({R.id.btn_of_back, R.id.ll_auth, R.id.tv_of_hashrate_record, R.id.ll_share_to_friends})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.tv_of_hashrate_record:
                //算力提示记录
                ARouter.getInstance().build(RouterPath.HASHRATE_RECORD_ACTIVITY2).navigation();
                break;
            case R.id.ll_auth:
                ARouter.getInstance().build(RouterPath.IDENTITY_AUTHENTICATION_ACTIVITY).navigation();
                break;
            case R.id.ll_share_to_friends:
                ARouter.getInstance().build(RouterPath.SHARE_ACTIVITY).navigation();
                break;
        }
    }
}
