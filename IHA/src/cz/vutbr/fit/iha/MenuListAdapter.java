package cz.vutbr.fit.iha;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Class for location left drawer menu
 * @author ThinkDeep
 *
 */
public class MenuListAdapter extends BaseAdapter {

	// Declare Variables
	Context context;
	
	private ArrayList<String> mTitle;
	private ArrayList<String> mSubTitle;
	private ArrayList<Integer> mIcon;
	
	private LayoutInflater mInflater;
	
	private ArrayList<String> mHeaders;
	private ArrayList<Integer> mLengths;
	private int mSectionStartPosition = -1;
	private int mSSP;
	private int mHeaderPosition = 0;

	/**
	 * Constructor
	 * @param context
	 */
	public MenuListAdapter(Context context){
		this.context = context;
		mTitle = new ArrayList<String>();
		mSubTitle = new ArrayList<String>();
		mIcon = new ArrayList<Integer>();
		mHeaders = new ArrayList<String>();
		mLengths = new ArrayList<Integer>();
	}
	
	/**
	 * Constructor
	 * @param context
	 * @param title
	 * @param subtitle
	 * @param icon
	 */
	public MenuListAdapter(Context context, ArrayList<String> title, ArrayList<String> subtitle, ArrayList<Integer> icon) {
		this.context = context;
		this.mTitle = title;
		this.mSubTitle = subtitle;
		this.mIcon = icon;
		this.mHeaders = new ArrayList<String>();
		this.mLengths = new ArrayList<Integer>();
	}

	@Override
	public int getCount() {
		return mTitle.size() + mHeaders.size();
	}

	@Override
	public Object getItem(int position) {
		return mTitle.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addSection(String section, ArrayList<String> title, ArrayList<String> subtitle, ArrayList<Integer> icon){
		mHeaders.add(section);
		mLengths.add(title.size());
		if(mSectionStartPosition < 0){
			mSectionStartPosition = mTitle.size();
			mSSP = mTitle.size();
		}
		
		mTitle.addAll(title);
		mSubTitle.addAll(subtitle);
		mIcon.addAll(icon);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// Declare Variables
		TextView txtTitle;
		TextView txtSubTitle;
		ImageView imgIcon;
		TextView header;
		
		View resultView;

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(mSectionStartPosition != position){
			resultView = mInflater.inflate(R.layout.drawer_listview_item, parent, false);
			
			// Locate the TextViews in drawer_list_item.xml
			txtTitle = (TextView) resultView.findViewById(R.id.title);
			txtSubTitle = (TextView) resultView.findViewById(R.id.subtitle);

			// Locate the ImageView in drawer_list_item.xml
			imgIcon = (ImageView) resultView.findViewById(R.id.icon);

			// Set the results into TextViews
			txtTitle.setText(mTitle.get(position-mHeaderPosition));
			txtSubTitle.setText(mSubTitle.get(position-mHeaderPosition));

			// Set the results into ImageView
			imgIcon.setImageResource(mIcon.get(position-mHeaderPosition));
			
		}else{
			resultView = mInflater.inflate(R.layout.drawer_listview_header, parent, false);
			
			if(mHeaderPosition < mHeaders.size()){
				header = (TextView)resultView.findViewById(R.id.section);
				header.setText(mHeaders.get(mHeaderPosition));
				
				mSectionStartPosition += mLengths.get(mHeaderPosition) + 1;

				mHeaderPosition++;
			}
		}
		if(position == getCount()-1){
			mHeaderPosition = 0;
			mSectionStartPosition = mSSP;
		}
		
		return resultView;
	}
	
	public TYPE getTypeByPosition(int position){
		if(position == 0)
			return TYPE.USER;
		if(mSectionStartPosition > 0){
			if(position == mSectionStartPosition)
				return TYPE.HEADER;
			if(position > mSectionStartPosition && position <= (1 + mLengths.get(0)))
				return TYPE.LOCATION;
			if(position == (1 + 1 + mLengths.get(0)))
				return TYPE.HEADER;
			else
				return TYPE.VIEW;
		}
		
		return TYPE.HEADER;
	}
	
	public int getTypePosition(int position, TYPE type){
		if(type == TYPE.LOCATION){
			return position - 2;
		}
		if(type == TYPE.VIEW){
			return position - 3 - mLengths.get(0);
		}
		return 0;
	}
	
	public enum TYPE{
		USER, HEADER, LOCATION, VIEW
	}

}
