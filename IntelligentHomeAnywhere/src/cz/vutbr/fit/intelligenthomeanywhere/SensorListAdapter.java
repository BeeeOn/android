package cz.vutbr.fit.intelligenthomeanywhere;

import android.content.Context;
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
    double[] mValue;
    int[] mIcon;
    LayoutInflater inflater;
 
    public SensorListAdapter(Context context, String[] title, double[] value,  int[] icon) {
        this.context = context;
        this.mTitle = title;
        this.mValue = value;
        this.mIcon = icon;
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
        ImageView imgIcon;
 
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.sensor_listview_item, parent,false);
 
        // Locate the TextViews in drawer_list_item.xml
        txtTitle = (TextView) itemView.findViewById(R.id.titleofsensor);
        txtValue = (TextView) itemView.findViewById(R.id.valueofsensor);
 
        // Locate the ImageView in drawer_list_item.xml
        imgIcon = (ImageView) itemView.findViewById(R.id.iconofsensor);
 
        // Set the results into TextViews
        txtTitle.setText(mTitle[position]);
        txtValue.setText(Double.toString(mValue[position]));
 
        // Set the results into ImageView
        imgIcon.setImageResource(mIcon[position]);
 
        return itemView;
	}

}
