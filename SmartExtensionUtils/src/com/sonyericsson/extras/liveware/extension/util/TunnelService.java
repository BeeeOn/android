/*
Copyright (c) 2014 Sony Mobile Communications AB.

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

package com.sonyericsson.extras.liveware.extension.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.tunnel.Tunnel;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService.ExtensionIntentSender;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService.LocalBinder;


/**
 * This service enables host apps to tunnel intents through a
 * Messenger instead of sending them over the BroadcastQueue
 * to prevent them from being delayed by the new queue behavior
 * in KitKat. It implements the SmartExtension {@link Tunnel} protocol.
 *
 * The service maintains a map of Messenger-instances.
 * The Messenger-instances are set right after the connection has been established.
 * They are cleared if the extension service has disconnected itself (stopSelf)
 * or the last host app disconnects.
 *
 * The service can be enabled by adding it to the manifest as follows:
 *
 * <service android:name="com.sonyericsson.extras.liveware.extension.util.TunnelService"/>
 */
public class TunnelService extends Service implements ExtensionIntentSender {

    private static final String LOG_TAG = "TunnelService";

    private ExtensionService mLocalExtensionService;
    boolean mBound = false;
    private final LinkedList<Intent> mIntentQueue = new LinkedList<Intent>();

    /**
     * Handler of incoming messages from clients.
     */
    @SuppressLint("HandlerLeak")
    private class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Tunnel.Messages.TUNNEL_INTENT:
                {
                    Intent intent = (Intent)msg.obj;
                    handleIntentFromHostApp(intent);
                    break;
                }
                case Tunnel.Messages.SETUP_MESSENGER:
                {
                    Bundle b = msg.getData();
                    b.setClassLoader(getClassLoader());

                    String hostAppPkgName = b.getString(Control.Intents.EXTRA_AHA_PACKAGE_NAME);
                    mHostAppMessengers.put(hostAppPkgName, msg.replyTo);

                    Log.d(LOG_TAG, "Setup messenger.");
                    break;
                }

                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private final Messenger mMessengerHandlingHostAppIntents = new Messenger(new IncomingHandler());

    private final LinkedHashMap<String, Messenger> mHostAppMessengers = new LinkedHashMap<String, Messenger>();

    /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection mExtensionServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mLocalExtensionService = binder.getService();

            // Enables the service to respond back with intents through
            // the BindService.
            mLocalExtensionService.setIntentSender(TunnelService.this);

            mBound = true;
            Log.d(LOG_TAG, "Connected to extension service.");

            handleQueuedIntentsFromHostApp();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;

            Log.v(LOG_TAG, "Local service disconnected, sending DISCONNECT to host apps.");

            Iterator<Messenger> itr = mHostAppMessengers.values().iterator();
            while(itr.hasNext()) {
                Messenger m = itr.next();
                if (m != null) {
                    sendMessageToHostApp(m,
                            Tunnel.Messages.DISCONNECT, null);
                    Log.d(LOG_TAG, "Sent DISCONNECT to " + m.toString());
                }
            }
            mHostAppMessengers.clear();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // Makes local binding to ExtensionService.
        Intent serviceIntent = new Intent();
        ComponentName extensionServiceComponentName = getExtensionServiceComponentName(this);
        if (extensionServiceComponentName != null) {
            Log.d(LOG_TAG, "Binding extension service...");
            serviceIntent.setComponent(extensionServiceComponentName);
            bindService(serviceIntent, mExtensionServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            sendSetupFailedMsg();
        }

        return mMessengerHandlingHostAppIntents.getBinder();
    }

    private void sendSetupFailedMsg() {
        Log.d(LOG_TAG, "Failed bind, no extension service found.");
        Message msg = Message.obtain(null,
                Tunnel.Messages.SETUP_FAILED);
        try {
            mMessengerHandlingHostAppIntents.send(msg);
        } catch (RemoteException e) {
            Log.d(LOG_TAG, "Binding extension service...", e);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "Unbinding local extension service.");

        if (mBound) {
            unbindService(mExtensionServiceConnection);
        }

        mHostAppMessengers.clear();

        return super.onUnbind(intent);
    }

    protected void handleQueuedIntentsFromHostApp() {
        while(!mIntentQueue.isEmpty() && mBound) {
            handleIntentFromHostApp(mIntentQueue.poll());
        }
    }

    public void handleIntentFromHostApp(Intent intent) {
        if (mBound && mLocalExtensionService != null) {
            mLocalExtensionService.handleIntent(intent);
            Log.v(LOG_TAG, "Forwarded intent to local service.");
        } else {
            mIntentQueue.offer(intent);
            Log.v(LOG_TAG, "Queued intent for local service, no bound extension service.");
        }
    }

    private ComponentName getExtensionServiceComponentName(Context c) {
        PackageManager pm = c.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(c.getPackageName(),
                    PackageManager.GET_SERVICES);
            for (ServiceInfo si : packageInfo.services) {
                String className = si.name.startsWith(".")
                        ? si.packageName + si.name : si.name;
                Class<?> serviceClass = Class.forName(className);
                if (ExtensionService.class.isAssignableFrom(serviceClass)) {
                    return new ComponentName(c.getPackageName(), si.name);
                }
            }
        } catch (ClassNotFoundException e) {
            sendSetupFailedMsg();
        } catch (NameNotFoundException e) {
            sendSetupFailedMsg();
        }
        return null;
    }

    @Override
    public void send(Intent intent) {
        sendMessageToHostApp(intent.getPackage(),
                Tunnel.Messages.TUNNEL_INTENT, intent);
    }

    private void sendMessageToHostApp(String hostAppPkg, int what, Object obj) {
        Messenger messenger = mHostAppMessengers.get(hostAppPkg);
        sendMessageToHostApp(messenger, what, obj);
    }

    private void sendMessageToHostApp(Messenger m, int what, Object obj) {

        if (m == null) {
            Log.e(LOG_TAG, "Failed sending message, no client messenger.");
            return;
        }

        Message msg = Message.obtain(null, what, 0, 0, obj);
        try {
            m.send(msg);
            Log.v(LOG_TAG, "Forwarded intent to host app.");
        } catch (RemoteException e) {
            Log.e(LOG_TAG, String.format("Failed sending message."), e);
        }
    }
}
