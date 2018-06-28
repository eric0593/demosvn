package com.idea.jgw.ui.login;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.ServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.bean.LoginRequest;
import com.idea.jgw.bean.RegisterRequest;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.CommonUtils;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.utils.common.SharedPreferenceManager;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 设置登录密码页面（注册第二步）
 */
@Route(path = RouterPath.SET_LOGIN_PASSWORD_ACTIVITY)
public class SetPasswordActivity extends BaseActivity {

    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.iBtn_of_show_pwd)
    ImageButton iBtnOfShowPwd;
    @BindView(R.id.btn_of_next)
    Button btnOfRegister;
    @BindView(R.id.et_of_pwd)
    EditText etOfPwd;

    @Autowired
    String verifyCode;
    @Autowired
    String phone;
    @Autowired
    String inviteCode;
    private Subscription loginSubscription;
    private Subscription registerSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().hasExtra("phone")) {
            phone = getIntent().getStringExtra("phone");
        }
        if(getIntent().hasExtra("verifyCode")) {
            verifyCode = getIntent().getStringExtra("verifyCode");
        }
        if(getIntent().hasExtra("inviteCode")) {
            inviteCode = getIntent().getStringExtra("inviteCode");
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_set_password;
    }

    @Override
    public void initView() {
        tvOfTitle.setText(R.string.set_login_pwd);
    }

    @OnClick({R.id.btn_of_back, R.id.iBtn_of_show_pwd, R.id.btn_of_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.iBtn_of_show_pwd:
                if (iBtnOfShowPwd.isSelected()) {
                    iBtnOfShowPwd.setSelected(false);
                    CommonUtils.setTextPwdInputType(etOfPwd);
                } else {
                    iBtnOfShowPwd.setSelected(true);
                    CommonUtils.setTextInputType(etOfPwd);
                }
                break;
            case R.id.btn_of_next:
                final String pwd = etOfPwd.getText().toString().trim();
                if(validPassword(pwd)) {
                    RegisterRequest registerRequest = new RegisterRequest();
                    registerRequest.setAccount(phone);
                    registerRequest.setVerifycode(verifyCode);
//                    registerRequest.setDevice_id(CommonUtils.getIMEI(this));
                    registerRequest.setDevice_id("qwe");
                    registerRequest.setIp(CommonUtils.getIp(this));
                    registerRequest.setPasswd(pwd);

                    register(registerRequest);
                }
                break;
        }
    }

    private void register(RegisterRequest registerRequest) {
        registerSubscription = ServiceApi.getInstance().getApiService()
                .register(registerRequest.getQueryMap())
//                            .register(registerRequest.getAccount(), registerRequest.getPasswd(), registerRequest.getVerifycode())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(this, getResources().getString(R.string.loading), true) {

            @Override
            protected void _onNext(BaseResponse baseResponse) {
                if (baseResponse.getCode() == BaseResponse.RESULT_OK) {
                    login();
                } else if (baseResponse.getCode() == BaseResponse.INVALID_SESSION) {
                    reLogin();
                }
                MToast.showToast(baseResponse.getData().toString());
            }

            @Override
            protected void _onError(String message) {
                MToast.showToast(message);
            }
        });
    }

    private void login() {
        String pwd = etOfPwd.getText().toString().trim();
        LoginRequest loginRequest = new LoginRequest();
        String phone1 = phone.split("-")[1];
        loginRequest.setAccount(phone1);
        loginRequest.setPasswd(pwd);

        login(loginRequest);
    }

    protected void login(LoginRequest loginRequest) {
        loginSubscription = ServiceApi.getInstance().getApiService()
                .login(loginRequest.getQueryMap())
                .subscribeOn(Schedulers.io())
                .subscribe(new RxSubscriber<BaseResponse>(SetPasswordActivity.this, getResources().getString(R.string.loading), true) {

            @Override
            protected void _onNext(BaseResponse baseResponse) {
                if(baseResponse.getCode() == BaseResponse.RESULT_OK) {
                    SPreferencesHelper.getInstance(App.getInstance()).saveData(ShareKey.KEY_OF_SESSION, baseResponse.getData().toString());
                    SPreferencesHelper.getInstance(App.getInstance()).saveData(ShareKey.KEY_OF_LOGIN, true);
                    ARouter.getInstance().build(RouterPath.LOAD_OR_CREATE_WALLET_ACTIVITY).navigation();
                }
            }

            @Override
            protected void _onError(String message) {
                MToast.showToast(message);
            }
        });
    }

    private boolean validPassword(String pwd) {
        return !TextUtils.isEmpty(pwd) && pwd.length() > 5;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribe(loginSubscription);
    }
}
