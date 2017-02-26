package org.weibeld.nytexplore.listener;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.weibeld.nytexplore.R;

/**
 * Created by dw on 26/02/17.
 */

/*
 * Emulate an "OnItemClickListener" for a RecyclerView.
 *
 * Usage:
 *
 * Create values/ids.xml:
 * <resources>
 *     <item name="item_click_support" type="id" />
 * </resources>
 *
 * In onCreate of activity:
 * RecyclerViewItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new RecyclerViewItemClickSupport.OnItemClickListener() {
 *    @Override
 *    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
 *        // ...
 *    }
 * });
 *
 * Source: http://www.littlerobots.nl/blog/Handle-Android-RecyclerView-Clicks/
 */
public class RecyclerViewItemClickSupport {
    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
        }
    };
    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                return mOnItemLongClickListener.onItemLongClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
            return false;
        }
    };
    private RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);
            }
            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

    private RecyclerViewItemClickSupport(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setTag(R.id.item_click_support, this);
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public static RecyclerViewItemClickSupport addTo(RecyclerView view) {
        RecyclerViewItemClickSupport support = (RecyclerViewItemClickSupport) view.getTag(R.id.item_click_support);
        if (support == null) {
            support = new RecyclerViewItemClickSupport(view);
        }
        return support;
    }

    public static RecyclerViewItemClickSupport removeFrom(RecyclerView view) {
        RecyclerViewItemClickSupport support = (RecyclerViewItemClickSupport) view.getTag(R.id.item_click_support);
        if (support != null) {
            support.detach(view);
        }
        return support;
    }

    public RecyclerViewItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    public RecyclerViewItemClickSupport setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
        return this;
    }

    private void detach(RecyclerView view) {
        view.removeOnChildAttachStateChangeListener(mAttachListener);
        view.setTag(R.id.item_click_support, null);
    }

    public interface OnItemClickListener {

        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }

    public interface OnItemLongClickListener {

        boolean onItemLongClicked(RecyclerView recyclerView, int position, View v);
    }
}
