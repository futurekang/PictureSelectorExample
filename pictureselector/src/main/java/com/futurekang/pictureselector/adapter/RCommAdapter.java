package com.futurekang.pictureselector.adapter;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public abstract class RCommAdapter<T> extends RecyclerView.Adapter<RCommAdapter.RCViewHolder> {
    private static final String TAG = "RCommAdapter";
    private List<T> dataList;
    private int itemlayoutId = -1;
    private int headerViewId = -1;
    private int footerViewId = -1;

    public final int TYPE_HEADER = 0;//头布局
    public final int TYPE_NORMAL = 1;//条目
    public final int TYPE_FOOTER = 2;//底部布局

    // 当前加载状态，默认为加载完成
    private int loadState = 2;
    // 正在加载
    public static final int LOADING = 1;
    // 加载完成
    public static final int LOADING_COMPLETE = 2;
    // 加载到底
    public static final int LOADING_END = 3;


    @IntDef({LOADING, LOADING_COMPLETE, LOADING_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoadState {
    }


    public RCommAdapter(List<T> dataList, @LayoutRes int layoutId) {
        this.itemlayoutId = layoutId;
        this.dataList = dataList;
    }

    public void addLoadMoreListener(@Nullable RecyclerView recyclerView, @Nullable EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener) {
        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
    }


    public static class RCViewHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> mViews;
        private View item;

        public RCViewHolder(@NonNull View itemView) {
            super(itemView);
            mViews = new SparseArray<>();
            item = itemView;
        }

        /**
         * 获取节点view
         */
        @SuppressWarnings("unchecked")
        public <T extends View> T getItemView(@IdRes int id) {
            View view = mViews.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                mViews.append(id, view);
            }
            return (T) view;

        }

        public void setText(String text, @IdRes int id) {
            TextView textView = getItemView(id);
            textView.setText(text);
        }


        public void setOnClickListener(@IdRes int id, @Nullable View.OnClickListener listener) {
            getItemView(id).setOnClickListener(listener);
        }
    }


    @NonNull
    @Override
    public RCViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View v = LayoutInflater.from(parent.getContext()).inflate(itemlayoutId, parent, false);
            return new RCViewHolder(v);
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(headerViewId, parent, false);
            return new RCViewHolder(v);
        } else if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(footerViewId, parent, false);
            return new RCViewHolder(v);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull RCViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_NORMAL && headerViewId != -1) {
            setView(holder, position, dataList.get(position - 1));
        } else if (getItemViewType(position) == TYPE_NORMAL) {
            setView(holder, position, dataList.get(position));
        } else {
            setView(holder, position, null);
        }

        if (getItemViewType(position) == TYPE_FOOTER && loadState == LOADING) {
            holder.itemView.setVisibility(View.VISIBLE);
        } else if (getItemViewType(position) == TYPE_FOOTER && (loadState == LOADING_COMPLETE || loadState == LOADING_END)) {
            holder.itemView.setVisibility(View.GONE);
        }
    }

    public abstract void setView(RCViewHolder viewHolder, int position, T itemData);

    @Override
    public int getItemCount() {
        int itemCount = -1;
        if (headerViewId == -1 && footerViewId == -1) {
            itemCount = dataList.size();
        } else if (headerViewId != -1 && footerViewId != -1) {
            itemCount = dataList.size() + 2;
        } else if (headerViewId == -1 || footerViewId == -1) {
            itemCount = dataList.size() + 1;
        }
        return itemCount;
    }

    public T getItem(int postion) {
        int itemCount = -1;
        if (getItemViewType(postion) == TYPE_NORMAL) {
            return dataList.get(postion);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (headerViewId != -1 && position == 0) {
            return TYPE_HEADER;
        } else if (footerViewId != -1 && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    /**
     * 通过布局的ID 设置header
     *
     * @param headerViewId
     */
    public void setHeaderViewId(@LayoutRes int headerViewId) {
        this.headerViewId = headerViewId;
    }

    public int getItemlayoutId() {
        return itemlayoutId;
    }

    public int getHeaderViewId() {
        return headerViewId;
    }

    public int getFooterViewId() {
        return footerViewId;
    }

    public interface OnItemClickListener {
        public boolean onItemClick(RCViewHolder viewHolder, int position);
    }

    /**
     * 设置上拉加载状态
     *
     * @param loadState 0.正在加载 1.加载完成 2.加载到底
     */
    public void setLoadState(@LoadState int loadState) {
        if (footerViewId != -1) {
            this.loadState = loadState;
            notifyDataSetChanged();
        }
    }

    public void setFooterViewId(@LayoutRes int footerViewId) {
        this.footerViewId = footerViewId;
    }

}
