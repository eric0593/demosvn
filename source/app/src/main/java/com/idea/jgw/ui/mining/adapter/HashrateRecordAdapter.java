package com.idea.jgw.ui.mining.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idea.jgw.R;
import com.idea.jgw.ui.BaseRecyclerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by idea on 2018/5/16.
 */

public class HashrateRecordAdapter extends BaseRecyclerAdapter<String> {

    @Override
    public RecyclerView.ViewHolder onCreate(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_of_hashrate_record_list, parent, false);
        DigitalCurrencyListHolder holder = new DigitalCurrencyListHolder(view);
        return holder;
    }

    @Override
    public void onBind(RecyclerView.ViewHolder viewHolder, int realPosition, String data) {
    }

    class DigitalCurrencyListHolder extends Holder {

        @BindView(R.id.tv_of_digital_name)
        TextView tvOfDigitalName;
        @BindView(R.id.tv_of_date)
        TextView tvOfDate;
        @BindView(R.id.tv_of_num)
        TextView tvOfNum;

        public DigitalCurrencyListHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
