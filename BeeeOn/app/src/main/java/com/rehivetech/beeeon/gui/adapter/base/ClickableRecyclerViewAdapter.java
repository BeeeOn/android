package com.rehivetech.beeeon.gui.adapter.base;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * Recycler adapter which implements clickable adapter, used as abstract class for more adapters
 *
 * @author mlyko
 * @since 09.05.2016
 */
public abstract class ClickableRecyclerViewAdapter<VIEW_HOLDER extends ClickableRecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<VIEW_HOLDER> {
	protected @Nullable OnItemClickListener mOnItemClickListener;
	protected @Nullable OnItemLongClickListener mOnItemLongClickListener;
	protected @Nullable View mEmptyView;

	public interface OnItemClickListener {
		/**
		 * When user clicks on recyclerview's item
		 *
		 * @param viewHolder
		 * @param position   of clicked item
		 * @param viewType   type of clicked item
		 */
		void onRecyclerViewItemClick(ViewHolder viewHolder, int position, int viewType);
	}

	public interface OnItemLongClickListener {
		/**
		 * When user long clicks on recyclerview's item
		 *
		 * @param viewHolder
		 * @param position   of clicked item
		 * @param viewType   type of clicked item
		 * @return
		 */
		boolean onRecyclerViewItemLongClick(ViewHolder viewHolder, int position, int viewType);
	}

	public void setEmptyView(@Nullable View view) {
		mEmptyView = view;
		setEmptyViewVisible(true);
	}

	public void setEmptyViewVisible(boolean isVisible) {
		if (mEmptyView == null) return;
		mEmptyView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	public void setOnItemLongClickListener(@Nullable OnItemLongClickListener listener) {
		mOnItemLongClickListener = listener;
	}

	/**
	 * Clickable viewholder
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		public OnItemClickListener mOnItemClickListener;
		public OnItemLongClickListener mOnItemLongClickListener;

		public ViewHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public ViewHolder(View itemView, OnItemClickListener clickListener) {
			this(itemView);
			mOnItemClickListener = clickListener;
		}

		public ViewHolder(View itemView, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
			this(itemView);
			mOnItemClickListener = clickListener;
			mOnItemLongClickListener = longClickListener;
		}

		public void setOnItemClickListener(OnItemClickListener listener) {
			mOnItemClickListener = listener;
		}

		public void setOnItemLongClickListener(OnItemLongClickListener listener) {
			mOnItemLongClickListener = listener;
		}

		@Override
		public void onClick(View v) {
			if (mOnItemClickListener != null) {
				mOnItemClickListener.onRecyclerViewItemClick(this, getAdapterPosition(), getItemViewType());
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if (mOnItemLongClickListener != null && mOnItemLongClickListener.onRecyclerViewItemLongClick(this, getAdapterPosition(), getItemViewType())) {
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
			return false;
		}
	}
}
