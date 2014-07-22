/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2012-2014, Sony Mobile Communications AB

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

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.sonyericsson.extras.liveware.aef.notification.Notification;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.sensor.AccessorySensor;
import com.sonyericsson.extras.liveware.extension.util.widget.BaseWidget;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Returns information needed during extension registration
 */
public abstract class RegistrationInformation {

    /**
     * Constant defining that an API is not required for extension.
     */
    public static int API_NOT_REQUIRED = 0;

    private static final int CATEGORY_SUPPORT_API_VERSION = 5;

    /**
     * Return if the specified Control API device is supported by the extension.
     * Default implementation checks if the display size is supported by
     * the extension. Override this method to adapt it to your needs,
     * e.g. to include tap support.
     *
     * @param deviceInfo The device.
     * @return True if the Control API device is supported.
     */
    public boolean isControlDeviceSupported(DeviceInfo deviceInfo) {
        for (DisplayInfo display : deviceInfo.getDisplays()) {
            if (isDisplaySizeSupported(display.getWidth(), display.getHeight())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the required notifications API version
     *
     * @see Registration.ExtensionColumns#NOTIFICATION_API_VERSION
     * @see #getSourceRegistrationConfigurations
     * @see ExtensionService#onViewEvent
     * @see ExtensionService#onRefreshRequest
     * @return Required notification API version, or {@link #API_NOT_REQUIRED}
     *         if not requiring notification support.
     */
    public abstract int getRequiredNotificationApiVersion();

    /**
     * Get the extension registration information.
     *
     * @see ExtensionService#onRegisterResult
     * @return The registration configuration.
     */
    public abstract ContentValues getExtensionRegistrationConfiguration();

    /**
     * Get all source registration configurations. Note that the extension
     * specific id must be set if there are more than one source. Override
     * this method if this is a notification extension.
     *
     * @see Notification.SourceColumns#EXTENSION_SPECIFIC_ID
     * @see #getRequiredNotificationApiVersion()
     * @return The source registration information.
     */
    public ContentValues[] getSourceRegistrationConfigurations() {
        throw new IllegalArgumentException(
                "getSourceRegistrationConfiguration() not implemented. Notification extensions must override this method");
    }

    /**
     * Checks if the widget size is supported. Override this to provide
     * extension specific implementation.
     *
     * @param width The widget width.
     * @param height The widget height.
     * @return True if the widget size is supported.
     */
    public boolean isWidgetSizeSupported(int width, int height) {
        throw new IllegalArgumentException(
                "isWidgetSizeSupported() not implemented. Widget extensions must override this method");
    }

    /**
     * Return the required widget API version.
     *
     * @see #isWidgetSizeSupported
     * @see ExtensionService#createWidgetExtension
     * @see Registration.ApiRegistrationColumns#WIDGET_API_VERSION
     * @return Required API widget version, or {@link #API_NOT_REQUIRED} if not supporting widget.
     */
    abstract public int getRequiredWidgetApiVersion();

    /**
     * Return the target widget API version.
     *
     * @see #isWidgetSizeSupported
     * @see ExtensionService#createWidgetExtension
     * @see Registration.ApiRegistrationColumns#WIDGET_API_VERSION
     * @return Required API widget version, or {@link #API_NOT_REQUIRED} if not
     *         supporting widget. Returns required widget API version by default.
     */
    public int getTargetWidgetApiVersion() {
        return getRequiredWidgetApiVersion();
    }

    /**
     * Return if the display size is supported.
     *
     * @param width The display width.
     * @param height The display height.
     * @see ExtensionService#createControlExtension
     * @return True if the display size is supported.
     */
    public boolean isDisplaySizeSupported(final int width, final int height) {
        throw new IllegalArgumentException(
                "isDisplaySizeSupported() not implemented. Control extensions must override this method");
    }

    /**
     * Return the required control API version.
     *
     * @see #isDisplaySizeSupported
     * @see ExtensionService#createControlExtension
     * @see Registration.ApiRegistrationColumns#CONTROL_API_VERSION
     * @return Required API control version, or {@link #API_NOT_REQUIRED}
     *         if not supporting control.
     */
    abstract public int getRequiredControlApiVersion();

    /**
     * Return the target control API version.
     *
     * @see #isDisplaySizeSupported
     * @see ExtensionService#createControlExtension
     * @see Registration.ApiRegistrationColumns#CONTROL_API_VERSION
     * @return Required API control version, or {@link #API_NOT_REQUIRED}
     *         if not supporting control. Returns required control API version
     *         by default.
     */
    public int getTargetControlApiVersion() {
        return getRequiredControlApiVersion();
    }

    /**
     * Return if the sensor is used by extension.
     *
     * @param sensor The sensor.
     * @return True if sensor is used by the extension.
     */
    public boolean isSensorSupported(final AccessorySensor sensor) {
        throw new IllegalArgumentException(
                "isSensorSupported() not implemented. Sensor extensions must override this method");

    }

    /**
     * Return the required sensor API version.
     *
     * @see #isDisplaySizeSupported
     * @see Registration.ApiRegistrationColumns#SENSOR_API_VERSION
     * @return Required API sensor version, or {@link #API_NOT_REQUIRED}
     *         if not supporting sensor.
     */
    abstract public int getRequiredSensorApiVersion();

    /**
     * Return the target sensor API version.
     *
     * @see #isDisplaySizeSupported
     * @see #getRequiredSensorApiVersion()
     * @see Registration.ApiRegistrationColumns#SENSOR_API_VERSION
     * @return Target API sensor version, or {@link #API_NOT_REQUIRED}
     *         if not supporting sensor. Returns required sensor API version
     *         by default.
     */
    public int getTargetSensorApiVersion() {
        return getRequiredSensorApiVersion();
    }

    /**
     * Return true if low power mode is used by the control extension.
     *
     * @return True if low power mode is used by the extension.
     */
    public boolean supportsLowPowerMode() {
        return false;
    }

    /**
     * Return true if the control extension wants to intercept the back button
     * of the connected accessory.
     *
     * @return True if extension wants to intercept back button
     */
    public boolean controlInterceptsBackButton() {
        return false;
    }

    /**
     * Return true if the sources shall be updated when the extension service is
     * created. This might be handy if the extension use dynamic sources and the
     * source can change when the extension service is not running.
     *
     * @return True if the source registration shall be update when the
     *         extension service is created.
     */
    public boolean isSourcesToBeUpdatedAtServiceCreation() {
        return false;
    }

    /**
     * Check if widget shall be supported for this host application by checking
     * that the host application has device with a supported widget size. This
     * method can be override to provide extension specific implementations.
     *
     * @param context The context.
     * @param hostApplication The host application.
     * @return True if widget shall be supported.
     */
    public boolean isSupportedWidgetAvailable(final Context context,
            final HostApplicationInfo hostApplication) {
        if (getRequiredWidgetApiVersion() == API_NOT_REQUIRED) {
            return false;
        }

        if (hostApplication.getWidgetApiVersion() == 0) {
            return false;
        }

        if (getRequiredWidgetApiVersion() > hostApplication.getWidgetApiVersion()) {
            if (Dbg.DEBUG) {
                Dbg.w("isSupportedWidgetAvailable: required widget API version not supported");
            }
            return false;
        }

        for (DeviceInfo device : hostApplication.getDevices()) {
            if (isWidgetSizeSupported(device.getWidgetWidth(), device.getWidgetHeight())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if sensor shall be supported for this host application by checking
     * if the host application has at least one supported sensor. This method
     * can be override to provide extension specific implementations.
     *
     * @param context The context.
     * @param hostApplication The host application.
     * @return True if sensor shall be supported.
     */
    public boolean isSupportedSensorAvailable(final Context context,
            final HostApplicationInfo hostApplication) {
        if (getRequiredSensorApiVersion() == API_NOT_REQUIRED) {
            return false;
        }

        if (hostApplication.getSensorApiVersion() == 0) {
            return false;
        }

        if (getRequiredSensorApiVersion() > hostApplication.getSensorApiVersion()) {
            if (Dbg.DEBUG) {
                Dbg.w("isSupportedSensorAvailable: required sensor API version not supported");
            }
            return false;
        }

        for (DeviceInfo device : hostApplication.getDevices()) {
            for (AccessorySensor sensor : device.getSensors()) {
                if (isSensorSupported(sensor)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if control shall be supported for this host application by checking
     * that the host application has a device with a supported display size.
     * This method can be override to provide extension specific
     * implementations.
     *
     * @param context The context.
     * @param hostApplication The host application.
     * @return True if control shall be supported.
     */
    public boolean isSupportedControlAvailable(final Context context,
            final HostApplicationInfo hostApplication) {
        if (getRequiredControlApiVersion() == API_NOT_REQUIRED) {
            return false;
        }

        if (hostApplication.getControlApiVersion() == 0) {
            return false;
        }

        if (getRequiredControlApiVersion() > hostApplication.getControlApiVersion()) {
            if (Dbg.DEBUG) {
                Dbg.w("isSupportedControlAvailable: required control API version not supported");
            }
            return false;
        }

        for (DeviceInfo device : hostApplication.getDevices()) {
            if (isControlDeviceSupported(device)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get widget registration values for a host application widget.
     *
     * @param context The context.
     * @param hostAppPackageName The host application package name
     * @param widgetContainer The host application widget the registration is for.
     * @param registrationVersion
     * @return Content values with widget registration values.
     */
    public List<ContentValues> getWidgetRegistrationValues(Context context,
            String packageName, WidgetContainer widgetContainer, int registrationVersion) {
        List<ContentValues> widgetList = new ArrayList<ContentValues>();
        WidgetClassList widgetRegistrations = getWidgetClasses(context,
                packageName,
                widgetContainer);
        for (Class<?> widgetClass : widgetRegistrations) {
            BaseWidget wr = getWidgetInstanceFromClass(context, packageName,
                    WidgetExtension.NOT_SET, widgetClass.getName());
            ContentValues values = new ContentValues();
            values.put(WidgetRegistrationColumns.NAME, context.getString(wr.getName()));
            values.put(WidgetRegistrationColumns.WIDTH, wr.getWidth());
            values.put(WidgetRegistrationColumns.HEIGHT, wr.getHeight());
            values.put(WidgetRegistrationColumns.TYPE, widgetContainer.getType());
            if (Dbg.DEBUG) {
                Dbg.w("Registraton version: " + registrationVersion);
            }
            if (registrationVersion >= CATEGORY_SUPPORT_API_VERSION) {
                values.put(WidgetRegistrationColumns.CATEGORY, wr.getCategory());
            }
            values.put(WidgetRegistrationColumns.KEY, widgetClass.getName());
            values.put(WidgetRegistrationColumns.PREVIEW_IMAGE_URI,
                    ExtensionUtils.getUriString(context,
                            wr.getPreviewUri()));
            widgetList.add(values);
        }

        return widgetList;
    }

    /**
     * Class containing a list of widgets. Used to define multiple widgets
     * for an extension.
     */
    public final static class WidgetClassList implements Iterable<Class<? extends BaseWidget>> {

        ArrayList<Class<? extends BaseWidget>> mList = new ArrayList<Class<? extends BaseWidget>>();

        public WidgetClassList() {
        }

        public WidgetClassList(Class<? extends BaseWidget>... widgetClasses) {
            for (Class<? extends BaseWidget> class1 : widgetClasses) {
                add(class1);
            }
        }

        public boolean add(Class<? extends BaseWidget> object) {
            if (mList.contains(object)) {
                return false;
            }
            return mList.add(object);
        }

        @Override
        public Iterator<Class<? extends BaseWidget>> iterator() {
            return mList.iterator();
        }

    }

    /**
     * Get widget registration info for a host application widget.
     *
     * @param context The context.
     * @param hostAppPackageName The host application package name
     * @param widgetContainer The host application widget the registration is for.
     * @return A list with widget registration info.
     */
    protected WidgetClassList getWidgetClasses(Context context,
            String hostAppPackageName, WidgetContainer widgetContainer) {
        throw new IllegalArgumentException(
                "getExtensionWidgets() not implemented. Extensions targeting widget API version 3 or later must implement this method.");
    }

    /**
     * Get the extension's registration key.
     *
     * @return The extensionKey.
     */
    public String getExtensionKey() {
        throw new IllegalArgumentException("extensionKey == null or not implemented");
    }

    /**
     * Create a widget instance for provided class.
     *
     * @param context Extension service context.
     * @param hostAppPackageName Host app package name.
     * @param instanceId Widget instance ID.
     * @param key The widget class name.
     * @return Widget instance.
     */
    public static BaseWidget getWidgetInstanceFromClass(Context context,
            String hostAppPackageName, int instanceId, String key) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException(
                    "Registered Extension class is not valid: "
                            + key + " from key:" + key);
        }
        try {
            Class<?> widgetClass = Class.forName(key);
            if (widgetClass == null
                    || !BaseWidget.class.isAssignableFrom(widgetClass)) {
                throw new IllegalArgumentException(
                        "Widget class " + widgetClass
                                + " must extend WidgetExtension");
            }
            Constructor<?> constructor = widgetClass.getConstructor(BaseWidget.WidgetBundle.class);
            constructor.setAccessible(true);
            BaseWidget widgetExtension = (BaseWidget) constructor
                    .newInstance(new BaseWidget.WidgetBundle(context, hostAppPackageName,
                            instanceId));
            return widgetExtension;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found " + key, e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate Widget " + key, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate Widget" + key, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(key
                    + " must have the public constructor BaseWidget(WidgetBundle bundle) ");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(key
                    + "  must have the public constructor BaseWidget(WidgetBundle bundle) ");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(key
                    + "  must have the public constructor BaseWidget(WidgetBundle bundle) ");
        }
    }
}
