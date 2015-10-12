package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;

import java.util.ArrayList;

/**
 * Created by mlyko on 28. 7. 2015.
 */
public class DeviceRecycleAdapter extends RecyclerViewSelectableAdapter<RecyclerView.ViewHolder> {

	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_DEVICE = 0;
	public static final int TYPE_LOCATION = 1;

	private ArrayList<Object> mObjects = new ArrayList<>();
	private IItemClickListener mClickListener;

	private TimeHelper mTimeHelper;

	public DeviceRecycleAdapter(Context context, IItemClickListener listener) {
		super(context);

		mClickListener = listener;

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = Controller.getInstance(context).getUserSettings();
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);
	}

	/**
	 * Returns type based on object's on position class
	 * @param position
	 * @return TYPE_XXX
	 */
	@Override
	public int getItemViewType(int position) {
		if(mObjects.get(position) instanceof Device){
			return TYPE_DEVICE;
		}
		else if(mObjects.get(position) instanceof Location){
			return TYPE_LOCATION;
		}
		else{
			throw new ClassCastException(String.format("%s must be Device or Location!", mObjects.get(position).toString()));
		}
	}

	/**
	 * Creates ViewHolder base on if viewType is Device or Location
	 * @param parent
	 * @param viewType
	 * @return
	 */
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View v;
		switch (viewType){
			case TYPE_DEVICE:
				v = inflater.inflate(R.layout.item_list_device, parent, false);
				return new DeviceViewHolder(v, mClickListener);

			case TYPE_LOCATION:
				v = inflater.inflate(R.layout.item_list_location_header, parent, false);
				return new LocationViewHolder(v);

			default:
				// TODO should we use some kind of error viewHolder ?
				v = inflater.inflate(R.layout.item_list_location_header, parent, false);
				return new LocationViewHolder(v);
		}
	}

	/**
	 * Binds view base on type of object
	 * @param viewHolder
	 * @param position
	 */
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		switch(viewHolder.getItemViewType()){
			case TYPE_DEVICE:
				Device device = (Device) mObjects.get(position);
				if(device == null) return; // TODO should show error view or loading or sth
				Gate gate = Controller.getInstance(mContext).getGatesModel().getGate(device.getGateId());

				DeviceViewHolder deviceHolder = (DeviceViewHolder) viewHolder;

				deviceHolder.mTitle.setText(device.getName(mContext));
				deviceHolder.mSubTitle.setText(device.getType().getManufacturerRes());

				// last update
				deviceHolder.mSubText.setText((mTimeHelper != null && gate != null) ? mTimeHelper.formatLastUpdate(device.getLastUpdate(), gate) : "" );

				Integer battery = device.getBattery();
				// no battery, hide info
				if (battery == null || battery < 0){
					deviceHolder.mAdditional.setVisibility(View.INVISIBLE);
				}
				// otherwise set text and show
				else {
					deviceHolder.mAdditional.setText(String.format("%d %%", battery));
					deviceHolder.mAdditional.setVisibility(View.VISIBLE);
				}

				// sets selected background && icon
				deviceHolder.setSelected(isSelected(position), deviceHolder.mIcon, R.drawable.ic_mod_boiler_state);

				break;

			case TYPE_LOCATION:
				Location location = (Location) mObjects.get(position);
				if(location == null) return; // TODO should show error view or loading or sth
				LocationViewHolder locationHolder = (LocationViewHolder) viewHolder;

				//locationHolder.mDivider.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
				locationHolder.mHeader.setText(location.getName());
				break;
		}
	}

	@Override
	public int getItemCount() {
		return mObjects.size();
	}

	public Object getItem(int position){
		return mObjects.get(position);
	}

	/**
	 * Adds item & animate it if animator set
	 * @param position
	 */
	public void addItem(int position, Object data){
		mObjects.add(position, data);
		notifyItemInserted(position);
	}

	/**
	 * Removes item & animate it if animator set
	 * @param position
	 */
	public void removeItem(int position){
		mObjects.remove(position);
		notifyItemRemoved(position);
	}

	/**
	 * Updates data with new & tries to animate
	 * @param objs
	 */
	public void updateData(ArrayList<Object> objs) {
		mObjects = objs;
		notifyDataSetChanged();
		// TODO this should notify whole list
		notifyItemRangeChanged(0, mObjects.size());
	}

	// ------------------------------ ViewHolders ------------------------------ //

	/**
	 * ViewHolder for Device data
	 */
	public class DeviceViewHolder extends SelectableViewHolder implements View.OnClickListener, View.OnLongClickListener {
		public TextView mTitle;
		public ImageView mIcon;
		public TextView mSubTitle;
		public TextView mSubText;
		public TextView mAdditional;
		public IItemClickListener mListener;

		public DeviceViewHolder(View itemView, IItemClickListener listener) {
			super(itemView);
			mTitle = (TextView) itemView.findViewById(R.id.list_item_title);
			mIcon = (ImageView) itemView.findViewById(R.id.list_item_icon);
			mSubTitle = (TextView) itemView.findViewById(R.id.list_item_subtitle);
			mSubText = (TextView) itemView.findViewById(R.id.list_item_subtext);
			mAdditional = (TextView) itemView.findViewById(R.id.list_item_additional);
			mListener = listener;

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if(mListener != null){
				mListener.onRecyclerViewItemClick(getAdapterPosition(), getItemViewType());
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if(mListener != null && mListener.onRecyclerViewItemLongClick(getAdapterPosition(), getItemViewType())){
				v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
			return false;
		}
	}

	/**
	 * ViewHolder for Location Header
	 */
	public static class LocationViewHolder extends RecyclerView.ViewHolder{
		public TextView mHeader;
		//public View mDivider;

		public LocationViewHolder(View itemView) {
			super(itemView);
			mHeader = (TextView) itemView.findViewById(R.id.list_location_header_text);
			//mDivider = itemView.findViewById(R.id.list_module_item_sep_middle);
		}
	}

	public interface IItemClickListener {
		void onRecyclerViewItemClick(int position, int viewType);
		boolean onRecyclerViewItemLongClick(int position, int viewType);
	}
}
