/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (C) 2013-2014 Sony Mobile Communications AB

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

 * Neither the name of the Sony Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

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

package com.sonyericsson.extras.liveware.extension.util.registration;

import android.content.Context;
import android.os.AsyncTask;

import com.sonyericsson.extras.liveware.extension.util.Dbg;

/**
 * Perform extension registration or update in background
 */
public class RegisterExtensionTask extends AsyncTask<Void, Void, Boolean> {

    private final Context mContext;

    private final RegistrationInformation mRegistrationInformation;

    private IRegisterCallback mRegisterInterface;

    private final boolean mOnlySources;

    /**
     * Create register extension task
     *
     * @param context The context
     * @param registrationInformation Information needed during registration
     * @param registerInterface Registration callback interface
     * @param onlySources True if only sources shall be refreshed. False if full
     *            registration.
     */
    public RegisterExtensionTask(Context context, RegistrationInformation registrationInformation,
            IRegisterCallback registerInterface, boolean onlySources) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        mContext = context;

        if (registrationInformation == null) {
            throw new IllegalArgumentException("registrationInformation == null");
        }
        mRegistrationInformation = registrationInformation;
        if (registerInterface == null) {
            throw new IllegalArgumentException("registerInterface == null");
        }
        mRegisterInterface = registerInterface;

        mOnlySources = onlySources;
    }

    /**
     * Set register interface. Used to set interface to null to handle the case
     * when the service is destroyed before onPostExecute is executed.
     *
     * @param registerInterface The register interface
     */
    public void setRegisterInterface(IRegisterCallback registerInterface) {
        mRegisterInterface = registerInterface;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        RegistrationHelper helper = new RegistrationHelper(mContext, mRegistrationInformation);
        return helper.performRegistration(mOnlySources);
    }

    @Override
    protected void onPostExecute(Boolean registrationSuccess) {
        if (mRegisterInterface != null) {
            mRegisterInterface.onExtensionRegisterResult(mOnlySources, registrationSuccess);
        }
    }

    @Override
    protected void onCancelled() {
        if (Dbg.DEBUG) {
            Dbg.d("Registration task cancelled");
        }
    }
}
