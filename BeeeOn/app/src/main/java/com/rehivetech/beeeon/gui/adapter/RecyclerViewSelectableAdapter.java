package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

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
}