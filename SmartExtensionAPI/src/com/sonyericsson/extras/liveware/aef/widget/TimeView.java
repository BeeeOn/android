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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.LevelListDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.sdk.R;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * <p>This class represents a view with state that changes over time.
 * It can be configured to show one of the supported periods
 * represented by {@link TimeType}. It needs to be nested in a
 * TimeLayout to work.</p>
 *
 * <p>It is by default configured to display a single state.</p>
 *
 * <p>The appearance can be customized in mainly three ways:</p>
 * <ul>
 * <li>Setting a custom font using the fontPath attribute</li>
 * <li>Setting the strings that are shown in the TimeView (e.g. "January", "February" etc) using the textArray attribute.
 * <li>Setting the images that the TimeView should show using the background attribute. The background drawable set should be a LevelListDrawable resource with a number of drawables equal to TimeView's {@link TimeView#getTimeStateCount()} .</li>
 * </ul>
 *
 * <a name="TimeViewXmlAttributes"></a>
 * <p>XML Attributes</p>
 * <ul>
 * <li>timeType: Sets the type of time interval and thus the number of states. Default type is <i>{@link TimeView.TimeType#Constant }</i>.</li>
 * <li>textSize: The font size. Use pixels to prevent scaling. MATCH_PARENT will make the view find a size that allows for the current text to fit within the parent.</li>
 * <li>fontPath: Path in the asset folder to custom font to use instead of the device default font. The font will not be rendered in the layout editor.</li>
 * <li>textArray: An array of strings that should be displayed over time. It should have the same number of strings as the number of states indicated by the TimeView's timeType.</li>
 * <li>template: DateFormat template for default values.</li>
 * </ul>
 * </p>
 **/
public class TimeView extends TextView {

    private static class FontCache {
        private final HashMap<String, Typeface> fontMap = new HashMap<String, Typeface>();

        private FontCache() {}

        public Typeface getFont(Context c, String fontPath) {
            Typeface typeface = fontMap.get(fontPath);
            if (typeface == null) {
                typeface = Typeface.createFromAsset(c.getAssets(), fontPath);
                fontMap.put(fontPath, typeface);
            }
            return typeface;
        }
    }

    /**
     * This enumeration represents the cyclic periods that the view supports.
     * Each period implies a fixed number of states.
     */
    public enum TimeType {
        Constant,
        Seconds,
        SecondsDigit2,
        SecondsDigit1,
        Minutes,
        MinutesDigit2,
        MinutesDigit1,
        Hours,
        HoursDigit2,
        HoursDigit1,
        DaysOfWeek,
        DaysOfMonth,
        DaysOfMonthDigit2,
        DaysOfMonthDigit1,
        DaysOfYear,
        DaysOfYearDigit3,
        DaysOfYearDigit2,
        DaysOfYearDigit1,
        Months,
        MonthsDigit2,
        MonthsDigit1,
        YearsDigit4,
        YearsDigit3,
        YearsDigit2,
        YearsDigit1,
        AmPm,
        HoursFine
    }

    private static FontCache sFontCache = new FontCache();

    private TimeType mTimeType;

    private CharSequence[] mTimeTextResArray;

    private int mTextSize = ViewGroup.LayoutParams.MATCH_PARENT;

    private final Paint mFontPaint;

    private String mTemplate;

    private String mFontPath;

    public TimeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.TimeView, 0, 0);
        try {
            TypedValue textSizeValue = new TypedValue();
            if (a.getValue(R.styleable.TimeView_textSize, textSizeValue)) {
                if (textSizeValue.type == TypedValue.TYPE_DIMENSION) {
                    mTextSize = a.getDimensionPixelSize(R.styleable.TimeView_textSize,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                }
            }

            mTimeType = TimeType.values()[a.getInt(R.styleable.TimeView_timeType, 0)];
            mTemplate = a.getString(R.styleable.TimeView_template);
            mFontPath = a.getString(R.styleable.TimeView_fontPath);

            if (getTimeStateCount() > 1) {
                if (getBackground() instanceof LevelListDrawable) {
                    // Do nothing.
                } else {
                    final int textArrayId = a.getResourceId(R.styleable.TimeView_textArray, 0);
                    if (textArrayId != 0) {
                        mTimeTextResArray = loadTextResArray(
                                getResources().getIntArray(textArrayId));
                    } else {
                        mTimeTextResArray = getDefaultTimeTextResArray();
                    }
                    if (mTimeTextResArray != null && mTimeTextResArray.length > 0) {
                        // This gives the view a default size.
                        setText(mTimeTextResArray[0]);
                    }
                }
            }

        } finally {
            a.recycle();
        }

        if (mFontPath != null) {
            try {
                setTypeface(sFontCache.getFont(context, mFontPath));
            } catch (RuntimeException ex) {
                if (isInEditMode()) {
                    System.out.println("Custom fonts are not visible in the layout editor.");
                }
            }
        }

        mFontPaint = new TextPaint(getPaint());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTextSize == ViewGroup.LayoutParams.MATCH_PARENT) {

            int width = View.MeasureSpec.getSize(widthMeasureSpec);
            int height = View.MeasureSpec.getSize(heightMeasureSpec);

            int fontSize = height;
            while(fontSize > 0) {
                mFontPaint.setTextSize(fontSize);
                float textWidth = mFontPaint.measureText(getText().toString());
                if (textWidth < width - (getPaddingLeft() + getPaddingRight())) {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, (fontSize));
                    break;
                } else {
                    fontSize--;
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private CharSequence[] loadTextResArray(int[] textResArray) {
        CharSequence[] array = new CharSequence[textResArray.length];
        for (int i=0; i<textResArray.length; i++) {
            array[i] = getResources().getText(textResArray[i]);
        }
        return array;
    }

    private CharSequence[] getDefaultTimeTextResArray() {
        switch (mTimeType) {
            case DaysOfWeek:
                return generateDefaultDaysOfWeek();
            case DaysOfMonth:
                return generateDefaultDaysInMonth();
            case Months:
                return generateDefaultMonths();
                default:
                    return generateTextArray(getTimeStateCount());
        }

    }

    private CharSequence[] generateDefaultDaysOfWeek() {
        SimpleDateFormat weekDayFormat = new SimpleDateFormat(
                mTemplate == null ? "EEE" : mTemplate);
        CharSequence[] array = new CharSequence[getTimeStateCount()];
        Date d = new Date(0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        for (int i=0; i<array.length; i++) {
            // Starts from 1970, jan 5, Monday
            cal.set(1970, 0, 5+i);
            array[i] = weekDayFormat.format(cal.getTime());
        }
        return array;
    }

    private CharSequence[] generateDefaultMonths() {
        SimpleDateFormat weekDayFormat = new SimpleDateFormat(
                mTemplate == null ? "MMM" : mTemplate);
        CharSequence[] array = new CharSequence[getTimeStateCount()];
        Date d = new Date(0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        for (int i=0; i<array.length; i++) {
            // Starts from 1970, jan 5, Monday
            cal.set(1970, i+1, 0);
            array[i] = weekDayFormat.format(cal.getTime());
        }
        return array;
    }

    private CharSequence[] generateDefaultDaysInMonth() {
        SimpleDateFormat format = new SimpleDateFormat(
                mTemplate == null ? "d" : mTemplate);
        CharSequence[] array = new CharSequence[getTimeStateCount()];
        Date d = new Date(0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        for (int i=0; i<array.length; i++) {
            // Starts from 1970, jan 5, Monday
            cal.set(1970, 0, i+1);
            array[i] = format.format(cal.getTime());
        }
        return array;
    }

    private String getDefaultDigits(int stateCount) {
        if (stateCount < 11) {
            return "0";
        } else if (stateCount < 101) {
            return "00";
        } else if (stateCount < 1001) {
            return "000";
        } else {
            return "0000";
        }
    }

    private CharSequence[] generateTextArray(int timeStateCount) {
        CharSequence[] array = new CharSequence[timeStateCount];
        String defaultDigits = getDefaultDigits(timeStateCount);
        for (int i=0; i<array.length; i++) {
            String unpadded = String.valueOf(i);
            array[i] = defaultDigits.substring(unpadded.length()) + unpadded;
        }
        return array;
    }

    /**
     * Returns the number of states in the TimeType this View has, e.g. 60 if the View has a
     * Minutes TimeType, 24 if Hours etc.
     * @return the number of states.
     */
    public int getTimeStateCount() {
        switch (mTimeType) {
            case Seconds:
            case Minutes:
            case HoursFine:
                return 60;
            case SecondsDigit1:
            case MinutesDigit1:
            case HoursDigit1:
            case MonthsDigit1:
            case YearsDigit1:
            case YearsDigit2:
            case YearsDigit3:
            case YearsDigit4:
                return 10;
            case SecondsDigit2:
            case MinutesDigit2:
                return 6;
            case Hours:
                return 24;
            case HoursDigit2:
                return 3;
            case DaysOfWeek:
                return 7;
            case DaysOfMonth:
                return 31;
            case DaysOfYear:
                return 366;
            case Months:
            case MonthsDigit2:
                return 12;
            case AmPm:
                return 2;
            default:
                return 1;
        }
    }

    /**
     * Returns the TimeType that this view has been assigned, e.g. Minutes, Hours.
     * @return the configured time type.
     */
    public TimeType getTimeType() {
        return mTimeType;
    }

    /**
     * Returns the text resources contained in this TimeView, e.g. "January, February..."
     * @return the array of text resources if any, null otherwise
     */
    public CharSequence[] getTextResArray() {
        return mTimeTextResArray;
    }
}
