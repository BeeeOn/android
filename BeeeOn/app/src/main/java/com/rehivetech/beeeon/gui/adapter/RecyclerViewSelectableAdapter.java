package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageView;

import com.rehivetech.beeeon.R;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerViewSelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	@SuppressWarnings("unused")
	private static final String TAG = RecyclerViewSelectableAdapter.class.getSimpleName();

	private SparseBooleanArray mSelectedItems = new SparseBooleanArray();
	protected Context mContext;
	protected int mSelectableItemBackgroundDrawable;

	public RecyclerViewSelectableAdapter(Context context) {
		mContext = context;

		// set drawable as property so that it's selected only once
		TypedArray typedArray = mContext.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
		mSelectableItemBackgroundDrawable = typedArray.getResourceId(0, 0);
		typedArray.recycle();
	}

	/**
	 * Indicates if the item at position position is selected
	 * @param position Position of the item to check
	 * @return true if the item is selected, false otherwise
	 */
	public boolean isSelected(int position) {
		return getSelectedItems().contains(position);
	}

	/**
	 * Toggle the selection status of the item at a given position
	 * @param position Position of the item to toggle the selection status for
	 */
	public void toggleSelection(int position) {
		if (mSelectedItems.get(position, false)) {
			mSelectedItems.delete(position);
		} else {
			mSelectedItems.put(position, true);
		}
		notifyItemChanged(position);
	}

	/**
	 * Clear the selection status for all items
	 */
	public void clearSelection() {
		List<Integer> selection = getSelectedItems();
		mSelectedItems.clear();
		for (Integer i : selection) {
			notifyItemChanged(i);
		}
	}

	/**
	 * Swap selected item position while dragging item
	 * @param fromPosition
	 * @param toPosition
	 */
	public void swapSelectedPosition(int fromPosition, int toPosition) {
		boolean selection = mSelectedItems.get(fromPosition);

		mSelectedItems.put(fromPosition, !selection);
		mSelectedItems.put(toPosition, selection);
	}

	/**
	 * Selects only one item at a time
	 * @param position
	 */
	public void selectOne(int position){
		clearSelection();
		toggleSelection(position);
	}

	/**
	 * Count the selected items
	 * @return Selected items count
	 */
	public int getSelectedItemCount() {
		return mSelectedItems.size();
	}

	/**
	 * Indicates the list of selected items
	 * @return List of selected items ids
	 */
	public List<Integer> getSelectedItems() {
		List<Integer> items = new ArrayList<>(mSelectedItems.size());
		for (int i = 0; i < mSelectedItems.size(); ++i) {
			items.add(mSelectedItems.keyAt(i));
		}
		return items;
	}

	/**
	 * Sets selected items sparseArray based on list of selected positions
	 * @param selectedPositions
	 */
	public void setSelectedItems(List<Integer> selectedPositions){
		mSelectedItems.clear();
		for(Integer pos : selectedPositions){
			mSelectedItems.put(pos, true);
			notifyItemChanged(pos);
		}
	}

	/**
	 * Returns first selected item
	 * @return
	 */
	public int getFirstSelectedItem(){
		return mSelectedItems.keyAt(0);
	}

	public abstract class SelectableViewHolder extends RecyclerView.ViewHolder{

		public SelectableViewHolder(View itemView) {
			super(itemView);
		}

		/**
		 * Returns background of item (if selected different, otherwise clickable)
		 * @param isSelected
		 */
		protected void setSelectedBackground(boolean isSelected) {
			this.itemView.setBackgroundResource(isSelected ? R.color.gray_light : mSelectableItemBackgroundDrawable);
		}

		/**
		 * Based on item selection changes ImageView src & background
		 * ONLY FOR OVAL_ITEM !
		 *
		 * @param icon
		 * @param isSelected
		 * @param defaultSrc
		 */
		protected void setSelectedIcon(ImageView icon, boolean isSelected, int defaultSrc){
			if(isSelected){
				icon.setImageResource(R.drawable.ic_action_accept);
				icon.setBackgroundResource(R.drawable.oval_selected);
			}
			else{
				icon.setImageResource(defaultSrc);
				icon.setBackgroundResource(R.drawable.oval_primary);
			}
		}

		/**
		 * Sets selected without icon
		 * @param isSelected
		 */
		public void setSelected(boolean isSelected){
			setSelected(isSelected, null, 0);
		}

		/**
		 * Sets selected item with icon
		 * @param isSelected
		 * @param icon
		 * @param defaultSrc
		 */
		public void setSelected(boolean isSelected, @Nullable ImageView icon, int defaultSrc){
			// setups background resource based on "selected"
			setSelectedBackground(isSelected);
			// setups icon based on selection
			if(icon != null && defaultSrc > 0) {
				setSelectedIcon(icon, isSelected, defaultSrc);
			}
		}

	}

	public interface IItemClickListener {
		void onRecyclerViewItemClick(int position, int viewType);
		boolean onRecyclerViewItemLongClick(int position, int viewType);
	}

}