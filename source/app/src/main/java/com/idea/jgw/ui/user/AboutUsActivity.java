package com.idea.jgw.ui.user;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.ui.BaseActivity;
import com.idea.jgw.utils.CheckVersion;
import com.idea.jgw.utils.UpdateManager;
import com.idea.jgw.utils.common.CommonUtils;
import com.idea.jgw.utils.common.DialogUtils;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.MyLog;
import com.joker.annotation.PermissionsCustomRationale;
import com.joker.annotation.PermissionsGranted;
import com.joker.annotation.PermissionsNonRationale;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * 关于我们
 */
@Route(path = RouterPath.ABOUT_ACTIVITY)
public class AboutUsActivity extends BaseActivity {

    private static final int PHONE_STATE = 1;
    private static final int INSTALL_PACKAGE = 2;
    @BindView(R.id.btn_of_back)
    Button btnOfBack;
    @BindView(R.id.tv_of_title)
    TextView tvOfTitle;
    @BindView(R.id.ll_version_log)
    LinearLayout llVersionLog;
    @BindView(R.id.ll_update)
    LinearLayout llUpdate;
    private String apkPath = "";

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
                requestPermission(PHONE_STATE, Manifest.permission.READ_PHONE_STATE);
                break;
        }
    }

    private void checkAndUpdate(final String version) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (CommonUtils.pingNet("114.215.211.154")) {
                    CheckVersion getVersion = new CheckVersion();
                    getVersion.setCheckNotice(true);
                    final CheckVersion.VersionInfo info = getVersion.checkUpdate(version);
                    if (info == null) {
                        MyLog.e(" CheckVersion.VersionInfo is null");
                    } else {
                        MyLog.e("checkNotice --  versionInfo :" + JSON.toJSONString(info));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (info.isParaseOk && !TextUtils.isEmpty(info.mPackageUrl) && info.mPackageSize > 0) {
                                    if (compare(info.mNum, version)) {
                                        showAlertDialog(getResources().getString(R.string.app_update), info.mLog, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/11.apk";
                                                UpdateManager updateManager = new UpdateManager(AboutUsActivity.this, new UpdateManager.DownloadListener() {
                                                    @Override
                                                    public void onFinish() {
                                                        checkPermissionAndInstall();
                                                    }

                                                    @Override
                                                    public void onFail(Exception e) {
                                                        MToast.showToast(R.string.download_failed);
                                                    }

                                                    @Override
                                                    public int onProgress(int progress) {
                                                        return 0;
                                                    }
                                                });
                                                updateManager.downloadApk(info.mPackageUrl, apkPath);
                                            }
                                        }, true);
                                    }
                                } else {
                                        MToast.showToast(R.string.current_is_new);
                                }
                            }
                        });
                    }
                }
            }
        }).start();
    }

    boolean compare(String version1, String version2) {
//        String flag1 = version1.substring(0, 1);
//        String flag2 = version2.substring(0, 1);
//        if(!flag1.equals("v") && flag2.equals("v")) {
//            return false;
//        } else if(flag1.equals("v") && !flag2.equals("v")) {
//            return true;
//        }

        String[] str1 = getVersionNumber(version1);
        String[] str2 = getVersionNumber(version2);
        if (Integer.parseInt(str1[0]) > Integer.parseInt(str2[0])) {
            return true;
        } else if (Integer.parseInt(str1[0]) == Integer.parseInt(str2[0]) && Integer.parseInt
                (str1[1]) > Integer.parseInt(str2[1])) {
            return true;
        } else if (Integer.parseInt(str1[0]) == Integer.parseInt(str2[0]) && Integer.parseInt
                (str1[1]) == Integer.parseInt(str2[1]) && Integer.parseInt(str1[2]) > Integer
                .parseInt(str2[2])) {
            return true;
        }
        return false;
    }

    private String[] getVersionNumber(String version1) {
        String[] str;
        if(version1.startsWith("v")) {
            str = version1.replace("v", "").split("\\.");
        } else {
            str = version1.replace("d", "").split("\\.");
        }
        return str;
    }

    AlertDialog alertDialog;

    public void showAlertDialog(String title, String str, DialogInterface.OnClickListener negativeListener, DialogInterface.OnClickListener positiveListener, boolean showForground) {
        if (alertDialog != null) {
            if(showForground) {
                alertDialog.dismiss();
            }
            if (alertDialog.isShowing()) {
                return;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setNegativeButton(R.string.string_of_cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
        alertDialog = builder.create();
        if(!TextUtils.isEmpty(title)) {
            alertDialog.setTitle(title);
        } else {
            alertDialog.setTitle(null);
        }
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage(str);
//        if(negativeListener == null) {
//            negativeListener = null;
//        }
        alertDialog.setButton(BUTTON_NEGATIVE, getResources().getString(R.string.string_of_cancel), negativeListener);
        alertDialog.setButton(BUTTON_POSITIVE, getResources().getString(R.string.ok), positiveListener);
        alertDialog.show();
    }

    @PermissionsGranted({PHONE_STATE, INSTALL_PACKAGE})
    public void granted(int requestCode) {
        switch (requestCode) {
            case PHONE_STATE:
                checkAndUpdate(CommonUtils.getAppName(App.getInstance()));
                break;
            case INSTALL_PACKAGE:
                //安装apk
                install(apkPath);
                break;
        }
    }

    private void checkPermissionAndInstall() {
        if (Build.VERSION.SDK_INT >= 26) {
            //来判断应用是否有权限安装apk
            boolean installAllowed= getPackageManager().canRequestPackageInstalls();
            //有权限
            if (installAllowed) {
                //安装apk
                install(apkPath);
            } else {
                //无权限 申请权限
                requestPermission(INSTALL_PACKAGE, Manifest.permission.REQUEST_INSTALL_PACKAGES);
            }
        } else {
            install(apkPath);
        }
    }

    private void install(String apkPath) {
        File apkfile = new File(apkPath);
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //7.0以上通过FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(this, "com.idea.jgw.fileprovider", apkfile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.parse("file://" + apkPath), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    @PermissionsCustomRationale({PHONE_STATE, INSTALL_PACKAGE})
    public void customRationale(int requestCode) {
        switch (requestCode) {
            case PHONE_STATE:
                DialogUtils.showAlertDialog(this, "PHONE_STATE权限申请：\n我们需要您开启读PHONE_STATE权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission(PHONE_STATE, Manifest.permission.READ_PHONE_STATE);
                    }
                });
                break;
            case INSTALL_PACKAGE:
                checkAndUpdate(CommonUtils.getAppName(App.getInstance()));
                DialogUtils.showAlertDialog(this, "安装权限申请：\n我们需要您开启读安装权限，用以更新app", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission(PHONE_STATE, Manifest.permission.INSTALL_PACKAGES);
                    }
                });
                break;
        }
    }

    @PermissionsNonRationale({PHONE_STATE, INSTALL_PACKAGE})
    public void non(int requestCode, final Intent intent) {
        switch (requestCode) {
            case PHONE_STATE:
                DialogUtils.showAlertDialog(this, "PHONE_STATE权限申请：\n我们需要您开启读PHONE_STATE权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(intent);
                    }
                });
                break;
            case INSTALL_PACKAGE:
                DialogUtils.showAlertDialog(this, "安装权限申请：\n我们需要您开启读安装权限，用以更新app", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(intent);
                    }
                });
                break;
        }
    }
}
