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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlListItem;

import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.extension.watches.smartwatch2.SW2ExtensionService;

/**
 * GalleryTestControl displays a swipeable gallery, based on a string array.
 */
public class GalleryTestControl extends ManagedControlExtension {

    protected int mLastKnowPosition = 0;
    public final static String EXTRA_INITIAL_POSITION = "EXTRA_INITIAL_POSITION";

    /**
     * String array with dummy data to be displayed in gallery.
     */
    private String[] mGalleryContent = {
            "Detail Item 1", "Detail Item 2", "Detail Item 3", "Detail Item 4", "Detail Item 5",
            "Detail Item 6", "Detail Item 7", "Detail Item 8", "Detail Item 9", "Detail Item 10"
    };

    /**
     * @see ManagedControlExtension#ManagedControlExtension(Context, String,
     *      ControlManagerCostanza, Intent)
     */
    public GalleryTestControl(Context context, String hostAppPackageName,
            ControlManagerSmartWatch2 controlManager, Intent intent) {
        super(context, hostAppPackageName, controlManager, intent);
    }

    @Override
    public void onResume() {
        Log.d(SW2ExtensionService.LOG_TAG, "onResume");
        showLayout(R.layout.sw2_layout_test_gallery, null);
        sendListCount(R.id.gallery, mGalleryContent.length);

        // If requested, move to the correct position in the list.
        int startPosition = getIntent().getIntExtra(EXTRA_INITIAL_POSITION, 0);
        mLastKnowPosition = startPosition;
        sendListPosition(R.id.gallery, startPosition);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Position is saved into Control's Intent, possibly to be used later.
        getIntent().putExtra(EXTRA_INITIAL_POSITION, mLastKnowPosition);
    }

    @Override
    public void onRequestListItem(final int layoutReference, final int listItemPosition) {
        Log.d(SW2ExtensionService.LOG_TAG, "onRequestListItem() - position " + listItemPosition);
        if (layoutReference != -1 && listItemPosition != -1 && layoutReference == R.id.gallery) {
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
        mLastKnowPosition = listItem.listItemPosition;
    }

    @Override
    public void onListItemClick(final ControlListItem listItem, final int clickType,
            final int itemLayoutReference) {
        Log.d(SW2ExtensionService.LOG_TAG, "Item clicked. Position " + listItem.listItemPosition
                + ", itemLayoutReference " + itemLayoutReference + ". Type was: "
                + (clickType == Control.Intents.CLICK_TYPE_SHORT ? "SHORT" : "LONG"));
    }

    protected ControlListItem createControlListItem(int position) {

        ControlListItem item = new ControlListItem();
        item.layoutReference = R.id.gallery;
        item.dataXmlLayout = R.layout.sw2_item_gallery;
        item.listItemId = position;
        item.listItemPosition = position;

        // Header data
        Bundle headerBundle = new Bundle();
        headerBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.title);
        headerBundle.putString(Control.Intents.EXTRA_TEXT, mGalleryContent[position]);

        // Body data
        Bundle bodyBundle = new Bundle();
        bodyBundle.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.body);
        bodyBundle.putString(Control.Intents.EXTRA_TEXT, mGalleryContent[position]);

        item.layoutData = new Bundle[2];
        item.layoutData[0] = headerBundle;
        item.layoutData[1] = bodyBundle;

        return item;
    }

}
