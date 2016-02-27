package com.rehivetech.beeeon.gui.adapter.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 27.2.16.
 */
public class DashboardModuleSelectAdapter extends RecyclerView.Adapter {

	public static final int LAYOUT_TYPE_MODULE = 0;
	public static final int LAYOUT_TYPE_DEVICE_NAME = 1;
	public static final int LAYOT_TYPE_DEVICE_GROUP_NAME = 2;

	private List<Object> mItems = new ArrayList<>();

	private Context mContext;
	private ItemClickListener mClickListener;

	private int mSelectedIndex = -1;

	public DashboardModuleSelectAdapter(Context context, ItemClickListener clickListener) {
		mContext = context;
		mClickListener = clickListener;
	}

	public void setItems(List<Object> items) {
		mItems.addAll(items);
		notifyDataSetChanged();
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
			((ModuleViewHolder) holder).bind((ModuleItem) item);
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

	public class ModuleViewHolder extends RecyclerView.ViewHolder {
		public final ImageView mIcon;
		public final TextView mName;
		public final View mRoot;

		public ModuleViewHolder(View itemView) {
			super(itemView);

			mRoot = itemView;
			mIcon = (ImageView) itemView.findViewById(R.id.item_add_dashboard_module_icon);
			mName = (TextView) itemView.findViewById(R.id.item_add_dashboard_module_name);
		}

		public void bind(final ModuleItem item) {

			Controller controller = Controller.getInstance(mContext);

			Module module = controller.getDevicesModel().getModule(item.mGateId, item.mAbsoluteId);

			if (module == null) {
				return;
			}

			mIcon.setImageResource(module.getIconResource(IconResourceType.DARK));
			mName.setText(module.getName(mContext));

			mRoot.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mClickListener != null) {
						mClickListener.onItemClick(item.mAbsoluteId);
					}
				}
			});
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
			 int styleResId = (item.mHeaderType == HeaderItem.ITEM_TYPE_DEVICE_NAME) ?
					R.style.TextAppearance_AppCompat_Title : R.style.TextAppearance_AppCompat_Subhead;

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
				mLabel.setTextAppearance(styleResId);
			} else {
				mLabel.setTextAppearance(itemView.getContext(), styleResId);
			}
		}
	}


	public static class ModuleItem {
		private String mGateId;
		private String mAbsoluteId;

		public ModuleItem(String absoluteId, String gateId) {
			mAbsoluteId = absoluteId;
			mGateId = gateId;
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
