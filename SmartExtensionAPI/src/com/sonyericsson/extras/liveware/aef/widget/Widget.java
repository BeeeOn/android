/*
Copyright (c) 2011 Sony Ericsson Mobile Communications AB
Copyright (C) 2012 Sony Mobile Communications AB

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

package com.sonyericsson.extras.liveware.aef.widget;

import com.sonyericsson.extras.liveware.aef.registration.Registration.DeviceColumns;
import com.sonyericsson.extras.liveware.aef.registration.Registration.ExtensionColumns;

/**
 * Widget API is a part of the Smart Extension APIs.
 * <p>Some of our advanced accessories will support the Widget API.
 * The Widget API enables the extension to display a live image on the accessory,
 * sort of a preview of what the extension is about.
 * The {@link com.sonyericsson.extras.liveware.aef.registration.Registration.HostAppColumns#WIDGET_API_VERSION} specifies if the accessory
 * supports the Widget API and if so, which version that it supports.
 * </p>
 * <p>Topics covered here:
 * <ol>
 * <li><a href="#WidgetSize">How do extensions find out correct Widget image size</a>
 * <li><a href="#Update">Update Widget image</a>
 * <li><a href="#Touch">Touch events</a>
 * </ol>
 * </p>
 * <a name="WidgetSize"></a>
 * <h3>How do extensions find out correct Widget image size</h3>
 * <p>
 * Before an extension sends the Widget image to the Host Application, it has to figure out what
 * size the image should be. This might vary between accessories as some have a larger display.
 *
 * In Widget API version 1 and 2, all widgets for a device should have the same size (specified
 * in {@link com.sonyericsson.extras.liveware.aef.registration.Registration.Device}); also, an
 * extension can have one widget for each host application.
 * </p>
 * <p>
 * Starting with Widget API version 3, a host application may support several different widget
 * types and an extension can provide several different implementations for each of those types.
 * The supported widget types are specified in {@link com.sonyericsson.extras.liveware.aef.registration.Registration.Widget}
 * and each widget type is identified by the {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetColumns#TYPE}.
 * The extension specifies its available widgets in the {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistration} table,
 * and the different widgets are identified by the {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#KEY}.
 * The {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#KEY} is included in
 * {@link Intents#WIDGET_START_REFRESH_IMAGE_INTENT} as {@link Intents#EXTRA_KEY} to specify which widget
 * that was started.
 * The {@link Intents#WIDGET_START_REFRESH_IMAGE_INTENT} also includes a {@link Intents#EXTRA_INSTANCE_ID} that
 * is then used to refer to the started widget instance in all subsequent intents.
 * </p>
 * <p>
 * In Widget API version 3, the host application may position widgets based on a grid of cells.
 * If the size of a widget does not match the size of the cells, the widget size will be rounded up
 * to the nearest cell size.
 * </p>
 * <a name="Update"></a>
 * <h3>Update Widget image</h3>
 * <p>
 * At certain occasions, the Host Application will request a Widget image from the
 * extension, {@link Intents#WIDGET_START_REFRESH_IMAGE_INTENT}. E.g. when the user powers on the accessory,
 * or when a new Widget extension is installed.
 * When the extension receives this Intent, it must send back a Widget image to the Host
 * Application, {@link Intents#WIDGET_IMAGE_UPDATE_INTENT} or {@link Intents#WIDGET_PROCESS_LAYOUT_INTENT} (in Widget API version 2 or later).
 * The extension can continue to update its image until the intent
 * {@link Intents#WIDGET_STOP_REFRESH_IMAGE_INTENT} is received. The extension can resume updating
 * its image when {@link Intents#WIDGET_START_REFRESH_IMAGE_INTENT} has been received again.
 * </p>
 * <h4>Accessory state</h4>
 * <p>
 * Widgets may also visible when the accessory is in a low power mode and when disconnected
 * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetColumns#ACCESSORY_STATE}.
 * The default behavior is that the same content is shown when the accessory is in low power mode
 * {@link AccessoryState#POWERSAVE}
 * and nothing is displayed when the accessory is disconnected
 * {@link AccessoryState#DISCONNECTED}
 * </p>
 * <p>
 * If the extension wants to display content based on the accessory's state, it can provide
 * {@link Intents#EXTRA_ADDITIONAL_LAYOUTS} with given {@link Intents#EXTRA_ACCESSORY_STATE} flag in the
 * {@link Intents#WIDGET_PROCESS_LAYOUT_INTENT} intent.
 * If the extension does not provide any content for a display mode, it will not be visible in
 * that mode.
 * </p>
 * <a name="Touch"></a>
 * <h3>Touch events</h3>
 * <p>
 * In order to allow the user to interact with the Widget, the extension will get touch events
 * that occur when your Widget is in focus on the accessory display. This way, the extension
 * receives user feedback and can refresh the Widget image.
 * </p>
 * <p>
 * Touch events are sent to the extension through different intents depending on how the widget
 * image is supplied.
 * If the extension sends images in {@link Intents#WIDGET_IMAGE_UPDATE_INTENT} then touch events will
 * be sent as {@link Intents#WIDGET_ONTOUCH_INTENT} intents.
 * If the extension uses layouts to update the widget {@link Intents#WIDGET_PROCESS_LAYOUT_INTENT}, then
 * touch events will be sent as {@link Intents#WIDGET_OBJECT_CLICK_EVENT_INTENT} intents.
 * </p>
 * <p>
 * As an example, one could mention a media player controller Widget. The initial Widget image shows a couple
 * of buttons, play/pause, previous, next, etc. When a user presses somewhere on the Widget, the
 * {@link Intents#WIDGET_ONTOUCH_INTENT} will be sent to the extension. Since the extension provided the
 * initial image, it knows the exact layout/position of the buttons and can that way determine what button
 * was pressed and take action. In this case, the action could be to start playing a song. The extension can
 * also choose to update the Widget image so that it reflects the latest state, instead of the play button,
 * it might a the pause button and the title of the playing song.
 * </p>

 */

public class Widget {

    /**
     * @hide
     * This class is only intended as a utility class containing declared constants
     *  that will be used by Widget API extension developers.
     */
    protected Widget() {
    }

    /**
     * Intents sent between Widget extensions and Accessory Host Applications.
     */
    public interface Intents {

        /**
         * Intent sent by the Accessory Host Application whenever it wants the Widget to start updating it's Widget image.
         * Usually this Intent will be sent out when the accessory just starts and is about to show the Widget menu.
         * The Widget image should be updated as soon as possible and after the initial update the Widget image should
         * be updated occasionally until WIDGET_STOP_REFRESH_IMAGE_INTENT is received.
         * <p>
         * The {@link Intents#EXTRA_INSTANCE_ID} specifies the widget instance and is used in all subsequent intents to and
         * from that widget instance.
         * </p>
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AHA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_EXTENSION_KEY}</li>
         * <li>{@link #EXTRA_KEY} (required from version 3)</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 1.0
         */
        static final String WIDGET_START_REFRESH_IMAGE_INTENT = "com.sonyericsson.extras.aef.widget.START_REFRESH_IMAGE_REQUEST";

        /**
         * Intent sent by the Accessory Host Application whenever it wants the Widget to stop/pause updating it's Widget image.
         * The Widget should resume updating its image when {@link #WIDGET_START_REFRESH_IMAGE_INTENT} is received.
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AHA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_EXTENSION_KEY}</li>
         * <li>{@link #EXTRA_KEY} (required from version 3)</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 1.0
         */
        static final String WIDGET_STOP_REFRESH_IMAGE_INTENT = "com.sonyericsson.extras.aef.widget.STOP_REFRESH_IMAGE_REQUEST";


        /**
         * Intent sent by the extension whenever it wants to update the Accessory display by sending an XML layout.
         * <p>
         * {@link #EXTRA_LAYOUT_DATA} can be used to populate views in the layout with initial values.
         * The content of the views in the layout can also be updated using {@link #WIDGET_SEND_TEXT_INTENT} and {@link #WIDGET_SEND_IMAGE_INTENT}.
         * </p>
         * <p>
         * When using the broadcast queue, this intent should be sent with enforced security by supplying the host application permission
         * to sendBroadcast(Intent, String). {@link com.sonyericsson.extras.liveware.aef.registration.Registration#HOSTAPP_PERMISSION}
         * </p>
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AEA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_DATA_XML_LAYOUT}</li>
         * <li>{@link #EXTRA_LAYOUT_DATA} (optional)</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * <li>{@link #EXTRA_ADDITIONAL_LAYOUTS} (optional. If used, any resource
         * referenced in {@link #EXTRA_DATA_XML_LAYOUT} will be ignored and resources provided as
         * additional layout will be used.
         * </li>
         * </ul>
         * </p>
         * @since 2.0
         */
        static final String WIDGET_PROCESS_LAYOUT_INTENT = "com.sonyericsson.extras.aef.widget.PROCESS_LAYOUT";

        /**
         * Intent used by the Widget extension whenever it wants to update its widget image.
         * The Widget image should be updated occasionally.
         * If the extension tries to update its Widget image to often, the Host Application will ignore the requests.
         * <p>
         * When using the broadcast queue, this intent should be sent with enforced security by supplying the host application permission
         * to sendBroadcast(Intent, String). {@link com.sonyericsson.extras.liveware.aef.registration.Registration#HOSTAPP_PERMISSION}
         * </p>
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_WIDGET_IMAGE_URI}</li>
         * <li>{@link #EXTRA_WIDGET_IMAGE_DATA}</li>
         * <li>{@link #EXTRA_AEA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 1.0
         */
        static final String WIDGET_IMAGE_UPDATE_INTENT = "com.sonyericsson.extras.aef.widget.IMAGE_UPDATE";

        /**
         * This intent may be used by the Widget extension as a response to a {@link #WIDGET_ONTOUCH_INTENT}.
         * The widget should send this intent when it does not want to perform any action based on the on touch intent.
         * When receiving this intent the host application is free to stop interaction with this widget and enter a new
         * level or state internally.
         * <p>
         * When using the broadcast queue, this intent should be sent with enforced security by supplying the host application permission
         * to sendBroadcast(Intent, String). {@link com.sonyericsson.extras.liveware.aef.registration.Registration#HOSTAPP_PERMISSION}
         * </p>
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AEA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 1.0
         */
        static final String WIDGET_ENTER_NEXT_LEVEL_INTENT = "com.sonyericsson.extras.aef.widget.ENTER_NEW_LEVEL";

        /**
         * Intent sent by the Host Application to the Widget extension whenever a user interacts with the Widget image.
         * Usually as a result of this Intent the Widget extension will update its Widget image and take appropriate action
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AHA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_EVENT_TYPE}</li>
         * <li>{@link #EXTRA_EVENT_X_POS}</li>
         * <li>{@link #EXTRA_EVENT_Y_POS}</li>
         * <li>{@link #EXTRA_EXTENSION_KEY}</li>
         * <li>{@link #EXTRA_KEY} (required from version 3)</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 1.0
         */
        static final String WIDGET_ONTOUCH_INTENT = "com.sonyericsson.extras.aef.widget.ONTOUCH";

        /**
         * Intent sent by the Host Application to the Widget extension whenever an click
         * event is detected on a graphical object referenced from a layout.
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AHA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_EVENT_TYPE}</li>
         * <li>{@link #EXTRA_EXTENSION_KEY}</li>
         * <li>{@link #EXTRA_LAYOUT_REFERENCE}</li>
         * <li>{@link #EXTRA_KEY} (required from version 3)</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 2.0
         */
        static final String WIDGET_OBJECT_CLICK_EVENT_INTENT = "com.sonyericsson.extras.aef.widget.OBJECT_CLICK_EVENT";

        /**
         * Intent sent by the Widget extension whenever it wants to update an image in an ImageView on the accessory.
         * The image can be a URI or an array of a raw image, like JPEG
         * The image will replace any previous sent image with the same reference.
         * <p>
         * When using the broadcast queue, this intent should be sent with enforced security by supplying the host application permission
         * to sendBroadcast(Intent, String). {@link com.sonyericsson.extras.liveware.aef.registration.Registration#HOSTAPP_PERMISSION}
         * </p>
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AEA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_LAYOUT_REFERENCE}</li>
         * <li>{@link #EXTRA_WIDGET_IMAGE_URI}</li>
         * <li>{@link #EXTRA_WIDGET_IMAGE_DATA}</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 2.0
         */
        static final String WIDGET_SEND_IMAGE_INTENT = "com.sonyericsson.extras.aef.widget.SEND_IMAGE";

        /**
         * Intent sent by the Widget extension whenever it wants to update a text in a TextView on the accessory.
         * The text will replace any previous sent text with the same reference.
         * <p>
         * When using the broadcast queue, his intent should be sent with enforced security by supplying the host application permission
         * to sendBroadcast(Intent, String). {@link com.sonyericsson.extras.liveware.aef.registration.Registration#HOSTAPP_PERMISSION}
         * </p>
         * <p>
         * Intent-extra data:
         * </p>
         * <ul>
         * <li>{@link #EXTRA_AEA_PACKAGE_NAME}</li>
         * <li>{@link #EXTRA_LAYOUT_REFERENCE}</li>
         * <li>{@link #EXTRA_WIDGET_TEXT}</li>
         * <li>{@link #EXTRA_INSTANCE_ID} (required from version 3)</li>
         * </ul>
         * </p>
         * @since 2.0
         */
        static final String WIDGET_SEND_TEXT_INTENT = "com.sonyericsson.extras.aef.widget.SEND_TEXT";

        /**
         * The name of the Intent-extra used to identify the Host Application.
         * The Host Application will send its package name
         * <P>
         * TYPE: TEXT
         * </P>
         * @since 1.0
         */
        static final String EXTRA_AHA_PACKAGE_NAME = "aha_package_name";

        /**
         * The name of the Intent-extra used to identify the extension.
         * The extension will send its package name
         * <P>
         * TYPE: TEXT
         * </P>
         * @since 1.0
         */
        static final String EXTRA_AEA_PACKAGE_NAME = "aea_package_name";

        /**
         * The name of the Intent-extra used to identify the URI of the Widget image.
         * If the image is in raw data (e.g. an array of bytes) use {@link #EXTRA_WIDGET_IMAGE_DATA} instead.
         * The image is displayed in the Widget row on the Accessory display.
         * The image can be updated by the extension at a later stage
         * <P>
         * TYPE: TEXT
         * </P>
         * @since 1.0
         */
        static final String EXTRA_WIDGET_IMAGE_URI = "widget_image_uri";

        /**
         * The name of the Intent-extra used to identify the Widget image.
         * This Intent-extra should be used if the image is in raw data (e.g. an array of bytes).
         * The image is displayed in the Widget row on the Accessory display.
         * The image can be updated by the extension at a later stage
         * <P>
         * TYPE: BYTE ARRAY
         * </P>
         * @since 1.0
         */
        static final String EXTRA_WIDGET_IMAGE_DATA = "widget_image_data";

        /**
         * The name of the Intent-extra used to identify the touch event
         * <P>
         * TYPE: INTEGER (int)
         * </P>
         * <P>
         * ALLOWED VALUES:
         * <ul>
         * <li>{@link #EVENT_TYPE_SHORT_TAP}</li>
         * <li>{@link #EVENT_TYPE_LONG_TAP}</li>
         * </ul>
         * </P>
         * @since 1.0
         */
        static final String EXTRA_EVENT_TYPE = "widget_event_type";

        /**
         * The name of the Intent-extra used to carry the X coordinate of the touch event
         * <P>
         * TYPE: INTEGER (int)
         * </P>
         * @since 1.0
         */
        static final String EXTRA_EVENT_X_POS = "widget_event_x_pos";

        /**
         * The name of the Intent-extra used to carry the Y coordinate of the touch event
         * <P>
         * TYPE: INTEGER (int)
         * </P>
         * @since 1.0
         */
        static final String EXTRA_EVENT_Y_POS = "widget_event_y_pos";

        /**
         * The name of the Intent-extra containing the key set by the extension
         * in {@link ExtensionColumns#EXTENSION_KEY}. This Intent-data is present in
         * all Intents sent by accessory host application, except where
         * {@link android.app.Activity#startActivity(android.content.Intent)}
         * is used. See section <a href="Registration.html#Security">Security</a>
         * for more information
         *
         * @since 1.0
         */
        static final String EXTRA_EXTENSION_KEY = "extension_key";

        /**
         * The name of the Intent-extra used to identify the data XML layout to be processed by the host application
         * and displayed by the accessory.
         * The layout resource id is used to identify the layout.
         * <P>
         * This is a standard Android layout, where a subset of the Android views are supported.
         * </P>
         * </P>
         * <h3>Dimensions</h3>
         * <P>
         * The px dimensions is the only dimension supported.
         * </P>
         * <h3>ViewGroups</h3>
         * <P>
         * The following ViewGroups are supported:
         * <ul>
         * <li>AbsoluteLayout</li>
         * <li>FrameLayout</li>
         * <li>LinearLayout</li>
         * <li>RelativeLayout</li>
         * </ul>
         * All XML attributes are supported for the supported ViewGroups.
         * </P>
         * <h3>Views</h3>
         * <P>
         * The following Views are supported:
         * <ul>
         * <li>View</li>
         * <li>ImageView</li>
         * <li>TextView</li>
         * </ul>
         * An accessory may support only a subset of these layouts.
         * {@link DeviceColumns#LAYOUT_SUPPORT} specifies which Views that are
         * supported for a certain accessory.
         * </p>
         * <p>
         * The following View XML attributes are supported
         * <ul>
         * <li>android:background - can be a solid color such as "#ff000000" (black) or a
         * StateListDrawable with solid colors.
         * Only the android:state_pressed attribute of the StateListDrawable is supported.
         * Using a StateListDrawable requires that the view has at least one of android:clickable or
         * android:longClickable set to true.</li>
         * <li>android:clickable - {@link #WIDGET_OBJECT_CLICK_EVENT_INTENT} are sent for short
         * clicks views where this flag is set to true</li>
         * <li>android:longClickable - {@link #WIDGET_OBJECT_CLICK_EVENT_INTENT} are sent for
         * long clicks views where this flag is set to true</li>
         * <li>android:id</li>
         * <li>android:padding</li>
         * <li>android:paddingBottom</li>
         * <li>android:paddingLeft</li>
         * <li>android:paddingRight</li>
         * <li>android:paddingTop</li>
         * </ul>
         * </P>
         * <h3>ImageView</h3>
         * <P>
         * For an ImageView the following XML attributes are supported
         * <ul>
         * <li>android:src - can be a BitmapDrawable or a NinePatchDrawable</li>
         * <li>android:scaleType</li>
         * </ul>
         * </P>
         * <h3>TextView</h3>
         * <P>
         * For a TextView the following XML attributes are supported
         * <ul>
         * <li>android:ellipsize - can be none, start, middle or end</li>
         * <li>android:gravity</li>
         * <li>android:lines</li>
         * <li>android:maxLines</li>
         * <li>android:singleLine</li>
         * <li>android:text</li>
         * <li>android:textColor</li>
         * <li>android:textSize - Not all text sizes are supported by all accessories.
         * If a not supported text size is used the accessory will select the closest available text size.
         * See the accessory white paper for a list of supported text sizes.
         * </li>
         * <li>android:textStyle</li>
         * </ul>
         * </P>
         * <P>
         * TYPE: INTEGER
         * </P>
         * @since 2.0
         */
        static final String EXTRA_DATA_XML_LAYOUT = "data_xml_layout";
        /**
         * The name of the Intent-extra used to identify a reference within an extension.
         * The extension may use the same reference in multiple layouts.
         * A reference is a text or an image owned by the extension and will
         * be used to map between layouts and images/texts.
         * Corresponds to the android:id XML attribute in the layout.
         * <P>
         * TYPE: INTEGER
         * </P>
         * @since 2.0
         */
        static final String EXTRA_LAYOUT_REFERENCE = "layout_reference";

        /**
         * The name of the Intent-extra used when sending a text (String)
         * from the extension to the accessory. The accessory will map the text
         * to a layout reference.
         * <P>
         * TYPE: STRING
         * </P>
         * @since 2.0
         */
        static final String EXTRA_WIDGET_TEXT = "text_from extension";

        /**
         * The name of the intent extra used to identify a widget in case the
         * extension supports several different widgets. The extension specifies
         * this in {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#KEY}.
         * <P>
         * TYPE: STRING
         * </P>
         *
         * @since 3.0
         */
        static final String EXTRA_KEY = "key";

        /**
         * Data used to populate the views in a XML layout with dynamic info.
         * For example updating a TextView with a new text or setting a new
         * image in an ImageView. {@link #EXTRA_LAYOUT_REFERENCE} specifies the
         * view to be updated and one of {@link #EXTRA_WIDGET_TEXT},
         * {@link #EXTRA_WIDGET_IMAGE_URI} and {@link #EXTRA_WIDGET_IMAGE_DATA} specifies the new
         * information in the view.
         * <P>
         * TYPE: Array of BUNDLEs with following information in each BUNDLE.
         * <ul>
         * <li>{@link #EXTRA_LAYOUT_REFERENCE}</li>
         * <li>{@link #EXTRA_WIDGET_TEXT} or {@link #EXTRA_WIDGET_IMAGE_URI} or {@link #EXTRA_WIDGET_IMAGE_DATA}</li>
         * </ul>
         * </P>
         *
         * @since 3.0
         */
        static final String EXTRA_LAYOUT_DATA = "layout_data";

        /**
         * Data used to add state specific layouts to a widget. {@link #EXTRA_DATA_XML_LAYOUT}
         * specifies the XML layout and {@link #EXTRA_ACCESSORY_STATE} specifies the state in which
         * the provided layout is relevant.
         * If no state specific resources are supplied, the resource provided at {@link #EXTRA_DATA_XML_LAYOUT} is used.
         * If no resource is provided for either {@link AccessoryState#POWERSAVE} or {@link AccessoryState#DISCONNECTED}, the {@link AccessoryState#DEFAULT} resource is used.
         * <P>
         * TYPE: Array of BUNDLEs with following information in each BUNDLE.
         * <ul>
         * <li>{@link #EXTRA_DATA_XML_LAYOUT}</li>
         * <li>{@link #EXTRA_ACCESSORY_STATE}</li>
         * </ul>
         * </P>
         *
         * @since 3.0
         */
        static final String EXTRA_ADDITIONAL_LAYOUTS = "additional_layouts";

        /**
         * Specifies the accessory state that a given resource is applicable for
         *
         * <P>
         * TYPE: INTEGER (int), see {@link AccessoryState}.
         * </P>
         *
         * @since 3.0
         */
        static final String EXTRA_ACCESSORY_STATE = "display_mode";

        /**
         * Specifies the widget instance.
         *
         * <P>
         * TYPE: INTEGER
         * </P>
         *
         * @since 3.0
         */
        static final String EXTRA_INSTANCE_ID = "instance_id";

        /**
         * The event type is a short tap.
         *
         * @since 1.0
         */
        static final int EVENT_TYPE_SHORT_TAP = 0;

        /**
         * The event type is a long tap
         *
         * @since 1.0
         */
        static final int EVENT_TYPE_LONG_TAP = 1;
    }

    /**
     * An accessory can enter different states. If an extension assigns an AccessoryState to
     * certain resources the accessory shall use that resource while in the given state.
     * @see Intents#WIDGET_PROCESS_LAYOUT_INTENT
     * @see Intents#WIDGET_IMAGE_UPDATE_INTENT
     */
    public interface AccessoryState {

        /**
         * An accessory's default state. In this state, the display's properties, e.g. color
         * and latency are as defined in {@link com.sonyericsson.extras.liveware.aef.registration.Registration.DisplayColumns}. Touch
         * screen, if present, is active, and accessory is connected to a host application.
         *
         * @since 3.0
         */
        static final int DEFAULT = 1;

        /**
         * In this state, the display may be in low power mode and it's properties are not
         * guaranteed to match the definition in {@link com.sonyericsson.extras.liveware.aef.registration.Registration}.
         * The host application will convert any resources provided to suit this display mode.
         * If present, the accessory's touch screen will not be active. Accessory is connected
         * to a host application.
         *
         * @since 3.0
         */
        static final int POWERSAVE = 2;

        /**
         * In this state, the accessory is not connected to a host application
         *
         * @since 3.0
         */
        static final int DISCONNECTED = 4;
    }

    /**
     * Specifies the category to which a widget belongs.
     */
    public interface Category {
        /**
         * The default category, a widget using any of the Widget APIs can register to this category
         *
         * @since 3.0
         */
        static final int DEFAULT = 0;

        /**
         * Widget is a clock widget. Widgets that both act as clocks and use the clock API's
         * should register for this category
         *
         * @since 3.0
         */
        static final int CLOCK = 1;
    }
}
