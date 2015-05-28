package com.rehivetech.beeeon;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rehivetech.beeeon.activity.fragment.AddAdapterFragment;
import com.rehivetech.beeeon.activity.fragment.IntroImageFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class AddAdapterFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
	protected static final String[] CONTENT = new String[]{"Welcome", "to", "BeeeOn", "Test",};
	protected static final int[] ICONS = new int[]{
			R.drawable.loc_bath_room,
			R.drawable.loc_garden,
			R.drawable.loc_wc,
			R.drawable.loc_dinner_room
	};
	private final Context mActivity;

	private int mCount = 4;

	public AddAdapterFragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
		mActivity = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_aa_second_step, mActivity.getString(R.string.tut_add_adapter_text_1));
			case 1:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_aa_first_step, mActivity.getString(R.string.tut_add_adapter_text_2));
			case 2:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_aa_third_step, mActivity.getString(R.string.tut_add_adapter_text_3));
			case 3:
				return new AddAdapterFragment();
		}
		return null;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return AddAdapterFragmentAdapter.CONTENT[position % CONTENT.length];
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