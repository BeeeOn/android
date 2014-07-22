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

package com.sonyericsson.extras.liveware.aef.tunnel;

/**
 * Tunnel API is a part of the SmartExtension APIs.
 * <p>For all time sensitive user interactions extensions need to communicate
 * with the accessory with foreground priority. This API enables an host application
 * to bind to an extension service and exchange a pair of Messenger objects
 * allowing for a two way communication using a simple intent tunneling protocol.
 * The extension service will have foreground priority as long as a host application
 * is bound to it.
 * </p>
 * <a name="TunnelLifecycle"></a>
 * <h3>Life cycle of a tunnel</h3>
 * <p>
 * The host application should open a tunnel when it is about to send an intent
 * and it finds a service in the extension package exposing {@link #ACTION_BIND}
 * through its intent-filters, as the example below.
 * <br/>An opened tunnel should remain open until the service stops itself.
 * </p>
 * <a name="EnableTunnelInManifest"></a>
 * <h4>Enabling tunnel support in the manifest</h4>
 * <pre>
 * {@code
 * <service android:name="com.sonyericsson.extras.liveware.extension.util.TunnelService">
 *   <intent-filter>
 *     <action android:name="com.sonyericsson.extras.liveware.aef.tunnel.action.BIND" />
 *   </intent-filter>
 * </service>}
 * </pre>
 * <a name="ConnectionIntent"></a>
 * <h4>The Accessory Connection Intent</h4>
 * <p>The {@link com.sonyericsson.extras.liveware.aef.registration.Registration.Intents#ACCESSORY_CONNECTION_INTENT}
 * should by default always be sent over the broadcast queue preventing the extensions
 * from gaining foreground priority at startup. Raising the priority on all extensions
 * could strain devices with limited memory.
 * </p>
 * <p>
 * It is possible to override the default behavior by adding the connection intent
 * action in the tunnel service filter like the example below. This should only be used
 * when really necessary. An example of this is a call application which takes
 * control over the accessory screen when there is an incoming call. A user hearing
 * the ringing on his host device expects the watch to immediately show the call
 * control as soon as it gets in range.</p>
 * <pre>
 * {@code
 * <service android:name="com.sonyericsson.extras.liveware.extension.util.TunnelService">
 *   <intent-filter>
 *     <action android:name="com.sonyericsson.extras.liveware.aef.tunnel.action.BIND" />
 *     <action android:name="com.sonyericsson.extras.liveware.aef.registration.ACCESSORY_CONNECTION" />
 *   </intent-filter>
 * </service>
 * }
 * </pre>
 */
public class Tunnel {

    /**
     * @hide
     * This class is only intended as a utility class containing declared constants
     * that will be used by tunnel API extension developers.
     */
    protected Tunnel() {
    }

    /**
     * This intent action can be used to find a service that supports the tunnel API.
     *
     * @since 1.0
     */
    public static final String ACTION_BIND = "com.sonyericsson.extras.liveware.aef.tunnel.action.BIND";

    /**
     * Messenger messages sent between extensions and host applications.
     * The constants are used in the Message what-field.
     */
    public interface Messages {

        /**
         * Message sent by the host application to the extension
         * when the tunnel is first setup. This is the fist message sent
         * over the connection. It carries a bundle with the host application
         * package name and a reference to a host application Messenger
         * to be used by the extension for sending messages to the
         * host application.
         *
         * <p>
         * Message data:
         * </p>
         * <ul>
         * <li>obj: a Bundle</li>
         * <li>replyTo: a Messenger</li>
         * </ul>
         *
         * <p>
         * Bundle-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AHA_PACKAGE_NAME}</li>
         * </ul>
         *
         * @since 1.0
         */
        static final int SETUP_MESSENGER = 0;

        /**
         * Message sent by the extension if an unrecoverable failure is detected
         * during setup. Further use of the tunnel after this message is not
         * recommended.
         *
         * @since 1.0
         */
        static final int SETUP_FAILED = 1;

        /**
         * Message sent by the extension when it wants the host application
         * to close the tunnel.
         *
         * @since 1.0
         */
        static final int DISCONNECT = 2;

        /**
         * Message sent by either the host application or the extension
         * allowing the source to tunnel an intent directly to the target
         * intent handler without passing the Android broadcast queue.
         *
         * <p>
         * Message data:
         * </p>
         * <ul>
         * <li>obj: the intent to tunnel</li>
         * </ul>
         *
         * @since 1.0
         */
        static final int TUNNEL_INTENT = 3;


        /**
         * The name of the Bundle-extra used to identify the host application
         * by its package name.
         * <P>
         * TYPE: TEXT
         * </P>
         * @since 1.0
         */
        static final String EXTRA_AHA_PACKAGE_NAME = "aha_package_name";
    }
}
