package com.idea.jgw.ui.mining;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.ShareKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 分享
 */
@Route(path = RouterPath.SHARE_ACTIVITY)
public class ShareActivity extends BaseActivity {


    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iv_content_bg)
    ImageView ivContentBg;
    @BindView(R.id.btn_of_share)
    Button btnOfShare;
    @BindView(R.id.tv_invite_code)
    TextView tvInviteCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_share;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.share_to_friends);
        tvInviteCode.setText(SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_INVITE_CODE, "").toString());
    }

    @OnClick({R.id.btn_of_back, R.id.btn_of_share})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.btn_of_share:
                share(this);
                break;
        }
    }

}
