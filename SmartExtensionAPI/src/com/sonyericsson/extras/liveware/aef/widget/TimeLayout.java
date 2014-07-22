/*
Copyright (C) 2013 Sony Mobile Communications AB

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

package com.sonyericsson.extras.liveware.aef.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.widget.LinearLayout;

import com.sonyericsson.extras.liveware.sdk.R;

/**
 * This class enables grouping of TimeView views. It provides horizontal
 * alignment based on each child's size.
 * It also makes it possible to offset the time for all child views.
 *
 * It can only contain TimeViews and will only position items horizontally.
 *
 * <p>XML Attributes</p>
 * <ul>
 * <li>timeOffset: Positive or negative offset in seconds that should be applied to all child views.</li>
 * <li>gravity: Specifies how to place the children, both on the x- and y-axis, within the layout.</li>
 * </ul>
 **/
public class TimeLayout extends LinearLayout {

    private int mGravity;

    private int mTimeOffset;

    public TimeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeLayout, 0, 0);
        try {
            setGravity(a.getInt(R.styleable.TimeLayout_gravity, 0));
            mTimeOffset = a.getInt(R.styleable.TimeLayout_timeOffset, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        if (child instanceof TimeView) {
            super.addView(child, params);
        } else {
            throw new InflateException("Only TimeViews are allowed as children in TimeLayout.");
        }
    }

    /**
     * Specifies how to place the content of an object, both on the x- and
     * y-axis, within the object itself.
     *
     * @see Gravity
     */
    @Override
    public void setGravity(int gravity) {
        mGravity = gravity;
        super.setGravity(gravity);
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     * @return negative or positive offset from the real time in seconds.
     */
    public int getTimeOffset() {
        return mTimeOffset;
    }
    /**
     * Contains constants for use with setGravity
     */
    public static class Gravity {
        /**
         * Push object to the top of its container, not changing its size.
         */
        public static final int TOP = 30;
        /**
         * Push object to the bottom of its container, not changing its size.
         */
        public static final int BOTTOM = 50;
        /**
         * Push object to the left of its container, not changing its size.
         */
        public static final int LEFT = 3;
        /**
         * Push object to the left of its container, not changing its size.
         */
        public static final int RIGHT = 5;
        /**
         * Push object to the in the vertical center of its container, not changing its size.
         */
        public static final int CENTER_VERTICAL = 10;
        /**
         * Push object to the in the horizontal center of its container, not changing its size.
         */
        public static final int CENTER_HORIZONTAL = 1;
        /**
         * Push object to the center of its container, not changing its size.
         */
        public static final int CENTER_ = 11;
    }
}
