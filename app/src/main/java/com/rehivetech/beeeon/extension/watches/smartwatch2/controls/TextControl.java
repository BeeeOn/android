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

import com.sonyericsson.extras.liveware.aef.control.Control;

import com.rehivetech.beeeon.R;

/**
 * GalleryTestControl displays a swipeable gallery, based on a string array.
 */
public class TextControl extends ManagedControlExtension {

	public final static String EXTRA_TEXT = "TEXT";

	public TextControl(Context context, String hostAppPackageName, ControlManagerSmartWatch2 controlManager, Intent intent) {
		super(context, hostAppPackageName, controlManager, intent);
	}

	@Override
	public void onResume() {
		String text = getIntent().getStringExtra(EXTRA_TEXT);
		if (text == null) {
			mControlManager.onBack();
			return;
		}

		Bundle b1 = new Bundle();
		b1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.text_only);
		b1.putString(Control.Intents.EXTRA_TEXT, text);

		Bundle[] data = new Bundle[1];

		data[0] = b1;

		showLayout(R.layout.sw2_text_only, data);
	}
}
