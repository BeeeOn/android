package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.rehivetech.beeeon.R;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public class ServerDetailDialog extends BaseFragmentDialog {

	/**
	 * Shows dialog for editting project
	 *
	 * @param context
	 * @param fragmentManager
	 */
	public static void showCreate(Context context, FragmentManager fragmentManager) {
		new ServerDetailDialogBuilder(context, fragmentManager)
				.setTitle(R.string.server_title_create)
				.setPositiveButtonText(R.string.dialog_create)
				.setNegativeButtonText(R.string.dialog_cancel)
				.show();
	}

	public static void showEdit(Context context, FragmentManager fragmentManager) {
		new ServerDetailDialogBuilder(context, fragmentManager)
				.setTitle(R.string.server_title_create)
				.setPositiveButtonText(R.string.dialog_create)
				.setNegativeButtonText(R.string.dialog_cancel)
				.setNeutralButtonText(R.string.dialog_delete)
				.show();
	}

	@Override
	public int getLayoutResource() {
		return R.layout.dialog_server_detail;
	}

	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder parentBuilder) {
		Builder builder = super.build(parentBuilder);

//		mRootView.findViewById()

		return builder;
	}

	public static class ServerDetailDialogBuilder extends BaseFragmentDialogBuilder {
		private long mProjectId;
		private long mAuthorId;

		public ServerDetailDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager);
		}

		@Override
		protected Bundle prepareArguments() {
			Bundle args = super.prepareArguments();
//			args.putLong(ARG_PROJECT_ID, mProjectId);
//			args.putLong(ARG_AUTHOR_ID, mAuthorId);
			return args;
		}
	}
}
