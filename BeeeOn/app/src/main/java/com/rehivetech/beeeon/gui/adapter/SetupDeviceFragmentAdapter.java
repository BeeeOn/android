package com.rehivetech.beeeon.gui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.SetupDeviceFragment;
import com.rehivetech.beeeon.gui.fragment.UnPairedDeviceListFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class SetupDeviceFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
	protected static final String[] CONTENT = new String[]{"Welcome", "to", "BeeeOn", "Test",};
	protected static final int[] ICONS = new int[]{
			R.drawable.ic_loc_bathroom_gray,
			R.drawable.ic_loc_garden_gray,
			R.drawable.ic_loc_wc_gray,
			R.drawable.ic_loc_dining_room_gray
	};

	private int mCount = 2;
	private String mGateId;
	private int mDeviceIndex;

	public SetupDeviceFragmentAdapter(FragmentManager fm, String gateId) {
		super(fm);
		mGateId = gateId;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return UnPairedDeviceListFragment.newInstance(mGateId);
			case 1:
				return SetupDeviceFragment.newInstance(mDeviceIndex);
		}
		return null;
	}

	public void showNext(int deviceIndex) {
		mDeviceIndex = deviceIndex;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return SetupDeviceFragmentAdapter.CONTENT[position % CONTENT.length];
	}

	@Override
	public int getIconResId(int index) {
		return ICONS[index % ICONS.length];
	}

	public void setCount(int count) {
		if (count > 0 && count <= 10) {
			mCount = count;
			notifyDataSetChanged();
		}
	}
}