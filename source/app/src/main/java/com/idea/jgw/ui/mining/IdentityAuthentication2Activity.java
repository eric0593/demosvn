package com.idea.jgw.ui.mining;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.OkhttpApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.dialog.LoadingDialog;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.ui.user.UserInfoActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.CommonUtils;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.utils.glide.GlideApp;
import com.socks.okhttp.plus.listener.UploadListener;
import com.socks.okhttp.plus.model.Progress;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

import static com.idea.jgw.api.OkhttpApi.BASE_HOST;

/**
 * 实名认证，第二步，上传照片
 */
@Route(path = RouterPath.IDENTITY_AUTHENTICATION_ACTIVITY2)
public class IdentityAuthentication2Activity extends BaseActivity {
    //调用系统相机请求码，正面
    public static final int DO_CAMERA_REQUEST = 100;
    //调用系统相册请求码，正面
    public static final int OPEN_SYS_ALBUMS_REQUEST = 101;
    //调用系统相机请求码，反面
    public static final int DO_CAMERA_REQUEST_BACK = 103;
    //调用系统相册请求码，反面
    public static final int OPEN_SYS_ALBUMS_REQUEST_BACK = 104;

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iv_front_id_card)
    ImageView ivFrontIdCard;
    @BindView(R.id.iv_back_id_card)
    ImageView ivBackIdCard;
    @BindView(R.id.btn_of_submit)
    Button btnOfSubmit;
    private String frontPhotoPath;
    private String backPhotoPath;
    private String idNumber;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_identity_authentication2;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.authentication);

        if(getIntent().hasExtra("name")) {
            name = getIntent().getStringExtra("name");
        }
        if(getIntent().hasExtra("idNumber")) {
            idNumber = getIntent().getStringExtra("idNumber");
        }
    }

    @OnClick({R.id.btn_of_back, R.id.iv_front_id_card, R.id.iv_back_id_card, R.id.tv_load_from_album_front, R.id.tv_load_from_album_back, R.id.btn_of_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.iv_front_id_card:
                frontPhotoPath = CommonUtils.doCamra(this, "front.jpg", DO_CAMERA_REQUEST);
                break;
            case R.id.iv_back_id_card:
                backPhotoPath = CommonUtils.doCamra(this, "back.jpg", DO_CAMERA_REQUEST_BACK);
                break;
            case R.id.tv_load_from_album_front:
                frontPhotoPath = CommonUtils.openSysPick(this, "front.jpg", OPEN_SYS_ALBUMS_REQUEST);
                break;
            case R.id.tv_load_from_album_back:
                backPhotoPath = CommonUtils.openSysPick(this, "back.jpg", OPEN_SYS_ALBUMS_REQUEST_BACK);
                break;
            case R.id.btn_of_submit:
                if(TextUtils.isEmpty(frontPhotoPath)) {
                    MToast.showToast(R.string.id_photo_is_null);
                } else if(TextUtils.isEmpty(backPhotoPath)) {
                    MToast.showToast(R.string.id_back_photo_is_null);
                } else {
                    certification();
                }
                break;
        }
    }

    private void certification() {
        File file1 = new File(frontPhotoPath);
        File file2 = new File(backPhotoPath);
        String token = SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_SESSION, "").toString();
        OkhttpApi.certification(token, name, idNumber, file1, file2, new UploadListener() {
            @Override
            public void onSuccess(String data) {
                BaseResponse baseResponse = JSON.parseObject(data, BaseResponse.class);
                MToast.showToast(baseResponse.getData().toString());
                if(baseResponse.getCode() == 200) {
                    setResult(RESULT_OK);
                    finish();
                } else if(baseResponse.getCode() == 0) {
                    ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onUIProgress(Progress progress) {
            }

            @Override
            public void onUIStart() {
                LoadingDialog.showDialogForLoading(IdentityAuthentication2Activity.this);
            }

            @Override
            public void onUIFinish() {
                LoadingDialog.cancelDialogForLoading();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case DO_CAMERA_REQUEST:
                case OPEN_SYS_ALBUMS_REQUEST:
                    GlideApp.with(this).load(frontPhotoPath).centerInside().into(ivFrontIdCard);
                    break;
                case DO_CAMERA_REQUEST_BACK:
                case OPEN_SYS_ALBUMS_REQUEST_BACK:
                    GlideApp.with(this).load(backPhotoPath).centerInside().into(ivBackIdCard);
                    break;
            }

        }
    }
}
