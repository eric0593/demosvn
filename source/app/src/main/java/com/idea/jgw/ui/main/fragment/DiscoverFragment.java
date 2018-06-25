package com.idea.jgw.ui.main.fragment;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.idea.jgw.App;
import com.idea.jgw.R;
import com.idea.jgw.RouterPath;
import com.idea.jgw.api.retrofit.ServiceApi;
import com.idea.jgw.bean.BaseResponse;
import com.idea.jgw.bean.CoinMining;
import com.idea.jgw.bean.AllMiningData;
import com.idea.jgw.ui.BaseFragment;
import com.idea.jgw.ui.BaseRecyclerAdapter;
import com.idea.jgw.ui.main.adapter.MiningAdapter;
import com.idea.jgw.utils.SPreferencesHelper;
import com.idea.jgw.utils.baserx.RxSubscriber;
import com.idea.jgw.utils.common.MToast;
import com.idea.jgw.utils.common.ShareKey;
import com.idea.jgw.view.FloatView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * <p>挖矿tab</p>
 * Created by idea on 2018/5/16.
 */

public class DiscoverFragment extends BaseFragment implements BaseRecyclerAdapter.OnItemClickListener<CoinMining> {

    MiningAdapter miningAdapter;
    MediaPlayer mMediaPlayer;

    @BindView(R.id.sv_of_content)
    SurfaceView svOfContent;
    @BindView(R.id.tv_notify)
    TextView tvNotify;
    @BindView(R.id.tv_asset)
    TextView tvAsset;
    @BindView(R.id.tv_hashrate)
    TextView tvHashrate;
    @BindView(R.id.tv_mining_income_label)
    TextView tvMiningIncomeLabel;
    @BindView(R.id.btn_add_hashrate)
    Button btnAddHashrate;
    @BindView(R.id.rv_of_detail_mining)
    RecyclerView rvOfDetailAsset;
    @BindView(R.id.fv_of_mining)
    FloatView fvOfMining;

    private Subscription miningSubscription;
    private Subscription receiveMiningSubscription;
    AllMiningData allMiningData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        miningAdapter = new MiningAdapter();
//        miningAdapter.addDatas(getTestDatas(3));
        miningAdapter.setOnItemClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_discovery, null);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvOfDetailAsset.setLayoutManager(layoutManager);
        rvOfDetailAsset.setAdapter(miningAdapter);
        initSurfaceView();

//        fvOfMining.setList(getTestData());
        fvOfMining.setOnItemClickListener(new FloatView.OnItemClickListener() {
            @Override
            public void itemClick(FloatView.FloatViewData value) {
//                Toast.makeText(getActivity(), "当前是第"+position+"个，其值是"+value.floatValue(), Toast.LENGTH_SHORT).show();
                receiveMiningData(value.getType(), value.getValue());
            }
        });

        tvHashrate.setText(String.format(getString(R.string.sample_hashrate), 0));

        getMiningData();
        return view;
    }

    private void getMiningData() {
        String token = SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_SESSION, "").toString();
//        String imei = CommonUtils.getIMEI(App.getInstance());
        String imei = "qwe"; //设备号暂时使用qwe
        miningSubscription = ServiceApi.getInstance().getApiService()
                .miningData(imei, token)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(getActivity(), getResources().getString(R.string.loading), true) {
                               @Override
                               protected void _onNext(BaseResponse baseResponse) {
                                   if (baseResponse.getCode() == BaseResponse.RESULT_OK) {
//                                       allMiningData = GsonUtils.parseJson(baseResponse.getData().toString(), AllMiningData.class);
                                       allMiningData = JSON.parseObject(baseResponse.getData().toString(), AllMiningData.class);
                                       tvHashrate.setText(String.format(getString(R.string.sample_hashrate), allMiningData.getCalculation()));
                                       SPreferencesHelper.getInstance(App.getInstance()).saveData(ShareKey.KEY_OF_HASHRATE, allMiningData.getCalculation());
                                       List<CoinMining> coinMinings = allMiningData.getList();
                                       miningAdapter.replaceDatas(coinMinings);

                                       List<FloatView.FloatViewData> list = new ArrayList<>();
                                       for(CoinMining coinMining : coinMinings) {
                                           FloatView.FloatViewData data = new FloatView.FloatViewData();
                                           data.setType(coinMining.getCoin_info().getId());
                                           data.setValue(coinMining.getReceive_profit());
                                           list.add(data);
                                       }
                                       fvOfMining.setList(list);
                                   } else if(baseResponse.getCode() == BaseResponse.INVALID_SESSION) {
                                       reLogin();
                                       MToast.showToast(baseResponse.getData().toString());
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

    private void receiveMiningData(final int type, final double value) {
        String token = SPreferencesHelper.getInstance(App.getInstance()).getData(ShareKey.KEY_OF_SESSION, "").toString();
        receiveMiningSubscription = ServiceApi.getInstance().getApiService()
                .receiveMiningData(type, String.valueOf(value), token)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseResponse>(getActivity(), getResources().getString(R.string.loading), true) {
                               @Override
                               protected void _onNext(BaseResponse baseResponse) {
                                   if (baseResponse.getCode() == BaseResponse.RESULT_OK) {
                                       fvOfMining.removeAt(type);
                                       for(CoinMining coinMining : miningAdapter.getmDatas()) {
                                           if(coinMining.getCoin_info().getId() == type) {
                                               double profit = coinMining.getBalance() + value;
                                               coinMining.setBalance(profit);
                                               break;
                                           }
                                       }
                                       miningAdapter.notifyDataSetChanged();
                                       MToast.showToast(baseResponse.getData().toString());
                                   } else if (baseResponse.getCode() == BaseResponse.INVALID_SESSION) {
                                       reLogin();
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

    private List<Float> getTestData() {
        List<Float> list = new ArrayList<>();
        list.add((float) 1.567);
        list.add((float) 0.261);
        list.add((float) 2.455);
        list.add((float) 2.4000255);
        list.add((float) 0.000255);
        return list;
    }

    @Override
    public void onItemClick(int position, CoinMining data) {
        int coinType = data.getCoin_info().getId();
        double balance = data.getBalance();
        ARouter.getInstance().build(RouterPath.MINING_DETAIL_ACTIVITY).withInt("coinType", coinType).withDouble("balance", balance).navigation();
    }

//    public List getTestDatas(int size) {
//        List<CoinMining> digitalCurrencys = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            CoinMining coinData = new CoinMining();
//            coinData.setType(i+1);
//            digitalCurrencys.add(coinData);
//        }
//        return digitalCurrencys;
//    }

    @Override
    public void onResume() {
        super.onResume();
        initMediaPlayer();
    }

    private void initSurfaceView() {

        svOfContent.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initMediaPlayer();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                mMediaPlayer.release();
                Log.i(">>>>>>>>>>>>>>>>>>>>>", "销毁了");
            }
        });
    }

    boolean loopVideo;

    private void initMediaPlayer() {

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.reset();//初始化
            AssetFileDescriptor afd = getActivity().getAssets().openFd("video1.mp4");//获取视频资源
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setDisplay(svOfContent.getHolder());
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
            loopVideo = false;
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnClick(R.id.btn_add_hashrate)
    public void onClick() {
        ARouter.getInstance().build(RouterPath.MINING_HASHRATE_ACTIVITY).navigation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unSubscribe(miningSubscription);
        unSubscribe(receiveMiningSubscription);
    }

    public void unSubscribe(Subscription subscription) {
        if(subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
