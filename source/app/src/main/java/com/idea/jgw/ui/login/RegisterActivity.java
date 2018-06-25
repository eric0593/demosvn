package com.idea.jgw.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.BaseCallback;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.ServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.dialog.ChooseDialog;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.MToast;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 注册首页面
 */
@Route(path = RouterPath.REGISTER_ACTIVITY)
public class RegisterActivity extends BaseActivity implements BaseCallback {

    private static final int CUSTOMER_SERVICE = 11;
    private static final int GET_NATION_CODE = 22;
    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.tv_of_get_security_code)
    TextView tvOfGetSecurityCode;
    @BindView(R.id.tv_of_agreement)
    TextView tvOfAgreement;
    @BindView(R.id.tv_of_customer_service)
    TextView tvOfCustomerService;
    @BindView(R.id.btn_of_next)
    Button btnOfNext;
    @BindView(R.id.et_of_nickname)
    EditText etOfPhone;
    @BindView(R.id.iBtn_of_delete)
    ImageButton iBtnOfDelete;
    @BindView(R.id.tv_of_login)
    TextView tvOfLogin;
    @BindView(R.id.iBtn_of_agree_customer_service)
    ImageButton iBtnOfAgreeCustomerService;
    @BindView(R.id.et_of_security_code)
    EditText etOfSecurityCode;
    @BindView(R.id.ll_of_nation_code)
    LinearLayout llOfNationCode;
    @BindView(R.id.et_of_invite_code)
    EditText etOfInviteCode;
    @BindView(R.id.tv_nation_name)
    TextView tvNationName;
    @BindView(R.id.ll_of_agreement)
    LinearLayout llOfAgreement;
    private String nationCode = "+86";
    private String nationName = "中国大陆";
    private Subscription sendsmsSubscription;
    private Subscription waitSubscription;
    static final int WAIT_TIME = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    public void initView() {
        btnOfBack.setVisibility(View.GONE);
        tvOfTitle.setText(R.string.register);
    }

    @OnClick({R.id.btn_of_back, R.id.tv_of_get_security_code, R.id.iBtn_of_agree_customer_service, R.id.tv_of_agreement, R.id.tv_of_customer_service, R.id.btn_of_next, R.id.iBtn_of_delete, R.id.tv_of_login, R.id.ll_of_nation_code})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_of_back:
                finish();
                break;
            case R.id.tv_of_get_security_code:
                getSecurityCode();
                break;
            case R.id.iBtn_of_agree_customer_service:
                if (view.isSelected()) {
                    view.setSelected(false);
                } else {
                    view.setSelected(true);
                }
                break;
            case R.id.ll_of_nation_code:
                ARouter.getInstance().build(RouterPath.NATION_CODE_ACTIVITY).navigation(this, GET_NATION_CODE);
                break;
            case R.id.tv_of_agreement:
                break;
            case R.id.tv_of_customer_service:
                showCustomerServiceDialog();
                break;
            case R.id.btn_of_next:
                String phone = etOfPhone.getText().toString().trim();
                String verifyCode = etOfSecurityCode.getText().toString().trim();
                String inviteCode = etOfInviteCode.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    MToast.showToast(R.string.phone_is_null);
                } else if (TextUtils.isEmpty(verifyCode)) {
                    MToast.showToast(R.string.verify_code_is_null);
                } else if (!iBtnOfAgreeCustomerService.isSelected()) {
                    MToast.showToast(R.string.agree_service_manager);
                } else {
                    phone = nationCode + "-" + phone;
                    ARouter.getInstance().build(RouterPath.SET_LOGIN_PASSWORD_ACTIVITY)
                            .withString("verifyCode", verifyCode)
                            .withString("phone", phone)
                            .withString("inviteCode", inviteCode)
                            .navigation();
                }
                break;
            case R.id.iBtn_of_delete:
                etOfPhone.setText("");
                break;
            case R.id.tv_of_login:
                ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
                finish();
                break;
        }
    }

    private void getSecurityCode() {
        String phone = etOfPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            MToast.showToast(R.string.phone_is_null);
        } else {
            phone = nationCode + "-" + phone;
            getSms(phone);
        }
    }

    private void getSms(String phone) {
        sendsmsSubscription = ServiceApi.getInstance().getApiService()
                .sendsms(phone)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(this, getResources().getString(R.string.loading), true) {
                               @Override
                               protected void _onNext(BaseResponse baseResponse) {
                                   if(baseResponse.getCode() == 200) {
                                       getSecurityCodeWait();
                                   }
                                   MToast.showToast(baseResponse.getData().toString());
                               }

                               @Override
                               protected void _onError(String message) {
                                   MToast.showToast(message);
                               }
                           }
                );
    }

    private void getSecurityCodeWait() {
        tvOfGetSecurityCode.setClickable(false);
        tvOfGetSecurityCode.setFocusable(false);
        waitSubscription = Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if(aLong < 60) {
                            String s = String.format(getResources().getString(R.string.resend_notice), WAIT_TIME - aLong);
                            tvOfGetSecurityCode.setText(s);
                        } else {
                            tvOfGetSecurityCode.setClickable(true);
                            tvOfGetSecurityCode.setFocusable(true);
                            tvOfGetSecurityCode.setText(R.string.send_security);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GET_NATION_CODE) {
                if (data.hasExtra("nationCode"))
                    nationCode = data.getStringExtra("nationCode");
                if (data.hasExtra("nationName")) {
                    nationName = data.getStringExtra("nationName");
                    tvNationName.setText(nationName);
                }
            }
        }
    }

    private void showCustomerServiceDialog() {
        ChooseDialog chooseDialog = new ChooseDialog(this, CUSTOMER_SERVICE, this);
        chooseDialog.show();
    }

    @Override
    public void callback(Object... parameters) {
        if (parameters.length > 1) {
            int requestCode = Integer.parseInt(parameters[0].toString().trim());
            int resultCode = Integer.parseInt(parameters[1].toString().trim());
            if (requestCode == CUSTOMER_SERVICE) {
                if (resultCode == ChooseDialog.SELECTED_OK) {
                    MToast.showToast("call service!");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribe(sendsmsSubscription);
        unSubscribe(waitSubscription);
    }
}
