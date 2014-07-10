package cz.vutbr.fit.iha;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SensorListAdapter extends BaseAdapter {
	
	// Declare Variables
    Context context;
    String[] mTitle;
    String[] mValue;
    String[] mUnit;
    Time[] mTime;
    int[] mIcon;
    LayoutInflater inflater;
 
    public SensorListAdapter(Context context, String[] title, String[] value,String[] unit, Time[] time , int[] icon) {
        this.context = context;
        this.mTitle = title;
        this.mValue = value;
        this.mIcon = icon;
        this.mUnit = unit;
        this.mTime = time;
    }

	@Override
	public int getCount() {
		return this.mTitle.length;
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
		// Declare Variables
        TextView txtTitle;
        TextView txtValue;
        TextView txtUnit;
        TextView txtTime;
        ImageView imgIcon;
 
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.sensor_listview_item, parent,false);
 
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
        txtTime.setText(context.getString(R.string.last_update)+" "+setLastUpdate(mTime[position]));
 
        // Set the results into ImageView
        imgIcon.setImageResource(mIcon[position]);
 
        return itemView;
	}
	
	private CharSequence setLastUpdate(Time lastUpdate) {
		// Last update time data
		Time yesterday = new Time();
		yesterday.setToNow();
		yesterday.set(yesterday.toMillis(true) - 24 * 60 * 60 * 1000); // -24
																		// hours

		// If sync time is more that 24 ago, show only date. Show time
		// otherwise.
		DateFormat dateFormat = yesterday.before(lastUpdate) ? DateFormat
				.getTimeInstance() : DateFormat.getDateInstance();
		
		Date lastUpdateDate = new Date(lastUpdate.toMillis(true));
		return dateFormat.format(lastUpdateDate);
	}

}
