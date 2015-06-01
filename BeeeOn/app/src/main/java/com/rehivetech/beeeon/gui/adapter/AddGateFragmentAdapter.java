package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.gui.fragment.IntroImageFragment;
import com.viewpagerindicator.IconPagerAdapter;

public class AddGateFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
	protected static final int[] ICONS = new int[]{
			R.drawable.loc_bath_room,
			R.drawable.loc_garden,
			R.drawable.loc_wc,
			R.drawable.loc_dinner_room
	};
	private final Context mContext;

	private static final int PAGES_COUNT = 4;

	private AddGateFragment mAddGateFragment;

	public AddGateFragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_aa_second_step, mContext.getString(R.string.tut_add_gate_text_1));
			case 1:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_aa_first_step, mContext.getString(R.string.tut_add_gate_text_2));
			case 2:
				return IntroImageFragment.newInstance(R.drawable.beeeon_tutorial_aa_third_step, mContext.getString(R.string.tut_add_gate_text_3));
			case 3:
				return new AddGateFragment();
		}
		return null;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);
		if (position == PAGES_COUNT - 1) {
			mAddGateFragment = (AddGateFragment) fragment;
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return PAGES_COUNT;
	}

	@Override
	public int getIconResId(int index) {
		return ICONS[index % ICONS.length];
	}

	public AddGateFragment getAddGateFragment() {
		return mAddGateFragment;
	}
}