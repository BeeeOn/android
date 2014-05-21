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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView.OnClickListener;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.extension.watches.smartwatch2.SW2ExtensionService;
import cz.vutbr.fit.intelligenthomeanywhere.network.ActualUser;

/**
 * GalleryControl displays a swipeable gallery.
 */
public class GalleryControlExtension extends ManagedControlExtension {

	protected int mLastKnowPosition = 0;

	public static final String EXTRA_INITIAL_POSITION = "EXTRA_INITIAL_POSITION";
	public static final String EXTRA_ADAPTER_ID = "ADAPTER_ID";
	public static final String EXTRA_LOCATION_NAME = "LOCATION_NAME";

	private Adapter mAdapter;
	private String mLocationName;

	private List<BaseDevice> mDevices;

	private ControlViewGroup mLayout = null;

	/**
	 * Bundle for menu icons
	 */
	private Bundle[] mMenuItemsIcons = new Bundle[1];

	private static final int MENU_REFRESH = 1;

	/**
	 * Minimum battery state for showing red battery
	 */
	private static final int MIN_BATTERY_STATE = 10;

	/**
	 * @see ManagedControlExtension#ManagedControlExtension(Context, String,
	 *      ControlManagerCostanza, Intent)
	 */
	public GalleryControlExtension(Context context, String hostAppPackageName,
			ControlManagerSmartWatch2 controlManager, Intent intent) {
		super(context, hostAppPackageName, controlManager, intent);

		// setupClickables(context);
		initializeMenus();
	}

	private void initializeMenus() {
		mMenuItemsIcons[0] = new Bundle();
		mMenuItemsIcons[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID,
				MENU_REFRESH);
		mMenuItemsIcons[0].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
				ExtensionUtils.getUriString(mContext, R.drawable.sync_grey));
	}

	@Override
	public void onResume() {
		Log.d(SW2ExtensionService.LOG_TAG, "onResume");
		showLayout(R.layout.sw2_gallery, null);

		// If requested, move to the correct position in the list.
		int startPosition = getIntent().getIntExtra(EXTRA_INITIAL_POSITION, 0);
		mLastKnowPosition = startPosition;

		String adapterId = getIntent().getStringExtra(EXTRA_ADAPTER_ID);
		String locationStr = getIntent().getStringExtra(EXTRA_LOCATION_NAME);
		if (adapterId == null || locationStr == null) {
			mControlManager.onBack();
			return;
		}

		mDevices = mController.getAdapter(adapterId, false)
				.getDevicesByLocation(locationStr);

		sendListCount(R.id.gallery, mDevices.size());
		sendListPosition(R.id.gallery, startPosition);

	}

	@Override
	public void onPause() {
		super.onPause();
		// Position is saved into Control's Intent, possibly to be used later.
		getIntent().putExtra(EXTRA_INITIAL_POSITION, mLastKnowPosition);
	}

	@Override
	public void onRequestListItem(final int layoutReference,
			final int listItemPosition) {
		Log.d(SW2ExtensionService.LOG_TAG, "onRequestListItem() - position "
				+ listItemPosition);
		if (layoutReference != -1 && listItemPosition != -1
				&& layoutReference == R.id.gallery) {
			ControlListItem item = createControlListItem(listItemPosition);
			if (item != null) {
				sendListItem(item);
			}
		}
	}

	@Override
	public void onKey(final int action, final int keyCode, final long timeStamp) {
		Log.d(SW2ExtensionService.LOG_TAG, "onKey()");
		if (action == Control.Intents.KEY_ACTION_RELEASE
				&& keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
			showMenu(mMenuItemsIcons);
		} else {
			mControlManager.onKey(action, keyCode, timeStamp);
		}
	}

	@Override
	public void onMenuItemSelected(final int menuItem) {
		Log.d(SW2ExtensionService.LOG_TAG, "onMenuItemSelected() - menu item "
				+ menuItem);
		if (menuItem == MENU_REFRESH) {
			
			clearDisplay();
			// TODO
		}
	}

	@Override
	public void onListItemSelected(ControlListItem listItem) {
		super.onListItemSelected(listItem);
		// We save the last "selected" position, this is the current visible
		// list item index. The position can later be used on resume
		mLastKnowPosition = listItem.listItemPosition;
	}

	@Override
	public void onListItemClick(final ControlListItem listItem,
			final int clickType, final int itemLayoutReference) {
		Log.d(SW2ExtensionService.LOG_TAG, "Item clicked. Position "
				+ listItem.listItemPosition
				+ ", itemLayoutReference "
				+ itemLayoutReference
				+ ". Type was: "
				+ (clickType == Control.Intents.CLICK_TYPE_SHORT ? "SHORT"
						: "LONG"));
	}

	protected ControlListItem createControlListItem(int position) {

		ControlListItem item = new ControlListItem();
		item.layoutReference = R.id.gallery;
		item.dataXmlLayout = R.layout.sw2_item_gallery_sensor;
		item.listItemId = position;
		item.listItemPosition = position;

		BaseDevice curDevice = mDevices.get(position);

		// Title data
		Bundle syncBundle = new Bundle();
		syncBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE,
				R.id.sync_time);
		// TODO spravny cas tam dat
		String dateTime;
		DateFormat dateFormat;

		Calendar syncTime = Calendar.getInstance();
		Calendar actTime = Calendar.getInstance();

		actTime.add(Calendar.HOUR, -24);

		// If sync time is more that 24 ago, show only date. Show time
		// otherwise.
		if (actTime.before(syncTime)) {
			dateFormat = DateFormat.getTimeInstance();
		} else {
			dateFormat = DateFormat.getDateInstance();
		}

		dateTime = dateFormat.format(syncTime.getTime());
		syncBundle.putString(Control.Intents.EXTRA_TEXT, dateTime);

		// Title data
		Bundle headerBundle = new Bundle();
		headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE,
				R.id.gallery_title);
		headerBundle.putString(Control.Intents.EXTRA_TEXT, curDevice.getName());

		// Unit data
		Bundle unitBundle = new Bundle();
		unitBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.unit);
		unitBundle.putString(Control.Intents.EXTRA_TEXT,
				curDevice.getStringUnit(mContext));

		// Battery icon
		Bundle batteryBundle = new Bundle();
		if (curDevice.getBattery() < MIN_BATTERY_STATE) {
			batteryBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE,
					R.id.thumbnail);
			batteryBundle.putString(Control.Intents.EXTRA_DATA_URI,
					ExtensionUtils.getUriString(mContext, R.drawable.battery));
		}

		// Icon data
		Bundle iconBundle = new Bundle();
		iconBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE,
				R.id.thumbnail);
		iconBundle.putString(Control.Intents.EXTRA_DATA_URI,
				ExtensionUtils.getUriString(mContext, R.drawable.dev_teplota));
		// iconBundle.putString(Control.Intents.EXTRA_DATA_URI, ExtensionUtils
		// .getUriString(mContext, curDevice
		// .getTypeIconResource()));

		// Value data
		Bundle valueBundle = new Bundle();
		valueBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.value);
		// valueBundle.putString(Control.Intents.EXTRA_TEXT,
		// curDevice.getStringValueUnit(mContext));
		valueBundle.putString(Control.Intents.EXTRA_TEXT,
				curDevice.getStringValue());

		item.layoutData = new Bundle[6];
		item.layoutData[0] = headerBundle;
		item.layoutData[1] = iconBundle;
		item.layoutData[2] = valueBundle;
		item.layoutData[3] = batteryBundle;
		item.layoutData[4] = syncBundle;
		item.layoutData[5] = unitBundle;

		return item;
	}

}
