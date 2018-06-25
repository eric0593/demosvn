package com.idea.jgw.ui;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>XRecyclerView的基本适配器</p>
 * Created by dc on 2016/5/17.
 */

public abstract class BaseAdapter<T> extends BaseRecyclerAdapter<T> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;

    List<T> mDatas = new ArrayList<>();

    @Override
    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return position - 1;
    }

}
