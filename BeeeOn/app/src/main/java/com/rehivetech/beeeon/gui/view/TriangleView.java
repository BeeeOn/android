package com.rehivetech.beeeon.gui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.rehivetech.beeeon.R;

/**
 * Created by martin on 18.10.15.
 */
public class TriangleView extends View {

	private Paint mPaint;
	private Path mPath;

	public TriangleView(Context context) {
		super(context);
	}

	public TriangleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public TriangleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TriangleView);
		int color = typedArray.getColor(0, 0);
		typedArray.recycle();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(color);

		mPath = new Path();
		mPath.incReserve(3);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();

		mPath.moveTo(0, 0);
		mPath.lineTo(width, 0);
		mPath.lineTo(width / 2, height);
		mPath.close();

		canvas.drawPath(mPath, mPaint);
		mPath.rewind();
	}
}
