package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.model.entity.Server;
import com.rehivetech.beeeon.util.Utils;

import io.realm.Realm;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public class ServerDetailDialog extends BaseFragmentDialog {
	private static final String ARG_SERVER_ID = "server_id";
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
	public static void showCreate(Context context, FragmentManager fragmentManager) {
		new ServerDetailDialogBuilder(context, fragmentManager)
				.setTitle(R.string.server_detail_title_create)
				.setPositiveButtonText(R.string.dialog_create)
				.setNegativeButtonText(R.string.dialog_cancel)
				.show();
	}

	/**
	 * Shows editing of server
	 *
	 * @param context
	 * @param fragmentManager
	 */
	public static void showEdit(Context context, FragmentManager fragmentManager, long serverId) {
		new ServerDetailDialogBuilder(context, fragmentManager)
				.setServerId(serverId)
				.setTitle(R.string.server_detail_title_edit)
				.setPositiveButtonText(R.string.dialog_edit)
				.setNegativeButtonText(R.string.dialog_cancel)
				.setNeutralButtonText(R.string.dialog_delete)
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
			mServer.port = mServer.DEFAULT_PORT;
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

		return builder;
	}

	public static class ServerDetailDialogBuilder extends BaseFragmentDialogBuilder {
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
