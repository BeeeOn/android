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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.extension.watches.smartwatch2.SW2ExtensionService;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.model.LocationsModel;
import com.rehivetech.beeeon.util.Log;
import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * ListControlExtension displays a scrollable list, based on a string array. Tapping on list items opens a swipable detail view.
 */
public class ListLocationControlExtension extends ManagedControlExtension {

	public static final String EXTRA_ADAPTER_ID = "ADAPTER_ID";

	private Adapter mAdapter;
	private List<Location> mLocations;
	private String mAdapterId;

	private Bundle[] mMenuItemsIcons = new Bundle[1];

	/**
	 * first actualization is not forced to update values, second (sync button click) is already forced
	 */
	private boolean mForceUpdate = false;

	private static final int MENU_REFRESH = 1;

	/**
	 * @see ManagedControlExtension#ManagedControlExtension(Context, String, ControlManagerCostanza, Intent)
	 */
	public ListLocationControlExtension(Context context, String hostAppPackageName, ControlManagerSmartWatch2 controlManager, Intent intent) {
		super(context, hostAppPackageName, controlManager, intent);
		Log.d(SW2ExtensionService.LOG_TAG, "AdaptersListControl constructor");
		initializeMenus();

		mLocations = new ArrayList<Location>();

		mAdapterId = getIntent().getStringExtra(EXTRA_ADAPTER_ID);
		if (mAdapterId == null) {
			mControlManager.onBack();
			return;
		}
		actualize();
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
	public void onResume() {
		Log.d(SW2ExtensionService.LOG_TAG, "onResume");

		Bundle b1 = new Bundle();
		b1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.list_title);
		b1.putString(Control.Intents.EXTRA_TEXT, mContext.getString(R.string.choose_location));

		Bundle[] data = new Bundle[1];

		data[0] = b1;

		showLayout(R.layout.sw2_list_title, data);

		if (mAdapterId == null) {
			mControlManager.onBack();
			return;
		}

		sendListCount(R.id.listView, mLocations.size());
	}

	@Override
	public void onPause() {
		super.onPause();
		// Position is saved into Control's Intent, possibly to be used later.
		// getIntent().putExtra(GalleryControlExtension.EXTRA_INITIAL_POSITION,
		// mLastKnowPosition);
	}

	@Override
	public void onSwipe(int direction) {
		if (direction == Control.Intents.SWIPE_DIRECTION_RIGHT) {
			Intent intent = new Intent(mContext, ListAdapterControlExtension.class);
			mControlManager.previousScreen(intent);
		}
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
	public void onListItemSelected(ControlListItem listItem) {
		super.onListItemSelected(listItem);
		// We save the last "selected" position, this is the current visible
		// list item index. The position can later be used on resume
		// mLastKnowPosition = listItem.listItemPosition;
	}

	@Override
	public void onListItemClick(final ControlListItem listItem, final int clickType, final int itemLayoutReference) {
		Log.d(SW2ExtensionService.LOG_TAG, "Item clicked. Position " + listItem.listItemPosition + ", itemLayoutReference " + itemLayoutReference + ". Type was: "
				+ (clickType == Control.Intents.CLICK_TYPE_SHORT ? "SHORT" : "LONG"));

		if (clickType == Control.Intents.CLICK_TYPE_SHORT) {
			// Here we pass the item position to the next control. It would
			// also be possible to put some unique item id in the listitem and
			// pass listItem.listItemId here.
			// intent.putExtra(GalleryTestControl.EXTRA_INITIAL_POSITION,
			// listItem.listItemPosition);
			// mControlManager.startControl(intent);
			List<Facility> facilities = mController.getFacilitiesModel().getFacilitiesByLocation(mAdapter.getId(), mLocations.get(listItem.listItemPosition).getId());
			Intent intent;
			if (facilities.size() < 1) {
				intent = new Intent(mContext, TextControl.class);
				intent.putExtra(TextControl.EXTRA_TEXT, mContext.getString(R.string.no_sensor_available));
			} else {
				intent = new Intent(mContext, ListSensorControlExtension.class);
				intent.putExtra(ListSensorControlExtension.EXTRA_ADAPTER_ID, mAdapter.getId());
				intent.putExtra(ListSensorControlExtension.EXTRA_LOCATION_NAME, mLocations.get(listItem.listItemPosition).getId());
			}
			mControlManager.startControl(intent);
		}
	}

	protected ControlListItem createControlListItem(int position) {

		ControlListItem item = new ControlListItem();
		item.layoutReference = R.id.listView;
		item.dataXmlLayout = R.layout.sw2_item_location;
		item.listItemPosition = position;
		// We use position as listItemId. Here we could use some other unique id
		// to reference the list data
		item.listItemId = position;

		// Icon data
		Bundle iconBundle = new Bundle();
		iconBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.thumbnail);

		iconBundle.putString(Control.Intents.EXTRA_DATA_URI, ExtensionUtils.getUriString(mContext, mLocations.get(position).getIconResource()));

		Bundle headerBundle = new Bundle();
		headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.title);

		headerBundle.putString(Control.Intents.EXTRA_TEXT, mLocations.get(position).getName());

		item.layoutData = new Bundle[2];
		item.layoutData[0] = headerBundle;
		item.layoutData[1] = iconBundle;

		return item;
	}

	private void actualize() {
		Thread thLoc = new Thread(new Runnable() {
			@Override
			public void run() {

				mAdapter = mController.getAdaptersModel().getAdapter(mAdapterId);

				LocationsModel locationsModel = mController.getLocationsModel();
				locationsModel.reloadLocationsByAdapter(mAdapterId, mForceUpdate);
				mLocations = locationsModel.getLocationsByAdapter(mAdapterId);

				mForceUpdate = true;

				resume();

			}
		});
		thLoc.start();
	}

}
