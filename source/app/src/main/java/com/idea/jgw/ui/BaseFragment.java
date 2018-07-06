package com.idea.jgw.ui;

import android.app.Fragment;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.RouterPath;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.utils.common.SharedPreferenceManager;

/**
 * Created by idea on 2018/6/21.
 */

public class BaseFragment extends Fragment{

    public void reLogin() {
        SharedPreferenceManager.getInstance().setLogin(false);
        ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
        App.finishAllActivity();
        getActivity().finish();
    }

}
