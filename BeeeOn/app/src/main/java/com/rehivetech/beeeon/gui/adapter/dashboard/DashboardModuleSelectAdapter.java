package com.rehivetech.beeeon.gui.adapter.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.household.device.Module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 27.2.16.
 */
public class DashboardModuleSelectAdapter extends RecyclerViewSelectableAdapter {

	public static final int LAYOUT_TYPE_MODULE = 0;
	public static final int LAYOUT_TYPE_DEVICE_NAME = 1;
	public static final int LAYOT_TYPE_DEVICE_GROUP_NAME = 2;

	private List<Object> mItems = new ArrayList<>();

	private Context mContext;
	private ItemClickListener mClickListener;

	public DashboardModuleSelectAdapter(Context context, ItemClickListener clickListener) {
		super(context);
		mContext = context;
		mClickListener = clickListener;
	}

	public void setItems(List<Object> items) {
		mItems.addAll(items);
		notifyDataSetChanged();
	}


	public void selectFirstModuleItem() {
		int i = 0;

		for (Object o : mItems) {
			if (o instanceof ModuleItem) {
				toggleSelection(i);
				break;
			}
			i++;
		}
	}
	public Object getItem(int index) {
		return mItems.get(index);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case LAYOUT_TYPE_DEVICE_NAME:
			case LAYOT_TYPE_DEVICE_GROUP_NAME: {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_dashboard_module_header, parent, false);
				return new HeaderViewHolder(view);
			}
			case LAYOUT_TYPE_MODULE: {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_dashboard_module, parent, false);
				return new ModuleViewHolder(view);
			}
		}
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		Object item = mItems.get(position);
		if (item instanceof HeaderItem) {
			((HeaderViewHolder) holder).bind((HeaderItem) item);
		} else {
			((ModuleViewHolder) holder).bind((ModuleItem) item, position);
		}
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	@Override
	public int getItemViewType(int position) {
		Object item = mItems.get(position);
		if (item instanceof HeaderItem) {
			return ((HeaderItem) item).mHeaderType == HeaderItem.ITEM_TYPE_DEVICE_NAME ? LAYOUT_TYPE_DEVICE_NAME : LAYOT_TYPE_DEVICE_GROUP_NAME;
		}
		else {
			return LAYOUT_TYPE_MODULE;
		}
	}

	public class ModuleViewHolder extends SelectableViewHolder {
		public final ImageView mIcon;
		public final TextView mName;
		public final CardView mRoot;

		public ModuleViewHolder(View itemView) {
			super(itemView);

			mRoot = (CardView) itemView;
			mIcon = (ImageView) itemView.findViewById(R.id.item_add_dashboard_module_icon);
			mName = (TextView) itemView.findViewById(R.id.item_add_dashboard_module_name);
		}

		public void bind(final ModuleItem item, final int position) {

			Controller controller = Controller.getInstance(mContext);

			Module module = controller.getDevicesModel().getModule(item.mGateId, item.mAbsoluteId);

			if (module == null) {
				return;
			}

			mIcon.setImageResource(module.getIconResource(isSelected(position) ? IconResourceType.WHITE : IconResourceType.DARK));
			mName.setText(module.getName(mContext));

			mRoot.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clearSelection();
					toggleSelection(position);
					if (mClickListener != null) {
						mClickListener.onItemClick(item.mAbsoluteId);
					}
				}
			});

			setSelected(isSelected(position));
		}

		@Override
		protected void setSelectedBackground(boolean isSelected) {
			mRoot.setSelected(isSelected);

			if (isSelected) {
				mRoot.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.beeeon_primary));
			} else {
				mRoot.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardview_light_background));
			}
		}
	}

	public class HeaderViewHolder extends  RecyclerView.ViewHolder {
		public final TextView mLabel;

		public HeaderViewHolder(View itemView) {
			super(itemView);

			mLabel = (TextView) itemView;
		}

		@SuppressWarnings("deprecation")
		@SuppressLint("PrivateResource")
		public void bind(HeaderItem item) {
			mLabel.setText(item.mName);

			int textColor = item.mHeaderType == HeaderItem.ITEM_TYPE_DEVICE_NAME ? R.color.black : R.color.beeeon_accent;

			mLabel.setTextColor(ContextCompat.getColor(mContext, textColor));
		}
	}


	public static class ModuleItem implements Parcelable{
		private String mGateId;
		private String mAbsoluteId;

		public ModuleItem(String absoluteId, String gateId) {
			mAbsoluteId = absoluteId;
			mGateId = gateId;
		}

		protected ModuleItem(Parcel in) {
			mGateId = in.readString();
			mAbsoluteId = in.readString();
		}

		public String getAbsoluteId() {
			return mAbsoluteId;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(mGateId);
			dest.writeString(mAbsoluteId);
		}

		public static final Creator<ModuleItem> CREATOR = new Creator<ModuleItem>() {
			public ModuleItem createFromParcel(Parcel source) {
				return new ModuleItem(source);
			}

			public ModuleItem[] newArray(int size) {
				return new ModuleItem[size];
			}
		};

		public static ModuleItem getEmpty() {
			return new ModuleItem("", "");
		}

		public boolean isEmpty() {
			return mGateId.isEmpty() || mAbsoluteId.isEmpty();
		}
	}

	public static class HeaderItem {

		public static final int ITEM_TYPE_DEVICE_NAME = 0;
		public static final int ITEM_TYPE_DEVICE_GROUP = 1;

		@Retention(RetentionPolicy.SOURCE)
		@IntDef({ITEM_TYPE_DEVICE_NAME, ITEM_TYPE_DEVICE_GROUP})
		public @interface HeaderType{}

		private String mName;
		private @HeaderType int mHeaderType;

		public HeaderItem(String name, @HeaderType int headerType) {
			mName = name;
			mHeaderType = headerType;
		}
	}

	public interface ItemClickListener {
		void onItemClick(String absoluteModuleId);
	}
}
