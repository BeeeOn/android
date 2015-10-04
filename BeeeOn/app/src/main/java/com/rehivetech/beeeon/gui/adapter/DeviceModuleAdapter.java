package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.List;

/**
 * @author martin on 13.8.2015.
 */
public class DeviceModuleAdapter extends RecyclerView.Adapter<DeviceModuleAdapter.DeviceModuleAdapterViewHolder> {

	public static final String TAG = DeviceModuleAdapter.class.getSimpleName();

	private static final int VIEW_TYPE_SPACE = 0;
	private static final int VIEW_TYPE_ITEM = 1;

	private Context mContext;
	private List<Module> mModuleList;
	private ItemClickListener mItemClickListener;
	private boolean mFirstItemSpace;

	public DeviceModuleAdapter(Context context, List<Module> moduleList, ItemClickListener itemClickListener) {
		mContext = context;
		mModuleList = moduleList;
		mItemClickListener = itemClickListener;
	}

	@Override
	public DeviceModuleAdapter.DeviceModuleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		int layoutId = 0;
		switch (viewType) {
			case VIEW_TYPE_SPACE:
				layoutId = R.layout.item_list_module_space;
				break;
			case VIEW_TYPE_ITEM:
				layoutId = R.layout.item_list_device_module;
				break;
		}
		View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
		return new DeviceModuleAdapterViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final DeviceModuleAdapter.DeviceModuleAdapterViewHolder holder, int position) {
		switch (getItemViewType(position)) {
			case VIEW_TYPE_ITEM: {

				final Module module = mModuleList.get(position);
				holder.mModuleId = module.getId();

				SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
				UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mContext);

				holder.mTitle.setText(module.getName(mContext));
				if (unitsHelper != null) {
					holder.mValue.setText(String.format("%s %s",
							unitsHelper.getStringValue(module.getValue()),
							unitsHelper.getStringUnit(module.getValue())));
				}
				holder.mIcon.setImageResource(module.getIconResource(IconResourceType.DARK));

				if (module.isActuator()) {
					if (module.getValue() instanceof EnumValue) {
						List<EnumValue.Item> items = ((EnumValue) module.getValue()).getEnumItems();

						if (items.size() > 2) {
							holder.mButton.setVisibility(View.VISIBLE);
							holder.mButton.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									mItemClickListener.onButtonChangeState(holder.mModuleId);
								}
							});
						} else {
							holder.mSwitch.setVisibility(View.VISIBLE);
							holder.mSwitch.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									mItemClickListener.onSwitchChange(holder.mModuleId);
								}
							});
						}
					} else {
						holder.mButton.setVisibility(View.VISIBLE);
						holder.mButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								mItemClickListener.onButtonSetNewValue(holder.mModuleId);
							}
						});
					}
				} else {
					holder.mButton.setVisibility(View.GONE);
					holder.mSwitch.setVisibility(View.GONE);
				}

				holder.mView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mItemClickListener.onItemClick(holder.mModuleId);
					}
				});
				break;
			}
			case VIEW_TYPE_SPACE:
				break;
		}
	}

	@Override
	public int getItemCount() {
		return mModuleList.size();
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0 && mFirstItemSpace) ? VIEW_TYPE_SPACE : VIEW_TYPE_ITEM;
	}

	public interface ItemClickListener {
		void onItemClick(String moduleId);

		void onButtonChangeState(String moduleId);

		void onButtonSetNewValue(String moduleId);

		void onSwitchChange(String moduleId);
	}

	public void setUseFirstItemSpace(boolean useFirstItemSpace) {
		if (useFirstItemSpace) {
			mModuleList.add(0, mModuleList.get(0));
		}
		mFirstItemSpace = useFirstItemSpace;
	}

	public void swapModules(List<Module> modules) {
		mModuleList = modules;
		notifyDataSetChanged();
	}

	public class DeviceModuleAdapterViewHolder extends RecyclerView.ViewHolder {

		public String mModuleId;

		public final ImageView mIcon;
		public final TextView mTitle;
		public final TextView mValue;
		public final Button mButton;
		public final SwitchCompat mSwitch;
		public final View mView;

		public DeviceModuleAdapterViewHolder(View itemView) {
			super(itemView);
			mIcon = (ImageView) itemView.findViewById(R.id.list_module_item_icon);
			mTitle = (TextView) itemView.findViewById(R.id.list_module_item_title);
			mValue = (TextView) itemView.findViewById(R.id.list_module_item_value);
			mButton = (Button) itemView.findViewById(R.id.list_device_module_item_set_button);
			mSwitch = (SwitchCompat) itemView.findViewById(R.id.list_device_module_item_switch);
			mView = itemView;
		}
	}
}
