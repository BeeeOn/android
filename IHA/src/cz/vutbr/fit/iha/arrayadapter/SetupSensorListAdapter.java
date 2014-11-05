package cz.vutbr.fit.iha.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.Facility;

public class SetupSensorListAdapter extends BaseAdapter {

	private Context mContext;
	private Facility mFacility;
	private EditText mName;

	private LayoutInflater mInflater;

	public SetupSensorListAdapter(Context context, Facility facility) {
		mContext = context;
		mFacility = facility;

	}

	@Override
	public int getCount() {
		return mFacility.getDevices().size();
	}

	@Override
	public String getItem(int position) {
		return mName.getText().toString();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Create basic View
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = mInflater.inflate(R.layout.setup_sensor_listview_item, parent, false);
		// Get GUI elements
		ImageView img = (ImageView) itemView.findViewById(R.id.setup_sensor_item_icon);
		mName = (EditText) itemView.findViewById(R.id.setup_sensor_item_name);
		// Set image resource by sensor type
		img.setImageResource(mFacility.getDevices().get(position).getIconResource());

		// TODO Auto-generated method stub
		return itemView;
	}

}
