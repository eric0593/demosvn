package com.idea.jgw.ui.main.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.idea.jgw.R;
import com.idea.jgw.bean.CoinData;
import com.idea.jgw.common.Common;
import com.idea.jgw.ui.BaseRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by idea on 2018/5/16.
 */

public class DigitalCurrencysAdapter extends BaseRecyclerAdapter<CoinData> {

    @Override
    public RecyclerView.ViewHolder onCreate(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_detail_asset, parent, false);
        CoinDataListHolder holder = new CoinDataListHolder(view);
        return holder;
    }

    @Override
    public void onBind(RecyclerView.ViewHolder viewHolder, int realPosition, CoinData data) {
        Log.e("", "onBind");

//        iv_of_digital_currency 图标
//        tv_of_digital_name 名字
        //tv_of_digital_unit_price 单价
//        tv_of_digital_total_price 结合人民币数量
//        tv_of_digital_number 数量
//        viewHolder.
//
//                cd.setAddress(address);
//        cd.setCoinTypeEnum(Common.CoinTypeEnum.JGW);
//        cd.setCount(balance);

        CoinDataListHolder v = (CoinDataListHolder) viewHolder;

        switch (data.getCoinTypeEnum()){
            case BTC:
                v.tvOfDigitalName.setText("BTC");
                break;
            case ETH:
                v.tvOfDigitalName.setText("ETH");
                v.ivOfDigitalCurrency.setImageResource(R.mipmap.icon_eth);
                break;
            case JGW:
                v.ivOfDigitalCurrency.setImageResource(R.mipmap.icon_oce);
                v.tvOfDigitalName.setText("JGW");
                break;
        }
        v.tvOfDigitalNumber.setText(TextUtils.isEmpty(data.getCount())?"0.0":data.getCount());
        v.tvOfDigitalUnitPrice.setVisibility(View.INVISIBLE);
        v.tvOfDigitalTotalPrice.setVisibility(View.INVISIBLE);

    }

    class CoinDataListHolder extends Holder {

        @BindView(R.id.iv_of_digital_currency)
        ImageView ivOfDigitalCurrency;
        @BindView(R.id.tv_of_digital_name)
        TextView tvOfDigitalName;
        @BindView(R.id.tv_of_digital_unit_price)
        TextView tvOfDigitalUnitPrice;
        @BindView(R.id.tv_of_digital_number)
        TextView tvOfDigitalNumber;
        @BindView(R.id.tv_of_digital_total_price)
        TextView tvOfDigitalTotalPrice;

        public CoinDataListHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
