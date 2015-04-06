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

package com.rehivetech.beeeon.extension.watches.smartwatch2.controls;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.extension.watches.smartwatch2.SW2ExtensionService;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.UnitsHelper;

/**
 * ListControlExtension displays a scrollable list, based on a string array. Tapping on list items opens a swipable detail view.
 */
public class ListSensorControlExtension extends ManagedControlExtension {

	public static final String EXTRA_ADAPTER_ID = "ADAPTER_ID";
	public static final String EXTRA_LOCATION_NAME = "LOCATION_NAME";

	private List<Device> mDevices;
	private String mLocationStr;
	private Adapter mAdapter;
	private String mAdapterId;

	private Bundle[] mMenuItemsIcons = new Bundle[1];

	private static final int MENU_REFRESH = 1;

	protected int mLastKnowPosition = 0;

	/**
	 * @see ManagedControlExtension#ManagedControlExtension(Context, String, ControlManagerCostanza, Intent)
	 */
	public ListSensorControlExtension(Context context, String hostAppPackageName, ControlManagerSmartWatch2 controlManager, Intent intent) {
		super(context, hostAppPackageName, controlManager, intent);
		Log.d(SW2ExtensionService.LOG_TAG, "AdaptersListControl constructor");
		initializeMenus();

		mAdapterId = getIntent().getStringExtra(EXTRA_ADAPTER_ID);
		mLocationStr = getIntent().getStringExtra(EXTRA_LOCATION_NAME);
		if (mAdapterId == null || mLocationStr == null) {
			mControlManager.onBack();
			return;
		}

		mDevices = new ArrayList<Device>();
		actualize();
	}

	@Override
	public void onResume() {
		Log.d(SW2ExtensionService.LOG_TAG, "onResume");

		Bundle b1 = new Bundle();
		b1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.list_title);
		b1.putString(Control.Intents.EXTRA_TEXT, mContext.getString(R.string.choose_sensor));

		Bundle[] data = new Bundle[1];

		data[0] = b1;

		showLayout(R.layout.sw2_list_title, data);

		if (mAdapterId == null || mLocationStr == null) {
			mControlManager.onBack();
			return;
		}

		sendListCount(R.id.listView, mDevices.size());

		// If requested, move to the correct position in the list.
		int startPosition = getIntent().getIntExtra(GalleryControlExtension.EXTRA_INITIAL_POSITION, 0);
		mLastKnowPosition = startPosition;
		sendListPosition(R.id.listView, startPosition);
	}

	@Override
	public void onPause() {
		super.onPause();
		// Position is saved into Control's Intent, possibly to be used later.
		getIntent().putExtra(GalleryControlExtension.EXTRA_INITIAL_POSITION, mLastKnowPosition);
	}

	@Override
	public void onRequestListItem(final int layoutReference, final int listItemPosition) {
		Log.d(SW2ExtensionService.LOG_TAG, "onRequestListItem() - position " + listItemPosition);
		if (layoutReference != -1 && listItemPosition != -1 && layoutReference == R.id.listView) {
			ControlListItem item = createControlListItem(listItemPosition);
			if (item != null) {
				sendListItem(item);
			}
		}
	}

	private void initializeMenus() {
		mMenuItemsIcons[0] = new Bundle();
		mMenuItemsIcons[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_REFRESH);
		mMenuItemsIcons[0].putString(Control.Intents.EXTRA_MENU_ITEM_ICON, ExtensionUtils.getUriString(mContext, R.drawable.sync_white));
	}

	@Override
	public void onMenuItemSelected(final int menuItem) {
		Log.d(SW2ExtensionService.LOG_TAG, "onMenuItemSelected() - menu item " + menuItem);
		if (menuItem == MENU_REFRESH) {
			// clearDisplay();
			actualize();
		}
	}

	@Override
	public void onKey(final int action, final int keyCode, final long timeStamp) {
		Log.d(SW2ExtensionService.LOG_TAG, "onKey()");
		if (action == Control.Intents.KEY_ACTION_RELEASE && keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
			showMenu(mMenuItemsIcons);
		} else {
			mControlManager.onKey(action, keyCode, timeStamp);
		}
	}

	@Override
	public void onSwipe(int direction) {
		if (direction == Control.Intents.SWIPE_DIRECTION_RIGHT) {
			Intent intent = new Intent(mContext, ListLocationControlExtension.class);
			intent.putExtra(ListLocationControlExtension.EXTRA_ADAPTER_ID, mAdapter.getId());
			mControlManager.previousScreen(intent);
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
	public void onListItemClick(final ControlListItem listItem, final int clickType, final int itemLayoutReference) {
		Log.d(SW2ExtensionService.LOG_TAG, "Item clicked. Position " + listItem.listItemPosition + ", itemLayoutReference " + itemLayoutReference + ". Type was: "
				+ (clickType == Control.Intents.CLICK_TYPE_SHORT ? "SHORT" : "LONG"));

		if (clickType == Control.Intents.CLICK_TYPE_SHORT) {
			// Here we pass the item position to the next control. It would
			// also be possible to put some unique item id in the listitem and
			// pass listItem.listItemId here.
			Intent intent = new Intent(mContext, GalleryControlExtension.class);
			intent.putExtra(ListSensorControlExtension.EXTRA_ADAPTER_ID, mAdapter.getId());
			intent.putExtra(ListSensorControlExtension.EXTRA_LOCATION_NAME, mLocationStr);
			intent.putExtra(GalleryControlExtension.EXTRA_INITIAL_POSITION, listItem.listItemPosition);
			mControlManager.startControl(intent);
		}
	}

	protected ControlListItem createControlListItem(int position) {

		ControlListItem item = new ControlListItem();
		item.layoutReference = R.id.listView;
		item.dataXmlLayout = R.layout.sw2_item_list_sensor;
		item.listItemPosition = position;
		// We use position as listItemId. Here we could use some other unique id
		// to reference the list data
		item.listItemId = position;

		// Icon data
		Bundle iconBundle = new Bundle();
		iconBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.thumbnail);
		iconBundle.putString(Control.Intents.EXTRA_DATA_URI, ExtensionUtils.getUriString(mContext, mDevices.get(position).getIconResource()));

		Bundle headerBundle = new Bundle();
		headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.title);
		headerBundle.putString(Control.Intents.EXTRA_TEXT, mDevices.get(position).getName());

		Bundle valueBundle = new Bundle();
		valueBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.value);

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();
		UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mContext);
		if (unitsHelper != null) {
			valueBundle.putString(Control.Intents.EXTRA_TEXT, unitsHelper.getStringValueUnit(mDevices.get(position).getValue()));
		}

		item.layoutData = new Bundle[3];
		item.layoutData[0] = headerBundle;
		item.layoutData[1] = iconBundle;
		item.layoutData[2] = valueBundle;

		return item;
	}

	private void actualize() {
		Thread thLoc = new Thread(new Runnable() {
			@Override
			public void run() {

				mController.reloadAdapters(true);
				mAdapter = mController.getAdapter(mAdapterId);
				if (mAdapter != null) {
					mDevices = new ArrayList<Device>();

					mController.reloadFacilitiesByAdapter(mAdapterId, true);
					List<Facility> facilities = mController.getFacilitiesByLocation(mAdapter.getId(), mLocationStr);
					for (Facility facility : facilities) {
						mDevices.addAll(facility.getDevices());
					}

					if (mDevices != null) {
						resume();
					}
				}
			}
		});
		thLoc.start();
	}

}
