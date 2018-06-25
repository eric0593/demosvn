package com.idea.jgw.ui;

import android.app.Fragment;

import com.alibaba.android.arouter.launcher.ARouter;
import com.idea.jgw.App;
import com.idea.jgw.RouterPath;

/**
 * Created by idea on 2018/6/21.
 */

public class BaseFragment extends Fragment{

    public void reLogin() {
        ARouter.getInstance().build(RouterPath.LOGIN_ACTIVITY).navigation();
        App.finishAllActivity();
        getActivity().finish();
    }

}
