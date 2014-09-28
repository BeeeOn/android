package cz.vutbr.fit.iha;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.util.Timezone;

public class SensorListAdapter extends BaseAdapter {

	private static final int MARGIN_LEFT_RIGHT = 2;
	private static final int MARGIN_TOP = 10;
	private static final int MARGIN_BOTTOM = 0;
	private static final int MARGIN_TOP_M_L = -2;
	private static final int PADDING_LEFT_RIGHT = 5;
	private static final int PADDING_TOP = 6;
	private static final int PADDING_BOTTOM = 5;
	private float mScale;

	// Declare Variables
	private Context mContext;
	private String[] mAdapterId;
	private String[] mTitle;
	private String[] mValue;
	private String[] mUnit;
	private Time[] mTime;
	private int[] mIcon;
	private int[] mRelPos;
	private int[] mFacSize;
	private LayoutInflater inflater;
	private int mLength;
	private boolean mShowAdd;

	private final Controller mController;

	public SensorListAdapter(Context context, String[] adapterId, String[] title, String[] value, String[] unit, Time[] time, int[] icon, int[] relPos, int[] facSize, boolean showAdd) {
		mContext = context;
		mAdapterId = adapterId;
		mTitle = title;
		mValue = value;
		mIcon = icon;
		mUnit = unit;
		mTime = time;
		mRelPos = relPos;
		mFacSize = facSize;
		mLength = mTitle.length;
		mController = Controller.getInstance(context);
		mShowAdd = showAdd;
	}

	@Override
	public int getCount() {
		return mShowAdd ? mLength + 1 : mLength;
	}

	@Override
	public Object getItem(int position) {
		return mTitle[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < mLength) {
			return addItem(position, convertView, parent);
		}
		return addAddSensor(convertView, parent);
	}

	private View addAddSensor(View convertView, ViewGroup parent) {
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.sensor_listview_addsensor, parent, false);
		return itemView;
	}

	private View addItem(int position, View convertView, ViewGroup parent) {
		// Declare Variables
		TextView txtTitle;
		TextView txtValue;
		TextView txtUnit;
		TextView txtTime;
		ImageView imgIcon;
		LinearLayout layout;

		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.sensor_listview_item, parent, false);

		// Locate the TextViews in drawer_list_item.xml
		txtTitle = (TextView) itemView.findViewById(R.id.titleofsensor);
		txtValue = (TextView) itemView.findViewById(R.id.valueofsensor);
		txtUnit = (TextView) itemView.findViewById(R.id.unitofsensor);
		txtTime = (TextView) itemView.findViewById(R.id.timeofsensor);

		// Locate the ImageView in drawer_list_item.xml
		imgIcon = (ImageView) itemView.findViewById(R.id.iconofsensor);

		Adapter adapter = mController.getAdapter(mAdapterId[position]);
		
		// Set the results into TextViews
		txtTitle.setText(mTitle[position]);
		txtValue.setText(mValue[position]);
		txtUnit.setText(mUnit[position]);
		txtTime.setText(String.format("%s %s", mContext.getString(R.string.last_update), Timezone.getSharedPreferenceOption(mController.getUserSettings()).formatLastUpdate(mTime[position], adapter)));

		// Set the results into ImageView
		imgIcon.setImageResource(mIcon[position]);
		mScale = parent.getResources().getDisplayMetrics().density;

		// Set layout with right background
		layout = (LinearLayout) itemView.findViewById(R.id.layoutofsensor);
		if (mFacSize[position] == 1) {// it is SOLO device from FACILITY
			layout.setBackgroundResource(R.drawable.iha_item_solo_bg);
			((LayoutParams) layout.getLayoutParams()).setMargins((int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_TOP, (int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_BOTTOM);
		} else if (mRelPos[position] == 1) { // FIRST from FACILITY
			layout.setBackgroundResource(R.drawable.iha_item_first_bg);
			((LayoutParams) layout.getLayoutParams()).setMargins((int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_TOP, (int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_BOTTOM);
		} else if (mRelPos[position] == mFacSize[position]) {// LAST from FACILITY
			layout.setBackgroundResource(R.drawable.iha_item_last_bg);
			((LayoutParams) layout.getLayoutParams()).setMargins((int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_TOP_M_L, (int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_BOTTOM);
			layout.setPadding((int) mScale * PADDING_LEFT_RIGHT, (int) mScale * PADDING_TOP, (int) mScale * PADDING_LEFT_RIGHT, (int) mScale * PADDING_BOTTOM);
		} else { // MIDLE from FACILITY
			layout.setBackgroundResource(R.drawable.iha_item_midle_bg);
			((LayoutParams) layout.getLayoutParams()).setMargins((int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_TOP_M_L, (int) mScale * MARGIN_LEFT_RIGHT, (int) mScale * MARGIN_BOTTOM);
		}

		return itemView;
	}

}
