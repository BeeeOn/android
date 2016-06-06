package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.model.entity.Server;
import com.rehivetech.beeeon.util.Utils;

import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public class ServerDetailDialog extends BaseBeeeOnDialog {
	private static final String ARG_SERVER_ID = "server_id";
	private static final int REQUEST_CERTIFICATE = 1;
	private static final int PERMISSION_STORAGE = 10;
	private static final String TAG = ServerDetailDialog.class.getSimpleName();

	private long mServerId;
	private Realm mRealm;
	private TextInputLayout mNameView;
	private TextInputLayout mPortView;
	private TextInputLayout mHostView;
	private Server mServer;

	/**
	 * Shows dialog for editting project
	 *
	 * @param context
	 * @param fragmentManager
	 */
	public static void showCreate(Context context, FragmentManager fragmentManager, int requestCode) {
		new ServerDetailDialogBuilder(context, fragmentManager)
				.setTitle(R.string.server_detail_title_create)
				.setPositiveButtonText(R.string.dialog_create)
				.setNegativeButtonText(R.string.dialog_cancel)
				.setRequestCode(requestCode)
				.show();
	}

	/**
	 * Shows editing of server
	 *
	 * @param context
	 * @param fragmentManager
	 */
	public static void showEdit(Context context, FragmentManager fragmentManager, int requestCode, long serverId) {
		new ServerDetailDialogBuilder(context, fragmentManager)
				.setServerId(serverId)
				.setTitle(R.string.server_detail_title_edit)
				.setPositiveButtonText(R.string.dialog_edit)
				.setNegativeButtonText(R.string.dialog_cancel)
				.setNeutralButtonText(R.string.dialog_delete)
				.setRequestCode(requestCode)
				.show();
	}

	/**
	 * Forces to specify layout which will be used for this type of dialogs
	 *
	 * @return Layout resource id
	 */
	@Override
	public int getLayoutResource() {
		return R.layout.dialog_server_detail;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mServerId = this.getArguments().getLong(ARG_SERVER_ID);
	}

	@Override
	public void onStart() {
		super.onStart();
		mRealm = Realm.getDefaultInstance();
		mServer = mRealm.where(Server.class).equalTo("id", mServerId).findFirst();
		fillUI();
	}

	private void fillUI() {
		if (mNameView.getEditText() != null) {
			mNameView.getEditText().setText(getServer().name);
		}

		if (mPortView.getEditText() != null) {
			mPortView.getEditText().setText(String.valueOf(getServer().port));
		}

		if (mHostView.getEditText() != null) {
			mHostView.getEditText().setText(getServer().address);
		}
	}

	/**
	 * Gets server object which should be alwasy filled with id (either generated or existing one)
	 *
	 * @return server object
	 */
	@NonNull
	public Server getServer() {
		if (mServer == null) {
			mServer = new Server(Utils.autoIncrement(mRealm, Server.class));
			mServer.port = Server.DEFAULT_PORT;
		}
		return mServer;
	}

	@Override
	public void onStop() {
		super.onStop();
		mRealm.close();
	}

	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder parentBuilder) {
		Builder builder = super.build(parentBuilder);

		mNameView = (TextInputLayout) mRootView.findViewById(R.id.server_name);
		mPortView = (TextInputLayout) mRootView.findViewById(R.id.server_port);
		mHostView = (TextInputLayout) mRootView.findViewById(R.id.server_host);

		// delete button
		if (Server.isEditable(mServerId)) {
			setDeleteButton(builder, getNeutralButtonText());
		}

		Button btn = ButterKnife.findById(mRootView, R.id.server_certificate_button);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickFileWithPermissionCheck();
			}
		});

		return builder;
	}

	private void pickFileWithPermissionCheck() {
		Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
		getIntent.setType("application/x-x509-ca-cert");
		getIntent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent chooserIntent = Intent.createChooser(getIntent, "Select certificate");

//		Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		pickIntent.setType("image/*");
//		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

		startActivityForResult(chooserIntent, REQUEST_CERTIFICATE);
	}

//	private void checkDataPermission() {
//		if (ContextCompat.checkSelfPermission(getActivity(),
//				Manifest.permission.READ_EXTERNAL_STORAGE)
//				!= PackageManager.PERMISSION_GRANTED) {
//			// Should we show an explanation?
//			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//					Manifest.permission.READ_EXTERNAL_STORAGE)) {
//
//				// Show an explanation to the user *asynchronously* -- don't block
//				// this thread waiting for the user's response! After the user
//				// sees the explanation, try again to request the permission.
//				ActivityCompat.requestPermissions(this,
//						new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//						PERMISSION_STORAGE);
//
//			} else {
//				// No explanation needed, we can request the permission.
//				if (DeviceUtils.getPermissionStorage()) {
//					startActivity(Utils.getAppSettingsIntent());
//				} else {
//					DeviceUtils.setPermissionStorage();
//					ActivityCompat.requestPermissions(this,
//							new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//							PERMISSION_STORAGE);
//				}
//			}
//		} else {
//			addImageAttachment();
//		}
//	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != REQUEST_CERTIFICATE) {
			Log.d(TAG, "Not handled activity result");
			return;
		}

		if (data == null) {
			Log.e(TAG, "No data from activity");
			return;
		}

		Uri uriData = data.getData();
		Toast.makeText(getActivity(), uriData.toString(), Toast.LENGTH_LONG).show();

		String imagePath;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//			imagePath = Utils.getImagePathOldApi(uriData);
		} else {
//			imagePath = Utils.getImagePathNewApi(uriData);
		}
//		String name = getFileNameFromPath();
//		if (name != null) {
//			attachmentsValue.setText(name);
	}

	public static class ServerDetailDialogBuilder extends BaseBeeeOnDialog.BaseBeeeOnDialogBuilder {
		private long mServerId;

		public ServerDetailDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager);
		}

		public ServerDetailDialogBuilder setServerId(long serverId) {
			mServerId = serverId;
			return this;
		}

		@Override
		protected Bundle prepareArguments() {
			Bundle args = super.prepareArguments();
			args.putLong(ARG_SERVER_ID, mServerId);
			return args;
		}
	}
}
