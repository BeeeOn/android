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
import android.database.Cursor;
import android.database.SQLException;

import com.sonyericsson.extras.liveware.aef.registration.Registration.Widget;
import com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetColumns;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * The display info describes a host application display.
 */
public class DisplayInfo {

    private final Context mContext;

    private final long mId;

    private final int mWidth;

    private final int mHeight;

    private final int mColors;

    private final int mRefreshRate;

    private final int mLatency;

    private final boolean mTapTouch;

    private final boolean mMotionTouch;

    private List<WidgetContainer> mWidgetContainers = null;

    /**
     * Create display info.
     *
     * @param context The context.
     * @param id The id.
     * @param width The width.
     * @param height The height.
     * @param colors The colors.
     * @param refreshRate The refresh rate.
     * @param latency The latency.
     * @param tapTouch True if tap touch is supported.
     * @param motionTouch True if motion touch is supported.
     */
    public DisplayInfo(Context context, final long id, final int width, final int height,
            final int colors, final int refreshRate, final int latency,
            final boolean tapTouch, final boolean motionTouch) {
        mContext = context;
        mId = id;
        mWidth = width;
        mHeight = height;
        mColors = colors;
        mRefreshRate = refreshRate;
        mLatency = latency;
        mTapTouch = tapTouch;
        mMotionTouch = motionTouch;
    }

    /**
     * Get the id.
     *
     * @see Registration.DisplayColumns.#_ID
     *
     * @return The id.
     */
    public long getId() {
        return mId;
    }

    /**
     * Get the width.
     *
     * @see Registration.DisplayColumns.#DISPLAY_WIDTH
     *
     * @return The width.
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Get the height.
     *
     * @see Registration.DisplayColumns.#DISPLAY_HEIGHT
     *
     * @return The height.
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Get the number of colors supported by the display.
     *
     * @see Registration.DisplayColumns.#COLORS
     *
     * @return The number of colors.
     */
    public int getColors() {
        return mColors;
    }

    /**
     * Get the refresh rate supported by the display.
     *
     * @see Registration.DisplayColumns.#REFRESH_RATE
     *
     * @return The refresh rate.
     */
    public int getRefreshRate() {
        return mRefreshRate;
    }

    /**
     * Get the display latency.
     *
     * @see Registration.DisplayColumns.#LATENCY
     *
     * @return The latency.
     */
    public int getLatency() {
        return mLatency;
    }

    /**
     * Is tap touch supported.
     *
     * @see Registration.DisplayColumns.#TAP_TOUCH
     *
     * @return True if tap touch is supported.
     */
    public boolean isTapTouch() {
        return mTapTouch;
    }

    /**
     * Is motion touch supported.
     *
     * @see Registration.DisplayColumns.#MOTION_TOUCH
     *
     * @return True if motion touch is supported.
     */
    public boolean isMotionTouch() {
        return mMotionTouch;
    }

    /**
     * Check if the display size is equal to the provided width and height.
     *
     * @param width The width to check.
     * @param height The height to check.
     * @return True if the display is equal to the provided with and height.
     */
    public boolean sizeEquals(int width, int height) {
        return (mWidth == width && mHeight == height);
    }

    /**
     * Get the widget containers available for this display.
     *
     * @return The widgets.
     */
    public List<WidgetContainer> getWidgetContainers() {
        if (mWidgetContainers != null) {
            // List of displays already available. Avoid re-reading from
            // database.
            return mWidgetContainers;
        }
        mWidgetContainers = new ArrayList<WidgetContainer>();

        // Specific widget info only available in V3 or later.
        if (ExtensionUtils.getRegistrationVersion(mContext) < 3) {
            return mWidgetContainers;
        }

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Widget.URI, null,
                    WidgetColumns.DISPLAY_ID + " = " + mId, null, null);
            while (cursor != null && cursor.moveToNext()) {
                long widgetId = cursor.getLong(cursor.getColumnIndexOrThrow(WidgetColumns._ID));
                int cellWidth = cursor.getInt(cursor
                        .getColumnIndexOrThrow(WidgetColumns.CELL_WIDTH));
                int cellHeight = cursor.getInt(cursor
                        .getColumnIndexOrThrow(WidgetColumns.CELL_HEIGHT));
                int maxWidth = cursor.getInt(cursor.getColumnIndexOrThrow(WidgetColumns.MAX_WIDTH));
                int maxHeight = cursor.getInt(cursor
                        .getColumnIndexOrThrow(WidgetColumns.MAX_HEIGHT));
                int accessoryStates = cursor.getInt(cursor
                        .getColumnIndexOrThrow(WidgetColumns.ACCESSORY_STATE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(WidgetColumns.TYPE));
                WidgetContainer widgetContainer = new WidgetContainer(widgetId, cellWidth,
                        cellHeight, maxWidth,
                        maxHeight, accessoryStates, type);
                mWidgetContainers.add(widgetContainer);
            }
        } catch (SQLException e) {
            if (Dbg.DEBUG) {
                Dbg.w("Failed to query widget containers", e);
            }
        } catch (SecurityException e) {
            if (Dbg.DEBUG) {
                Dbg.w("Failed to query widget containers", e);
            }
        } catch (IllegalArgumentException e) {
            if (Dbg.DEBUG) {
                Dbg.w("Failed to query widget containers", e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return mWidgetContainers;

    }

}
