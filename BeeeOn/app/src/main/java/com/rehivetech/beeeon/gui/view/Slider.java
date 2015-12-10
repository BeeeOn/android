package com.rehivetech.beeeon.gui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

import java.util.List;

/**
 * Created by martin on 22.11.15.
 */
public class Slider extends LinearLayout implements SeekBar.OnSeekBarChangeListener{

	private Context mContext;
	private TextView mValue;
	private AppCompatSeekBar mSeekbar;
	private List<String> mValues;

	public Slider(Context context) {
		super(context);
		init(context);
	}

	public Slider(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public Slider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}


	private void init(Context context) {
		mContext = context;
		View view = LayoutInflater.from(context).inflate(R.layout.slider, this, true);

		setOrientation(VERTICAL);
		mValue = (TextView) view.findViewById(R.id.slider_textview);
		mSeekbar = (AppCompatSeekBar) view.findViewById(R.id.slider_seekbar);
		mSeekbar.setOnSeekBarChangeListener(this);

	}

	public void setValues(List<String> values) {
		mSeekbar.setMax(values.size() - 1);
		mValues = values;

		ProgressDrawable progressDrawable = new ProgressDrawable(mSeekbar.getThumbOffset(), ContextCompat.getColor(mContext, R.color.beeeon_accent), mValues.size());
		mSeekbar.setProgressDrawable(progressDrawable);

		mValue.setText(mValues.get(mSeekbar.getProgress()));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (mValues != null) {
			mValue.setText(mValues.get(progress));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	public int getProgress() {
		return mSeekbar.getProgress();
	}

	public void setProgress(int progress) {
		mSeekbar.setProgress(progress);
	}

	private class ProgressDrawable extends Drawable {

		private Paint mPaint;
		private int mOffset;
		private int mSteps;


		public ProgressDrawable(int offset, @ColorInt int color, int steps) {
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setColor(color);
			mPaint.setTextAlign(Paint.Align.CENTER);
			mPaint.setStrokeWidth(4f);

			mOffset = offset;
			mSteps = steps;
		}


		@Override
		public void draw(Canvas canvas) {
			Rect b = getBounds();

			int height = getBottom() - getTop();
			int left = b.left;

			int right = b.right;

			int y = height / 2 - mOffset;

			canvas.drawLine(left, y, right + 10, y, mPaint);

			int step = (right + mSteps) / (mSteps - 1);
			int current = 0;

			for (int i = 0; i < mSteps; i++) {
				canvas.drawCircle(current, height/2 - mOffset, 5f, mPaint);
				current += step;
			}
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter colorFilter) {
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public int getIntrinsicHeight() {
			return -1;
		}
	}
}
