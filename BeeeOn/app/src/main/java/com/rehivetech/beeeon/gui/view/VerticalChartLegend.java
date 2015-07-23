package com.rehivetech.beeeon.gui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author martin on 12.7.2015.
 */
public class VerticalChartLegend<T extends DataSet<? extends Entry>> extends View {

	@IntDef({LEGEND_SHAPE_CIRCLE, LEGEND_SHAPE_SQUARE})
	public @interface LegendShape {
	}

	public static final int LEGEND_SHAPE_CIRCLE = 1;
	public static final int LEGEND_SHAPE_SQUARE = 2;


	Context mContext;
	Paint mPaint;
	List<T> mChartDatasets;
	float mShapeSize;
	float mEntrySpace;
	int mTextResId;
	int mTextColor;
	@LegendShape
	int mLegendShape;

	public VerticalChartLegend(Context context) {
		super(context);
		init(context);
	}

	public VerticalChartLegend(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VerticalChartLegend(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		mTextResId = R.style.TextAppearance_AppCompat_Body1;

		//create textView for styling paint
		TextView text = new TextView(context);
		text.setTextAppearance(context, mTextResId);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(text.getTextSize());
		mPaint.setTypeface(text.getTypeface());

		mTextColor = text.getCurrentTextColor();

		mShapeSize = text.getTextSize();

		mLegendShape = LEGEND_SHAPE_CIRCLE;
		mChartDatasets = new ArrayList<>();

		mEntrySpace = Utils.convertDpToPixel(5f);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isInEditMode()) {
			mPaint.setColor(Color.GRAY);
			canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
		} else {
			Iterator<T> datasetIterator = mChartDatasets.iterator();
			int i = 0;
			T entry;
			while (datasetIterator.hasNext()) {
				entry = datasetIterator.next();
				mPaint.setColor(entry.getColor());
				if (mLegendShape == LEGEND_SHAPE_SQUARE) {
					canvas.drawRect(0, i, mShapeSize, i + mShapeSize, mPaint);
				} else {
					canvas.drawCircle(0 + mShapeSize / 2, i + mShapeSize, mShapeSize / 2, mPaint);
				}
				mPaint.setColor(mTextColor);
				canvas.drawText(entry.getLabel(), mShapeSize + mEntrySpace, i + mShapeSize + mPaint.getTextSize() / 2, mPaint);
				i += mShapeSize + mEntrySpace;
			}
		}
	}

	/**
	 * Set legend shape
	 *
	 * @param legendShape
	 */
	public void setLegendShape(@LegendShape int legendShape) {
		mLegendShape = legendShape;
	}


	/**
	 * Sets datasets to get labels and colors
	 * @param dataSet chart dataset
	 */
	public void setChartDatasets(List<T> dataSet) {
		mChartDatasets = dataSet;
		setMinimumHeight((int) (dataSet.size() * (mShapeSize + mEntrySpace * 2)));
	}

	/**
	 * Set label text appearance
	 * @param resId text appearance rescource Id
	 */
	public void setTextAppearance(@StyleRes int resId) {
		mTextResId = resId;
		init(mContext);
	}
}
