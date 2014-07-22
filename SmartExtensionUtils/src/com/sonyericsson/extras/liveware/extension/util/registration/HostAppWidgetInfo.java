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

/**
 * The widget info class describes a host application widget. The information is
 * retrieved from the
 * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.Widget}
 * table.
 */
public class HostAppWidgetInfo {

    private final long mId;
    private final int mCellWidth;
    private final int mCellHeight;
    private final int mMaxWidth;
    private final int mMaxHeight;
    private final int mDisplayMode;
    private final String mType;

    /**
     * Create a host application widget info object.
     *
     * @param id
     * @param cellWidth
     * @param cellHeight
     * @param maxWidth
     * @param maxHeight
     * @param displayMode
     * @param type
     */
    public HostAppWidgetInfo(long id, int cellWidth, int cellHeight, int maxWidth, int maxHeight,
            int displayMode, String type) {
        super();
        mId = id;
        mCellWidth = cellWidth;
        mCellHeight = cellHeight;
        mMaxWidth = maxWidth;
        mMaxHeight = maxHeight;
        mDisplayMode = displayMode;
        mType = type;
    }

    /**
     * The ID of the widget. Is populated from the
     * {@link android.provider.BaseColumns#_ID} field of the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.Widget}
     * table.
     *
     * @return The ID.
     */
    public long getId() {
        return mId;
    }

    /**
     * The width of a cell in pixels. Is populated from the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetColumns#CELL_WIDTH}
     * field.
     *
     * @return The cell width.
     */
    public int getCellWidth() {
        return mCellWidth;
    }

    /**
     * The height of a cell in pixels. Is populated from the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetColumns#CELL_HEIGHT}
     * field.
     *
     * @return The cell height.
     */
    public int getCellHeight() {
        return mCellHeight;
    }

    /**
     * The maximum width of a widget in pixels. Is populated from the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetColumns#MAX_WIDTH}
     * field.
     *
     * @return The maximum width.
     */
    public int getMaxWidth() {
        return mMaxWidth;
    }

    /**
     * The maximum height of a widget in pixels. Is populated from the
     * {@link com.sonyericsson.extras.liveware.aef.registration.Registration.WidgetColumns#MAX_HEIGHT}
     * field.
     *
     * @return The maximum height.
     */
    public int getMaxHeight() {
        return mMaxHeight;
    }

    /**
     * The display mode
     *
     * @return the mDisplayMode
     */
    public int getDisplayMode() {
        return mDisplayMode;
    }

    /**
     * @return the mType
     */
    public String getType() {
        return mType;
    }
}
