package com.rehivetech.beeeon.gui.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.model.entity.Server;
import com.rehivetech.beeeon.util.Utils;

import butterknife.ButterKnife;
import icepick.State;
import io.realm.Realm;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public class ServerDetailDialog extends BaseBeeeOnDialog {
	private static final String TAG = ServerDetailDialog.class.getSimpleName();
	private static final String ARG_SERVER_ID = "server_id";
	public static final int REQUEST_CERTIFICATE_PICK = 10;

	private long mServerId;
	private Realm mRealm;
	private Server mServer;

	private TextInputLayout mNameView;
	private TextInputLayout mPortView;
	private TextInputLayout mHostView;
	private TextInputLayout mVerifyView;
	private Button mCertificateButtonView;
	private TextView mCertificateErrorView;

	// statefull information
	@State public String mServerName;
	@State public String mServerPort;
	@State public String mCertificateUri;
	@State public String mServerHost;
	@State public String mServerVerify;

	/**
	 * Shows dialog for editting project
	 *
	 * @param context         dialog created from
	 * @param fragmentManager for maintaing fragment
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
	 * @param context         dialog created from
	 * @param fragmentManager for maintaing fragment
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

	/**
	 * Dialog fragment was created
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		mServerId = arguments.getLong(ARG_SERVER_ID);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mRealm = Realm.getDefaultInstance();
		Server server = mRealm.where(Server.class).equalTo("id", mServerId).findFirst();
		mServer = server == null ? new Server(Utils.autoIncrement(mRealm, Server.class)) : server;

		if (savedInstanceState == null) {
			mServerName = mServer.name;
			mServerPort = String.valueOf(mServer.port);
			mServerHost = mServer.address;
			mServerVerify = mServer.certVerifyUrl;
		}

		fillUI();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mRealm.close();
	}

	/**
	 * Fills UI with data from DB (or new data)
	 */
	private void fillUI() {
		if (mNameView.getEditText() != null) {
			mNameView.getEditText().setText(mServerName);
		}

		if (mPortView.getEditText() != null) {
			mPortView.getEditText().setText(mServerPort);
		}

		if (mHostView.getEditText() != null) {
			mHostView.getEditText().setText(mServerHost);
		}

		if (mVerifyView.getEditText() != null) {
			mVerifyView.getEditText().setText(mServerVerify);
		}
	}

	/**
	 * Saves views data as state
	 *
	 * @param outState to be saved
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		mServerPort = mPortView.getEditText().getText().toString();
		mServerName = mNameView.getEditText().getText().toString();
		mServerHost = mHostView.getEditText().getText().toString();
		mServerVerify = mVerifyView.getEditText().getText().toString();
		super.onSaveInstanceState(outState);
	}

	/**
	 * Gets server object which should be alwasy filled with id (either generated or existing one)
	 *
	 * @return server object
	 */
	@NonNull
	public Server getServer() {
		return mServer;
	}

	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder parentBuilder) {
		Builder builder = super.build(parentBuilder);

		mNameView = ButterKnife.findById(mRootView, R.id.server_name);
		mPortView = ButterKnife.findById(mRootView, R.id.server_port);
		mHostView = ButterKnife.findById(mRootView, R.id.server_host);
		mVerifyView = ButterKnife.findById(mRootView, R.id.server_url_verify);


		// delete button
		if (Server.isEditable(mServerId)) {
			setDeleteButton(builder, getNeutralButtonText());
		}

		mCertificateErrorView = ButterKnife.findById(mRootView, R.id.server_certificate_button_error);

		mCertificateButtonView = ButterKnife.findById(mRootView, R.id.server_certificate_button);
		mCertificateButtonView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkStoragePermission()) {
					pickFile();
				}
			}
		});

		return builder;
	}

	/**
	 * Checks storage permission and setups result for specified activity
	 *
	 * @return if permitted
	 */
	public boolean checkStoragePermission() {
		// on older devices we don't have to check it
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.PERMISSION_CODE_STORAGE);
			return false;
		}
		return true;
	}

	/**
	 * Result from requesting permission
	 *
	 * @param requestCode  which was send
	 * @param permissions  were asked for
	 * @param grantResults granted
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case Constants.PERMISSION_CODE_STORAGE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission granted
					pickFile();
				} else {
					// permission denied
					Toast.makeText(getContext(), R.string.permission_camera_warning, Toast.LENGTH_LONG).show();
				}
				break;
		}
	}

	/**
	 * Picks file from storage (MUST CHECK FOR PERMISSIONS!)
	 */
	private void pickFile() {
		Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
		getIntent.setType("application/x-x509-ca-cert");
		getIntent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent chooserIntent = Intent.createChooser(getIntent, "Select certificate");

//		Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		pickIntent.setType("image/*");
//		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

		startActivityForResult(chooserIntent, REQUEST_CERTIFICATE_PICK);
	}

	/**
	 * Activity resulted -> handles result of picking file
	 *
	 * @param requestCode handles only REQUEST_CERTIFICATE_PICK
	 * @param resultCode  #Activity.RESULT_OK or #Activity.RESULT_CANCEL
	 * @param data        uri with file
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CERTIFICATE_PICK:
				if (resultCode == Activity.RESULT_OK) {
					mCertificateUri = data.getDataString();
					setCertificateError(null);
				} else if (resultCode == Activity.RESULT_CANCELED) {
					// TODO
				}

				break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Handles showing certificate button error
	 *
	 * @param error if null, hides error
	 */
	public void setCertificateError(@Nullable String error) {
		if (error == null) {
			mCertificateErrorView.setVisibility(View.GONE);
			mCertificateButtonView.setTextColor(ContextCompat.getColor(getActivity(), R.color.beeeon_primary_dark));
			return;
		}

		// sets the error with proper styles
		mCertificateErrorView.setVisibility(View.VISIBLE);
		mCertificateErrorView.setText(error);
		mCertificateButtonView.requestFocus();
		mCertificateButtonView.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
	}

	/**
	 * Dialog builder
	 */
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
