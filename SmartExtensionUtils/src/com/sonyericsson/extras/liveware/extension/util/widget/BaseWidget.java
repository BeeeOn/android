
package com.sonyericsson.extras.liveware.extension.util.widget;

import android.content.Context;

import com.sonyericsson.extras.liveware.aef.widget.Widget.Category;

public abstract class BaseWidget extends WidgetExtension {

    /**
     * BaseWidget constructor. This constructor is used by the system and should not be overwritten
     * or used for instantiation.
     * @see BaseWidget#onCreate()
     * @param bundle
     */
    public BaseWidget(WidgetBundle bundle) {
        super(bundle.getContext(), bundle.getHostAppPackageName());
        mInstanceId = bundle.getInstanceId();
    }

    /**
     * Called when the widget is starting. This is where most initialization should go.
     */
    public void onCreate() {

    }

    /**
     * Get the widget width in pixels. Is written to the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#WIDTH}
     * field.
     *
     * @return The width.
     */
    public abstract int getWidth();

    /**
     * Get the widget height in pixels. Is written to the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#HEIGHT}
     * field.
     *
     * @return The height.
     */
    public abstract int getHeight();

    /**
     * Get the widget preview image resource id used to describe this widget to the user in
     * the host application settings. The string resource is written to the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#PREVIEW_IMAGE_URI}
     * field.
     * {@link com.sonyericsson.extras.liveware.extension.util.ExtensionUtils#getUriString}
     * can be used to retrieve the string from a resource id.
     *
     * @return The preview image URI as a string.
     */
    public abstract int getPreviewUri();

    /**
     * Get the widget resource name used to describe this widget to the user in the host
     * application settings. The string resource written to the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#NAME}
     * field.
     *
     * @return The name.
     */
    public abstract int getName();

    /**
     * Get the widget category. The string resource written to the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetRegistrationColumns#CATEGORY}
     * field.
     *
     * @return The category
     */
    public int getCategory() {
        return Category.DEFAULT;
    }

    /**
     * Simple class that encapsulates values to instantiate BaseWidget
     */
    public static final class WidgetBundle {
        private final Context context;
        private final String hostAppPackageName;
        private final Integer instanceId;

        public WidgetBundle(Context context, String hostAppPackageName, Integer instanceId) {
            this.context = context.getApplicationContext();
            this.hostAppPackageName = hostAppPackageName;
            this.instanceId = instanceId;
        }

        public Context getContext() {
            return context;
        }

        public String getHostAppPackageName() {
            return hostAppPackageName;
        }

        public Integer getInstanceId() {
            return instanceId;
        }
    }
}
