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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;
import cz.vutbr.fit.intelligenthomeanywhere.extension.watches.smartwatch2.SW2ExtensionService;

/**
 * ListControlExtension displays a scrollable list, based on a string array.
 * Tapping on list items opens a swipable detail view.
 */
public class ListSensorControlExtension extends ManagedControlExtension {

	public static final String EXTRA_ADAPTER_ID = "ADAPTER_ID";
	public static final String EXTRA_LOCATION_NAME = "LOCATION_NAME";

	private List<BaseDevice> mDevices;
	
    /**
     * @see ManagedControlExtension#ManagedControlExtension(Context, String,
     *      ControlManagerCostanza, Intent)
     */
    public ListSensorControlExtension(Context context, String hostAppPackageName,
            ControlManagerSmartWatch2 controlManager, Intent intent) {
        super(context, hostAppPackageName, controlManager, intent);
        Log.d(SW2ExtensionService.LOG_TAG, "AdaptersListControl constructor");
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

        
        String adapterId = getIntent().getStringExtra(EXTRA_ADAPTER_ID);
        String locationStr = getIntent().getStringExtra(EXTRA_LOCATION_NAME);
        if (adapterId == null || locationStr == null) {
        	mControlManager.onBack();
        	return;
        }
        
        mDevices = mController.getAdapter(adapterId).getDevicesByLocation(locationStr);
        
        
        sendListCount(R.id.listView, mDevices.size());

    }

    @Override
    public void onPause() {
        super.onPause();
        // Position is saved into Control's Intent, possibly to be used later.
//        getIntent().putExtra(GalleryTestControl.EXTRA_INITIAL_POSITION, mLastKnowPosition);
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
    public void onListItemSelected(ControlListItem listItem) {
        super.onListItemSelected(listItem);
        // We save the last "selected" position, this is the current visible
        // list item index. The position can later be used on resume
//        mLastKnowPosition = listItem.listItemPosition;
    }

    @Override
    public void onListItemClick(final ControlListItem listItem, final int clickType,
            final int itemLayoutReference) {
        Log.d(SW2ExtensionService.LOG_TAG, "Item clicked. Position " + listItem.listItemPosition
                + ", itemLayoutReference " + itemLayoutReference + ". Type was: "
                + (clickType == Control.Intents.CLICK_TYPE_SHORT ? "SHORT" : "LONG"));

        if (clickType == Control.Intents.CLICK_TYPE_SHORT) {
            //Intent intent = new Intent(mContext, GalleryTestControl.class);
            // Here we pass the item position to the next control. It would
            // also be possible to put some unique item id in the listitem and
            // pass listItem.listItemId here.
        	//intent.putExtra(GalleryTestControl.EXTRA_INITIAL_POSITION, listItem.listItemPosition);
            //mControlManager.startControl(intent);
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
        iconBundle.putString(Control.Intents.EXTRA_DATA_URI,
                ExtensionUtils.getUriString(mContext, mDevices.get(position).getTypeIconResource()));        
        
        Bundle headerBundle = new Bundle();
        headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.title);
        
        headerBundle.putString(Control.Intents.EXTRA_TEXT, mDevices.get(position).getName());

        item.layoutData = new Bundle[2];
        item.layoutData[0] = headerBundle;
        item.layoutData[1] = iconBundle;

        return item;
    }

}
