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

import com.rehivetech.beeeon.controller.Controller;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;

public class ManagedControlExtension extends ControlExtension {

	/**
	 * Name for extra data put in ManagedControlExtension intents. Identifies boolean data. If the following boolean value is set to true, the new extension is not kept in the history stack. As soon
	 * as the user navigates away from it, the extension is not available on the back stack.
	 */
	public static final String EXTENSION_NO_HISTORY = "EXTENSION_NO_HISTORY";
	/**
	 * Name for extra data put in ManagedControlExtension intents. Identifies boolean data. If the following boolean value is set to true, extension handles presses of back button. As this can break
	 * the extension's navigation pattern this should be used with caution;
	 */
	public static final String EXTENSION_OVERRIDES_BACK = "EXTENSION_OVERRIDES_BACK";
	private Intent mIntent;
	protected ControlManagerSmartWatch2 mControlManager;

	/**
	 * Constructor for ManagedControlExtension. Should not be called directly, is called by ControlManager.
	 *
	 * @param context
	 * @param hostAppPackageName
	 * @param controlManager     the ControlManager that handles this extension's lifecycle
	 * @param intent             The intent used to handle the state of the ManagedControlExtension
	 */
	public ManagedControlExtension(Context context, String hostAppPackageName, ControlManagerSmartWatch2 controlManager, Intent intent) {
		super(context, hostAppPackageName);
		this.mIntent = intent;
		mControlManager = controlManager;
	}

	/**
	 * @return Return the intent that started this controlExtension.
	 */
	public Intent getIntent() {
		return mIntent;
	}

}
