package com.idea.jgw.ui.main.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.ServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.bean.UserInfo;
import com.idea.jgw.ui.user.UserInfoActivity;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.CommonUtils;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.utils.common.SharedPreferenceManager;
import com.idea.jgw.utils.glide.GlideApp;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.idea.jgw.api.OkhttpApi.BASE_HOST;
import static com.idea.jgw.api.OkhttpApi.UPDATE_PHOTO;

/**
 * <p>钱包tab</p>
 * Created by idea on 2018/5/16.
 */

public class MineFragment extends Fragment {

    private static final int UPDATE_INFO_REQUEST = 11;

    @BindView(R.id.tv_of_name)
    TextView tvOfName;
    @BindView(R.id.iv_photo)
    ImageView ivPhoto;
    @BindView(R.id.tv_phone)
    TextView tvPhone;
    @BindView(R.id.ll_security_manager)
    LinearLayout llSecurityManager;
    @BindView(R.id.ll_share)
    LinearLayout llShare;
    @BindView(R.id.ll_help)
    LinearLayout llHelp;
    @BindView(R.id.ll_about_us)
    LinearLayout llAboutUs;

    private Subscription getInfoSubscription;
    UserInfo userInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String token = SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_SESSION, "").toString();
        getInfoSubscription = ServiceApi.getInstance().getApiService()
                .getinfo(token)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(getActivity(), getResources().getString(R.string.loading), true) {
                               @Override
                               protected void _onNext(BaseResponse baseResponse) {
                                   if(baseResponse.getCode() == 200) {
                                       userInfo = JSON.parseObject(baseResponse.getData().toString(), UserInfo.class);
                                       tvOfName.setText(userInfo.getNickname());
                                       String phone = SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_PHONE, "").toString();
                                       tvPhone.setText(CommonUtils.replace(phone, "****"));
                                       Glide.with(MineFragment.this).load(BASE_HOST + userInfo.getFace()).apply(RequestOptions.circleCropTransform()).into(ivPhoto);
                                       SPreferencesHelper.getInstance(App.getInstance()).saveData(ShareKey.KEY_OF_INVITE_CODE, userInfo.getInvite_num());
                                       SPreferencesHelper.getInstance(App.getInstance()).saveData(ShareKey.KEY_OF_INVITE_NUM, userInfo.getInvite_man_num());
                                       SPreferencesHelper.getInstance(App.getInstance()).saveData(ShareKey.KEY_OF_INVITE_URL, userInfo.getInvite_url());
                                   } else if(baseResponse.getCode() == 0) {
                                       ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
                                       App.finishAllActivity();
                                       getActivity().finish();
                                   } else {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_mine, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick({R.id.ll_security_manager, R.id.ll_share, R.id.ll_help, R.id.ll_about_us,  R.id.ll_feedback, R.id.iv_photo})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_security_manager:
                ARouter.getInstance().build(RouterPath.SECURITY_MANAGER_ACTIVITY).navigation();
                break;
            case R.id.ll_share:
                ARouter.getInstance().build(RouterPath.SHARE_ACTIVITY).navigation();
                break;
            case R.id.ll_help:
                ARouter.getInstance().build(RouterPath.HELP_ACTIVITY).navigation();
                break;
            case R.id.ll_about_us:
                ARouter.getInstance().build(RouterPath.ABOUT_ACTIVITY).navigation();
                break;
            case R.id.ll_feedback:
                ARouter.getInstance().build(RouterPath.FEEDBACK_ACTIVITY2).navigation();
                break;
            case R.id.iv_photo:
                ARouter.getInstance().build(RouterPath.USER_INFO_ACTIVITY).withParcelable("userInfo", userInfo).navigation(getActivity(), UPDATE_INFO_REQUEST);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == UPDATE_INFO_REQUEST) {
                if(data.hasExtra("nickname")) {
                    String nickname = data.getStringExtra("nickname");
                    tvOfName.setText(nickname);
                }
                if(data.hasExtra("face")) {
                    String face = data.getStringExtra("face");
                    GlideApp.with(MineFragment.this).load(BASE_HOST + face).apply(RequestOptions.circleCropTransform()).into(ivPhoto);
                }
            }
        }
    }
}
