package com.rehivetech.beeeon.gui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author martin
 * @since 20.06.2016
 */
public class DeviceDetailInfoDialog extends BaseBeeeOnDialog {

	private static final String ARG_UUID = "device_uuid";
	private static final String ARG_MANUFACTURE = "device_manufacture";

	@BindView(R.id.device_info_uuid)
	TextView mUuidText;
	@BindView(R.id.device_info_manufacture)
	TextView mManufactureText;

	public String mUuid;
	public String mManufacture;

	public static void showDialog(Context context, FragmentManager fragmentManager, String uuid, String manufacture) {
		new DeviceDetailInfoDialogBuilder(context, fragmentManager)
				.setDeviceUuid(uuid)
				.setDeviceManufacture(manufacture)
				.setTitle(R.string.device_detail_info_dialog_title)
				.show();
	}

	@Override
	public int getLayoutResource() {
		return R.layout.dialog_device_info;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		mUuid = args.getString(ARG_UUID);
		mManufacture = args.getString(ARG_MANUFACTURE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mUuidText.setText(mUuid);
		mManufactureText.setText(mManufacture);
	}

	@Override
	protected Builder build(Builder parentBuilder) {
		Builder builder = super.build(parentBuilder);

		ButterKnife.bind(this, mRootView);

		return builder;
	}

	/**
	 * Dialog builder
	 */
	public static class DeviceDetailInfoDialogBuilder extends BaseBeeeOnDialogBuilder {

		private String mUuid;
		private String mManufacture;

		public DeviceDetailInfoDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager, DeviceDetailInfoDialog.class);
		}

		public DeviceDetailInfoDialogBuilder setDeviceUuid(String uuid) {
			mUuid = uuid;
			return this;
		}

		public DeviceDetailInfoDialogBuilder setDeviceManufacture(String manufacture) {
			mManufacture = manufacture;
			return this;
		}

		@Override
		protected Bundle prepareArguments() {
			Bundle bundle = super.prepareArguments();
			bundle.putString(ARG_UUID, mUuid);
			bundle.putString(ARG_MANUFACTURE, mManufacture);

			return bundle;
		}
	}
}
