/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.vutbr.fit.intelligenthomeanywhere.extension.watches.smartwatch2.controls;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Stack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.activity.LoginActivity;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;
import cz.vutbr.fit.intelligenthomeanywhere.extension.watches.smartwatch2.SW2ExtensionService;
import cz.vutbr.fit.intelligenthomeanywhere.network.GetGoogleAuth;

/**
 * The phone control manager manages which control to currently show on the
 * display. This class then forwards any life-cycle methods and events events to
 * the running control. This class handles API level 2 methods and an Intent
 * based ControlExtension history stack
 */
public class ControlManagerSmartWatch2 extends ControlManagerBase {

	private Stack<Intent> mControlStack;

	protected Controller mController;

	public ControlManagerSmartWatch2(Context context, String packageName) {
		super(context, packageName);
		mControlStack = new Stack<Intent>();

		mController = Controller.getInstance(mContext);

		Intent initialControlIntent;

		if (!mController.isLoggedIn()) {
			String lastEmail = mController.getLastEmail();
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				initialControlIntent = new Intent(mContext, TextControl.class);
				initialControlIntent.putExtra(TextControl.EXTRA_TEXT,
						mContext.getString(R.string.please_log_in));
				mCurrentControl = createControl(initialControlIntent);
				return;
			}
			if (!(lastEmail.length() < 1) && !mController.login(lastEmail)) {
				initialControlIntent = new Intent(mContext, TextControl.class);
				initialControlIntent.putExtra(TextControl.EXTRA_TEXT,
						mContext.getString(R.string.please_log_in));
				mCurrentControl = createControl(initialControlIntent);
				return;
			}

		}

		// Try to find default setting
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String adapterId = prefs
				.getString(Constants.SW2_PREF_DEF_ADAPTER, null);
		String strLocation = prefs.getString(Constants.SW2_PREF_DEF_LOCATION,
				null);

		Log.v(SW2ExtensionService.LOG_TAG, "Default adapter ID: "
				+ ((adapterId == null) ? "null" : adapterId));
		Log.v(SW2ExtensionService.LOG_TAG, "Default location: "
				+ ((strLocation == null) ? "null" : strLocation));

		// TODO zkontrolovat jestli neni cil prazdny
		if (adapterId != null) {
			Adapter adapter = Controller.getInstance(mContext).getAdapter(
					adapterId, false);
			// if default adapter is defined
			if (adapter != null) {
				if (strLocation != null) {
					List<BaseDevice> sensors = adapter
							.getDevicesByLocation(strLocation);
					if (sensors != null) {
						Intent intent = new Intent(mContext,
								ListSensorControlExtension.class);
						intent.putExtra(
								ListSensorControlExtension.EXTRA_ADAPTER_ID,
								adapter.getId());
						intent.putExtra(
								ListSensorControlExtension.EXTRA_LOCATION_NAME,
								strLocation);
						mCurrentControl = createControl(intent);
						return;
					}
				}
				Intent intent = new Intent(mContext,
						ListLocationControlExtension.class);
				intent.putExtra(ListLocationControlExtension.EXTRA_ADAPTER_ID,
						adapter.getId());
				mCurrentControl = createControl(intent);
				return;
			}
		}

		List<Adapter> adapters = mController.getAdapters();
		if (adapters.size() < 1) {
			initialControlIntent = new Intent(mContext, TextControl.class);
			initialControlIntent.putExtra(TextControl.EXTRA_TEXT,
					mContext.getString(R.string.no_adapter_available));
			mCurrentControl = createControl(initialControlIntent);
			return;
		} else if (adapters.size() < 2) {
			Intent intent = new Intent(mContext,
					ListLocationControlExtension.class);
			intent.putExtra(ListLocationControlExtension.EXTRA_ADAPTER_ID,
					adapters.get(0).getId());
			mCurrentControl = createControl(intent);
			return;
		}

		initialControlIntent = new Intent(mContext,
				ListAdapterControlExtension.class);
		mCurrentControl = createControl(initialControlIntent);
	}

	/**
	 * Get supported control width.
	 * 
	 * @param context
	 *            The context.
	 * @return the width.
	 */
	public static int getSupportedControlWidth(Context context) {
		return context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_2_control_width);
	}

	/**
	 * Get supported control height.
	 * 
	 * @param context
	 *            The context.
	 * @return the height.
	 */
	public static int getSupportedControlHeight(Context context) {
		return context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_2_control_height);
	}

	@Override
	public void onRequestListItem(int layoutReference, int listItemPosition) {
		Log.v(SW2ExtensionService.LOG_TAG, "onRequestListItem");
		if (mCurrentControl != null) {
			mCurrentControl
					.onRequestListItem(layoutReference, listItemPosition);
		}
	}

	@Override
	public void onListItemClick(ControlListItem listItem, int clickType,
			int itemLayoutReference) {
		Log.v(SW2ExtensionService.LOG_TAG, "onListItemClick");
		if (mCurrentControl != null) {
			mCurrentControl.onListItemClick(listItem, clickType,
					itemLayoutReference);
		}
	}

	@Override
	public void onListItemSelected(ControlListItem listItem) {
		Log.v(SW2ExtensionService.LOG_TAG, "onListItemSelected");
		if (mCurrentControl != null) {
			mCurrentControl.onListItemSelected(listItem);
		}
	}

	@Override
	public void onListRefreshRequest(int layoutReference) {
		Log.v(SW2ExtensionService.LOG_TAG, "onListRefreshRequest");
		if (mCurrentControl != null) {
			mCurrentControl.onListRefreshRequest(layoutReference);
		}
	}

	@Override
	public void onObjectClick(ControlObjectClickEvent event) {
		Log.v(SW2ExtensionService.LOG_TAG, "onObjectClick");
		if (mCurrentControl != null) {
			mCurrentControl.onObjectClick(event);
		}
	}

	@Override
	public void onKey(int action, int keyCode, long timeStamp) {
		Log.v(SW2ExtensionService.LOG_TAG, "onKey");

		if (action == Control.Intents.KEY_ACTION_RELEASE
				&& keyCode == Control.KeyCodes.KEYCODE_BACK) {
			Log.d(SW2ExtensionService.LOG_TAG,
					"onKey() - back button intercepted.");
			onBack();
		} else if (mCurrentControl != null) {
			super.onKey(action, keyCode, timeStamp);
		}
	}

	@Override
	public void onMenuItemSelected(int menuItem) {
		Log.v(SW2ExtensionService.LOG_TAG, "onMenuItemSelected");
		if (mCurrentControl != null) {
			mCurrentControl.onMenuItemSelected(menuItem);
		}
	}

	/**
	 * Closes the currently open control extension. If there is a control on the
	 * back stack it is opened, otherwise extension is closed.
	 */
	public void onBack() {
		Log.v(SW2ExtensionService.LOG_TAG, "onBack");
		if (!mControlStack.isEmpty()) {
			Intent backControl = mControlStack.pop();
			ControlExtension newControl = createControl(backControl);
			startControl(newControl);
		} else {
			stopRequest();
		}
	}

	protected void previousScreen(Intent intent) {
		if (!mControlStack.isEmpty()) {
			onBack();
		} else {
			ControlExtension newControl = createControl(intent);
			startControl(newControl);
		}
	}

	/**
	 * Start a new control. Any currently running control will be stopped and
	 * put on the control extension stack.
	 * 
	 * @param intent
	 *            the Intent used to create the ManagedControlExtension. The
	 *            intent must have the requested ManagedControlExtension as
	 *            component, e.g. Intent intent = new Intent(mContext,
	 *            CallLogDetailsControl.class);
	 */
	public void startControl(Intent intent) {
		addCurrentToControlStack();
		ControlExtension newControl = createControl(intent);
		startControl(newControl);
	}

	public void addCurrentToControlStack() {
		if (mCurrentControl != null
				&& mCurrentControl instanceof ManagedControlExtension) {
			Intent intent = ((ManagedControlExtension) mCurrentControl)
					.getIntent();
			boolean isNoHistory = intent.getBooleanExtra(
					ManagedControlExtension.EXTENSION_NO_HISTORY, false);
			if (isNoHistory) {
				// Not adding this control to history
				Log.d(SW2ExtensionService.LOG_TAG,
						"Not adding control to history stack");
			} else {
				Log.d(SW2ExtensionService.LOG_TAG,
						"Adding control to history stack");
				mControlStack.add(intent);
			}
		} else {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManageronly supports ManagedControlExtensions");
		}
	}

	private ControlExtension createControl(Intent intent) {
		ComponentName component = intent.getComponent();
		try {
			String className = component.getClassName();
			Log.d(SW2ExtensionService.LOG_TAG, "Class name:" + className);
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz
					.getConstructor(Context.class, String.class,
							ControlManagerSmartWatch2.class, Intent.class);
			if (ctor == null) {
				return null;
			}
			Object object = ctor.newInstance(new Object[] { mContext,
					mHostAppPackageName, this, intent });
			if (object instanceof ManagedControlExtension) {
				return (ManagedControlExtension) object;
			} else {
				Log.w(SW2ExtensionService.LOG_TAG,
						"Created object not a ManagedControlException");
			}

		} catch (SecurityException e) {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (NoSuchMethodException e) {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (IllegalArgumentException e) {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (InstantiationException e) {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (IllegalAccessException e) {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (InvocationTargetException e) {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		} catch (ClassNotFoundException e) {
			Log.w(SW2ExtensionService.LOG_TAG,
					"ControlManager: Failed in creating control", e);
		}
		return null;
	}

}
