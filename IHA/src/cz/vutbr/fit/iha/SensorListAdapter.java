package cz.vutbr.fit.iha;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.settings.Timezone;

public class SensorListAdapter extends BaseAdapter {

	// Declare Variables
	private Context mContext;
	private String[] mTitle;
	private String[] mValue;
	private String[] mUnit;
	private Time[] mTime;
	private int[] mIcon;
	private LayoutInflater inflater;
	// private int mCount;
	private int mLenght;
	
	private final Controller mController;

	public SensorListAdapter(Context context, String[] title, String[] value,
			String[] unit, Time[] time, int[] icon) {
		mContext = context;
		mTitle = title;
		mValue = value;
		mIcon = icon;
		mUnit = unit;
		mTime = time;
		mLenght = mTitle.length;
		mController = Controller.getInstance(context);
		// mCount = mTitle.length;
	}

	@Override
	public int getCount() {
		return mLenght;
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
		// if(position < mLenght)
		// {
		return addItem(position, convertView, parent);
		// }
		// return addAddSensor(convertView,parent);
	}

	/*
	 * private View addAddSensor(View convertView, ViewGroup parent) { inflater
	 * = (LayoutInflater)
	 * mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); View itemView
	 * = inflater.inflate(R.layout.sensor_listview_addsensor, parent,false);
	 * return itemView; }
	 */

	private View addItem(int position, View convertView, ViewGroup parent) {
		// Declare Variables
		TextView txtTitle;
		TextView txtValue;
		TextView txtUnit;
		TextView txtTime;
		ImageView imgIcon;

		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.sensor_listview_item, parent,
				false);

		// Locate the TextViews in drawer_list_item.xml
		txtTitle = (TextView) itemView.findViewById(R.id.titleofsensor);
		txtValue = (TextView) itemView.findViewById(R.id.valueofsensor);
		txtUnit = (TextView) itemView.findViewById(R.id.unitofsensor);
		txtTime = (TextView) itemView.findViewById(R.id.timeofsensor);

		// Locate the ImageView in drawer_list_item.xml
		imgIcon = (ImageView) itemView.findViewById(R.id.iconofsensor);

		// Set the results into TextViews
		txtTitle.setText(mTitle[position]);
		txtValue.setText(mValue[position]);
		txtUnit.setText(mUnit[position]);
		txtTime.setText(String.format(
				"%s %s",
				mContext.getString(R.string.last_update),
				Timezone.getSharedPreferenceOption(mController.getUserSettings()).formatLastUpdate(
						mTime[position])));

		// Set the results into ImageView
		imgIcon.setImageResource(mIcon[position]);

		return itemView;
	}

}
