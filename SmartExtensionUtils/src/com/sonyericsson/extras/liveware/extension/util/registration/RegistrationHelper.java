/*
Copyright (C) 2013-2014 Sony Mobile Communications AB.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sonyericsson.extras.liveware.aef.notification.Notification;
import com.sonyericsson.extras.liveware.aef.notification.Notification.SourceColumns;
import com.sonyericsson.extras.liveware.aef.registration.Registration;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.notification.NotificationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The registration helper class helps an extension to register with host
 * application based on the information provided in RegistrationInformation.
 */
public class RegistrationHelper {

    private final Context mContext;

    private final RegistrationInformation mRegistrationInformation;

    /**
     * Create registration helper.
     *
     * @param context The context.
     * @param registrationInformation The information about the extension that
     *            is needed to decided if it shall register and also the
     *            information is registers.
     */
    RegistrationHelper(Context context, RegistrationInformation registrationInformation) {
        mContext = context;
        mRegistrationInformation = registrationInformation;
    }

    /**
     * Perform the registration.
     *
     * @param onlySources True if only notification sources shall be refreshed.
     * @return True if registration was successful.
     */
    boolean performRegistration(boolean onlySources) {
        if (onlySources) {
            try {
                registerOrUpdateSources();
                return true;
            } catch (RegisterExtensionException e) {
                if (Dbg.DEBUG) {
                    Dbg.e("Source refresh failed", e);
                }
                return false;
            }
        } else {
            boolean registrationSuccess = registerOrUpdateExtension();
            if (registrationSuccess) {
                if (mRegistrationInformation.getRequiredWidgetApiVersion() > 0
                        || mRegistrationInformation.getRequiredControlApiVersion() > 0) {
                    registerWithAllHostApps();
                }
            }
            return registrationSuccess;
        }
    }

    /**
     * Register the extension or update the registration if already registered.
     * This method is called from the the background
     *
     * @return True if the extension was registered properly.
     */
    private boolean registerOrUpdateExtension() {
        if (Dbg.DEBUG) {
            Dbg.d("Start registration of extension.");
        }

        try {
            // Register or update extension
            if (!isRegistered()) {
                register();
                if (Dbg.DEBUG) {
                    Dbg.d("Registered extension.");
                }
            } else {
                updateRegistration();
                if (Dbg.DEBUG) {
                    Dbg.d("Updated extension.");
                }
            }
            if (mRegistrationInformation.getRequiredNotificationApiVersion() > 0) {
                // Register all sources
                registerOrUpdateSources();
            }

        } catch (RegisterExtensionException exception) {
            if (Dbg.DEBUG) {
                Dbg.e("Failed to register extension", exception);
            }
            return false;
        }

        return true;
    }

    /**
     * Find out if this extension is registered or not
     *
     * @return True if registered
     */
    private boolean isRegistered() {
        return ExtensionUtils.getExtensionId(mContext) != ExtensionUtils.INVALID_ID;
    }

    /**
     * Register extension. This method is called from the the background
     *
     * @param context The context.
     * @throws RegisterExtensionException
     */
    private void register() throws RegisterExtensionException {
        ContentValues configurationValues = mRegistrationInformation
                .getExtensionRegistrationConfiguration();
        try {
            if (!configurationValues
                    .containsKey(Registration.ExtensionColumns.NOTIFICATION_API_VERSION)) {
                configurationValues.put(Registration.ExtensionColumns.NOTIFICATION_API_VERSION,
                        mRegistrationInformation.getRequiredNotificationApiVersion());
            } else {
                if (configurationValues
                        .getAsInteger(Registration.ExtensionColumns.NOTIFICATION_API_VERSION) != mRegistrationInformation
                        .getRequiredNotificationApiVersion()) {
                    throw new RegisterExtensionException(
                            "NOTIFICATION_API_VERSION did not match getRequiredNotificationApiVersion");
                }
            }

            // Package name is not required but many of the SDK utility
            // methods are dependent of the package name.
            configurationValues.put(Registration.ExtensionColumns.PACKAGE_NAME,
                    mContext.getPackageName());

            Uri uri = mContext.getContentResolver().insert(Registration.Extension.URI,
                    configurationValues);
            if (uri == null) {
                DeviceInfoHelper.removeUnsafeValues(mContext, 1, configurationValues);
                uri = mContext.getContentResolver().insert(Registration.Extension.URI,
                        configurationValues);
                if (uri == null) {
                    throw new RegisterExtensionException("failed to insert extension");
                } else {
                    Dbg.e("Extension registered updated after retry!");
                }
            }
        } catch (SQLException exception) {
            // Registration failed, possibly due to configurationValues that
            // don't exist in v1 registration table, remove them and retry
            try {
                DeviceInfoHelper.removeUnsafeValues(mContext, 1, configurationValues);
                Uri uri = mContext.getContentResolver().insert(Registration.Extension.URI,
                        configurationValues);

                if (uri == null) {
                    throw new RegisterExtensionException("failed to insert extension");
                } else {
                    Dbg.e("Extension registered updated after retry!");

                }
            } catch (SQLException e) {
                logAndThrow("Update source failed", exception);
            }
        } catch (SecurityException exception) {
            logAndThrow("Failed to register", exception);
        } catch (IllegalArgumentException exception) {
            // If Liveware Manager is not installed.
            // When the extension is started from Liveware Manager this should
            // not happen.
            logAndThrow("Failed to register. Is Liveware Manager installed?", exception);
        }
    }

    /**
     * Update extension registration. This method is called from the the
     * background
     *
     * @param context The context.
     * @throws RegisterExtensionException
     */
    private void updateRegistration() throws RegisterExtensionException {
        if (Dbg.DEBUG) {
            Dbg.d("Updating existing registration.");
        }
        String where = Registration.ExtensionColumns.PACKAGE_NAME + " = ?";
        String[] selectionArgs = new String[] {
                mContext.getPackageName()
        };
        try {
            ContentValues values = mRegistrationInformation.getExtensionRegistrationConfiguration();
            DeviceInfoHelper.removeUnsafeValues(mContext, values);
            mContext.getContentResolver().update(Registration.Extension.URI,
                    values, where,
                    selectionArgs);
        } catch (SQLException exception) {
            logAndThrow("Failed to update registration", exception);
        } catch (SecurityException exception) {
            logAndThrow("Failed to update registration", exception);
        } catch (IllegalArgumentException exception) {
            logAndThrow("Failed to update registration", exception);
        }
    }

    /**
     * Register or update source. This method is called from the the background
     *
     * @param extensionSpecificId The source type to register.
     * @throws RegisterExtensionException
     */
    private void registerOrUpdateSources() throws RegisterExtensionException {
        ArrayList<String> oldExtensionSpecificIds = NotificationUtil
                .getExtensionSpecificIds(mContext);

        for (ContentValues sourceConfiguration : mRegistrationInformation
                .getSourceRegistrationConfigurations()) {
            String extensionSpecificId = (String) sourceConfiguration
                    .get(Notification.SourceColumns.EXTENSION_SPECIFIC_ID);
            // If we find the source id in the database then we have already
            // registered.
            long sourceId = NotificationUtil.getSourceId(mContext, extensionSpecificId);

            // Package name is not required but many of the SDK utility
            // methods are dependent of the package name.
            sourceConfiguration.put(SourceColumns.PACKAGE_NAME, mContext.getPackageName());

            if (sourceId == NotificationUtil.INVALID_ID) {
                sourceId = registerSource(sourceConfiguration);
            } else {
                updateSource(sourceConfiguration, sourceId);
            }
            if (Dbg.DEBUG) {
                Dbg.d("SourceType:" + extensionSpecificId + " SourceId:" + sourceId);
            }

            oldExtensionSpecificIds.remove(extensionSpecificId);
        }

        // Remove any sources that are no longer used.
        for (String deletedExtensionSpecificId : oldExtensionSpecificIds) {
            unregisterSource(deletedExtensionSpecificId);
        }
    }

    /**
     * Register source. This method is called from the the background
     *
     * @param context The context.
     * @param extensionSpecificId The extension specific id.
     * @return The source id.
     * @throws RegisterExtensionException
     */
    private long registerSource(ContentValues sourceValues) throws RegisterExtensionException {
        long sourceId = NotificationUtil.INVALID_ID;

        try {
            DeviceInfoHelper.removeUnsafeValues(mContext, sourceValues);
            Uri uri = mContext.getContentResolver().insert(Notification.Source.URI, sourceValues);
            if (uri == null) {
                throw new RegisterExtensionException("failed to insert source");
            }
            sourceId = (int) ContentUris.parseId(uri);
        } catch (SQLException exception) {
            logAndThrow("Register source failed", exception);
        } catch (SecurityException exception) {
            logAndThrow("Register source failed", exception);
        } catch (IllegalArgumentException exception) {
            logAndThrow("Register source failed", exception);
        }

        return sourceId;
    }

    /**
     * Update source information. This method is called from the the background
     *
     * @param context The context.
     * @param extensionSpecificId The extension specific id.
     * @param sourceId The source id.
     * @throws RegisterExtensionException
     */
    private void updateSource(ContentValues sourceValues, long sourceId)
            throws RegisterExtensionException {

        try {
            DeviceInfoHelper.removeUnsafeValues(mContext, sourceValues);
            int result = NotificationUtil.updateSources(mContext, sourceValues, BaseColumns._ID
                    + " = " + sourceId, null);

            if (result != 1) {
                if (Dbg.DEBUG) {
                    Dbg.e("Failed to update source");
                }
            }
        } catch (SQLException exception) {
            logAndThrow("Update source failed", exception);
        } catch (SecurityException exception) {
            logAndThrow("Update source failed", exception);
        } catch (IllegalArgumentException exception) {
            logAndThrow("Update source failed", exception);
        }
    }

    /**
     * Unregister source.
     *
     * @param sourceType The source type.
     * @throws RegisterExtensionException
     */
    private void unregisterSource(String extensionSpecificId) throws RegisterExtensionException {
        try {
            int noOfDeletedRows = NotificationUtil.deleteSources(mContext,
                    SourceColumns.EXTENSION_SPECIFIC_ID + "=" + "'" + extensionSpecificId + "'",
                    null);
            if (noOfDeletedRows == 0) {
                if (Dbg.DEBUG) {
                    Dbg.d("Source was already unregistered: " + extensionSpecificId);
                }
            } else {
                if (Dbg.DEBUG) {
                    Dbg.d("Unregistered source: " + extensionSpecificId);
                }
            }
        } catch (SQLException exception) {
            logAndThrow("Unregister source failed", exception);
        } catch (SecurityException exception) {
            logAndThrow("Unregister source failed", exception);
        } catch (IllegalArgumentException exception) {
            logAndThrow("Update source failed", exception);
        }
    }

    /**
     * Register with all host applications that supports the extension widget
     * and/or control requirements. This method is called from the the
     * background
     *
     * @param widgetReceiver The widget receiver for widget events. Null if no
     *            widget functionality.
     * @param controlReceiver The control receiver for control events. Null if
     *            no control functionality.
     */
    private void registerWithAllHostApps() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    Registration.HostApp.URI,
                    new String[] {
                            Registration.HostAppColumns._ID,
                            Registration.HostAppColumns.PACKAGE_NAME,
                            Registration.HostAppColumns.WIDGET_API_VERSION,
                            Registration.HostAppColumns.CONTROL_API_VERSION,
                            Registration.HostAppColumns.SENSOR_API_VERSION,
                            Registration.HostAppColumns.NOTIFICATION_API_VERSION,
                            Registration.HostAppColumns.WIDGET_REFRESH_RATE
                    }, null, null, null);
            if (cursor == null) {
                if (Dbg.DEBUG) {
                    Dbg.e("checkHostAppRegistration: cursor==null");
                }
                return;
            }
            if (cursor.getCount() == 0) {
                // No host apps available.
                return;
            }

            // Loop through the host apps.
            cursor.moveToFirst();
            int packageColumnIndex = cursor
                    .getColumnIndex(Registration.HostAppColumns.PACKAGE_NAME);
            int hostAppIdColumnIndex = cursor.getColumnIndex(Registration.HostAppColumns._ID);
            int widgetApiColumnIndex = cursor
                    .getColumnIndex(Registration.HostAppColumns.WIDGET_API_VERSION);
            int controlApiColumnIndex = cursor
                    .getColumnIndex(Registration.HostAppColumns.CONTROL_API_VERSION);
            int sensorApiColumnIndex = cursor
                    .getColumnIndex(Registration.HostAppColumns.SENSOR_API_VERSION);
            int notificationApiColumnIndex = cursor
                    .getColumnIndexOrThrow(Registration.HostAppColumns.NOTIFICATION_API_VERSION);
            int widgetRefreshRateColumnIndex = cursor
                    .getColumnIndexOrThrow(Registration.HostAppColumns.WIDGET_REFRESH_RATE);
            while (!cursor.isAfterLast()) {
                String packageName = cursor.getString(packageColumnIndex);
                long hostAppId = cursor.getLong(hostAppIdColumnIndex);
                int widgetApiVersion = cursor.getInt(widgetApiColumnIndex);
                int controlApiVersion = cursor.getInt(controlApiColumnIndex);
                int sensorApiVersion = cursor.getInt(sensorApiColumnIndex);
                int notificationApiVersion = cursor.getInt(notificationApiColumnIndex);
                int widgetRefreshRate = cursor.getInt(widgetRefreshRateColumnIndex);

                HostApplicationInfo hostApplication = new HostApplicationInfo(mContext,
                        packageName, hostAppId, widgetApiVersion, controlApiVersion,
                        sensorApiVersion, notificationApiVersion, widgetRefreshRate);

                List<ContentValues> widgetRegistrationValues = new ArrayList<ContentValues>();
                boolean widgetSupported = false;
                if (widgetApiVersion < 3 || mRegistrationInformation.getTargetWidgetApiVersion() < 3 ||
                        ExtensionUtils.getRegistrationVersion(mContext) < 3) {
                    // Host application can only have one widget type, either it is supported or not.
                    widgetSupported = mRegistrationInformation.isSupportedWidgetAvailable(
                            mContext, hostApplication);
                } else {
                    // Host application can have several different displays,
                    // each with different widget containers
                    for (DeviceInfo device : hostApplication.getDevices()) {
                        for (DisplayInfo display : device.getDisplays()) {
                            for (WidgetContainer widgetContainer : display.getWidgetContainers()) {
                                List<ContentValues> valuesList = mRegistrationInformation
                                        .getWidgetRegistrationValues(mContext, packageName,
                                                widgetContainer,
                                                ExtensionUtils.getRegistrationVersion(mContext));
                                if (valuesList != null && !valuesList.isEmpty()) {
                                    widgetSupported = true;
                                    for (ContentValues values : valuesList) {
                                        widgetRegistrationValues.add(values);
                                    }
                                }
                            }
                        }
                    }
                }

                boolean controlSupported = mRegistrationInformation.isSupportedControlAvailable(
                        mContext, hostApplication);
                boolean sensorSupported = mRegistrationInformation.isSupportedSensorAvailable(
                        mContext, hostApplication);

                // If widget, control or sensor was supported then register with
                // the host app.
                if (widgetSupported || controlSupported || sensorSupported) {
                    registerApiRegistration(hostApplication, packageName,
                            isHostAppRegistered(packageName), widgetSupported, controlSupported,
                            sensorSupported,
                            mRegistrationInformation.controlInterceptsBackButton(),
                            mRegistrationInformation.supportsLowPowerMode());
                }


                if (ExtensionUtils.getRegistrationVersion(mContext) >= 3 && widgetApiVersion >= 3) {
                    long extensionId = ExtensionUtils.getExtensionId(mContext);
                    long apiRegistrationId = ExtensionUtils.getRegistrationId(mContext,
                            packageName, extensionId);

                    List<String> oldWidgetKeys = getWidgetRegistrationKeys(apiRegistrationId);

                    if (widgetRegistrationValues.size() > 0) {
                        for (ContentValues values : widgetRegistrationValues) {
                            String key = values.getAsString(Registration.WidgetRegistrationColumns.KEY);
                            oldWidgetKeys.remove(key);
                            registerOrUpdateWidgetRegistration(apiRegistrationId, values);
                        }
                    }

                    // Delete any widget registration that are no longer supported.
                    for (String key : oldWidgetKeys) {
                        mContext.getContentResolver().delete(Registration.WidgetRegistration.URI,
                                Registration.WidgetRegistrationColumns.KEY + " = ?", new String[] {
                                    key
                                });
                    }
                }

                cursor.moveToNext();
            }
        } catch (SQLException exception) {
            if (Dbg.DEBUG) {
                Dbg.e("registerWithAllHostApps: " + exception.getMessage());
            }
        } catch (SecurityException exception) {
            if (Dbg.DEBUG) {
                Dbg.e("registerWithAllHostApps: " + exception.getMessage());
            }
        } catch (IllegalArgumentException exception) {
            if (Dbg.DEBUG) {
                Dbg.e("registerWithAllHostApps: " + exception.getMessage());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Checks if the extension is registered with a host application. This
     * method is called from the the background
     *
     * @param packageName The package name of the host application.
     * @return True if the extension is registered with the host application.
     */
    private boolean isHostAppRegistered(String packageName) {
        Cursor cursor = null;
        boolean isRegistered = false;
        long extensionId = ExtensionUtils.getExtensionId(mContext);
        String selection = Registration.ApiRegistrationColumns.EXTENSION_ID + " = " + extensionId
                + " AND " + Registration.ApiRegistrationColumns.HOST_APPLICATION_PACKAGE + " = ?";
        String[] selectionArgs = new String[] {
                packageName
        };

        try {
            cursor = mContext.getContentResolver().query(Registration.ApiRegistration.URI,
                    new String[] {
                        Registration.ApiRegistrationColumns.HOST_APPLICATION_PACKAGE
                    },
                    selection, selectionArgs, null);
            if (cursor != null) {
                isRegistered = (cursor.getCount() > 0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isRegistered;
    }

    /**
     * Register our extension with a host application. Override this to provide
     * extension specific implementation. This method is called from the the
     * background
     *
     * @param packageName The package name of host application.
     * @param isRegistered true if already registered.
     * @param widgetApiVersionSupported True if widget registration.
     * @param controlApiVersionSupported True if control registration.
     * @return True if registration was successful.
     */
    private boolean registerApiRegistration(HostApplicationInfo hostApplication,
            String packageName, boolean isRegistered,
            boolean widgetApiVersionSupported, boolean controlApiVersionSupported,
            boolean sensorApiVersionSupported, boolean controlInterceptsBack,
            boolean lowPowerSupport) {
        if (Dbg.DEBUG) {
            Dbg.d("Register API registration: " + packageName);
        }
        ContentValues values = new ContentValues();
        values.put(Registration.ApiRegistrationColumns.HOST_APPLICATION_PACKAGE, packageName);
        if (widgetApiVersionSupported) {
            // The api version inserted is based on either the supported level
            // in host app
            // or the target API version, whichever is lower.
            int apiVersion = Math.min(mRegistrationInformation.getTargetWidgetApiVersion(),
                    hostApplication.getWidgetApiVersion());
            values.put(Registration.ApiRegistrationColumns.WIDGET_API_VERSION, apiVersion);
        } else {
            values.put(Registration.ApiRegistrationColumns.WIDGET_API_VERSION, 0);
        }
        if (controlApiVersionSupported) {
            int apiVersion = Math.min(mRegistrationInformation.getTargetControlApiVersion(),
                    hostApplication.getControlApiVersion());
            values.put(Registration.ApiRegistrationColumns.CONTROL_API_VERSION, apiVersion);
        } else {
            values.put(Registration.ApiRegistrationColumns.CONTROL_API_VERSION, 0);
        }
        if (sensorApiVersionSupported) {
            int apiVersion = Math.min(mRegistrationInformation.getTargetSensorApiVersion(),
                    hostApplication.getControlApiVersion());
            values.put(Registration.ApiRegistrationColumns.SENSOR_API_VERSION, apiVersion);
        } else {
            values.put(Registration.ApiRegistrationColumns.SENSOR_API_VERSION, 0);
        }
        if (controlInterceptsBack) {
            values.put(Registration.ApiRegistrationColumns.CONTROL_BACK_INTERCEPT,
                    mRegistrationInformation.controlInterceptsBackButton());
        } else {
            values.put(Registration.ApiRegistrationColumns.CONTROL_BACK_INTERCEPT, 0);
        }
        if (lowPowerSupport) {
            values.put(Registration.ApiRegistrationColumns.LOW_POWER_SUPPORT,
                    mRegistrationInformation.supportsLowPowerMode());
        } else {
            values.put(Registration.ApiRegistrationColumns.LOW_POWER_SUPPORT, 0);
        }

        boolean res = false;
        long extensionId = ExtensionUtils.getExtensionId(mContext);
        if (!isRegistered) {
            values.put(Registration.ApiRegistrationColumns.EXTENSION_ID, extensionId);
            Uri uri = null;
            try {

                uri = mContext.getContentResolver()
                        .insert(Registration.ApiRegistration.URI, values);

                if (uri == null) {
                    // It's possible the registration did nothing because of an
                    // old
                    // database. Let's remove all API 2 values and try again
                    DeviceInfoHelper.removeUnsafeValues(mContext, 1, values);
                    uri = mContext.getContentResolver()
                            .insert(Registration.ApiRegistration.URI, values);
                }
            } catch (SQLException exception) {
                // It's possible the registration failed with an exception
                // because of an old database. Let's remove all API 2 values and
                // try again
                DeviceInfoHelper.removeUnsafeValues(mContext, 1, values);
                uri = mContext.getContentResolver()
                        .insert(Registration.ApiRegistration.URI, values);
            }
            res = uri != null;
        } else {
            long _id = ExtensionUtils.getRegistrationId(mContext, packageName, extensionId);

            int rows = 0;
            try {
                rows = mContext.getContentResolver().update(
                        ContentUris.withAppendedId(Registration.ApiRegistration.URI, _id), values,
                        null, null);
                if (rows == 0) {
                    // It's possible the registration update failed because of
                    // an old database. Let's remove all API 2 values and try again
                    DeviceInfoHelper.removeUnsafeValues(mContext, 1, values);
                    rows = mContext.getContentResolver().update(
                            ContentUris.withAppendedId(Registration.ApiRegistration.URI, _id),
                            values,
                            null, null);
                }
            } catch (SQLException exception) {
                // It's possible the registration update failed because of an
                // old database. Let's remove all API 2 values and try again
                DeviceInfoHelper.removeUnsafeValues(mContext, 1, values);
                rows = mContext.getContentResolver().update(
                        ContentUris.withAppendedId(Registration.ApiRegistration.URI, _id), values,
                        null, null);
            }
            res = rows > 0;
        }
        return res;
    }

    /**
     * Get the widget registration id from a widget registration key.
     *
     * @param apiRegistrationId The API registration id.
     * @param key The key.
     * @return The widget registration id.
     */
    private long getWidgetRegistrationId(long apiRegistrationId, String key) {
        if (ExtensionUtils.getRegistrationVersion(mContext) < 3) {
            // Widget registration only possible if Registration API version 3 or later.
            return ExtensionUtils.INVALID_ID;
        }

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    Registration.WidgetRegistration.URI,
                    new String[] {
                            Registration.WidgetRegistrationColumns._ID
                    }
                    ,Registration.WidgetRegistrationColumns.API_REGISTRATION_ID + " = ? AND "
                            + Registration.WidgetRegistrationColumns.KEY + " = ?", new String[] {
                            Long.toString(apiRegistrationId), key
                    }, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(Registration.WidgetRegistrationColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return ExtensionUtils.INVALID_ID;
    }

    /**
     * Register or update widget registration.
     *
     * @param apiRegistrationId The API registration ID.
     * @param values The content values to register.
     * @return True if the registration was successful.
     */
    private boolean registerOrUpdateWidgetRegistration(long apiRegistrationId, ContentValues values) {
        if (ExtensionUtils.getRegistrationVersion(mContext) < 3) {
            // Widget registration only possible if Registration API version 3 or later.
            return false;
        }

        values.put(Registration.WidgetRegistrationColumns.API_REGISTRATION_ID, apiRegistrationId);

        long widgetRegistrationId = getWidgetRegistrationId(apiRegistrationId,
                values.getAsString(Registration.WidgetRegistrationColumns.KEY));

        if (widgetRegistrationId != ExtensionUtils.INVALID_ID) {
            return mContext.getContentResolver().update(Registration.WidgetRegistration.URI,
                    values, Registration.WidgetRegistrationColumns._ID + " = ?", new String[] {
                        Long.toString(widgetRegistrationId)
                    }) > 0;

        } else {
            return mContext.getContentResolver()
                    .insert(Registration.WidgetRegistration.URI, values) != null;
        }
    }

    /**
     * Get all widget registration keys for a specific API registration ID.
     *
     * @param apiRegistrationId The API registration id.
     * @return A list with the keys.
     */
    private List<String> getWidgetRegistrationKeys(long apiRegistrationId) {
        List<String> keys = new ArrayList<String>();
        if (ExtensionUtils.getRegistrationVersion(mContext) < 3) {
            return keys;
        }

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Registration.WidgetRegistration.URI,
                    new String[] {
                        Registration.WidgetRegistrationColumns.KEY
                    }, Registration.WidgetRegistrationColumns.API_REGISTRATION_ID + " = ?",
                    new String[] {
                        Long.toString(apiRegistrationId)
                    }, null);
            while (cursor != null && cursor.moveToNext()) {
                String key = cursor.getString(cursor
                        .getColumnIndex(Registration.WidgetRegistrationColumns.KEY));
                keys.add(key);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return keys;
    }

    /**
     * Write to log and throw exception This method is called from the the
     * background
     *
     * @param text Text to write to log
     * @param exception exception to throw
     * @throws RegisterExtensionException
     */
    private void logAndThrow(String text, Exception exception) throws RegisterExtensionException {
        if (Dbg.DEBUG) {
            Dbg.e(text, exception);
        }
        throw new RegisterExtensionException(text);
    }

    private static class RegisterExtensionException extends Exception {

        private static final long serialVersionUID = 8351396734279924253L;

        public RegisterExtensionException(String string) {
            super(string);
        }
    }


}
