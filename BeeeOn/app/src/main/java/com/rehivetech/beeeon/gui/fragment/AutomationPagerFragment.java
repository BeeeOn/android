package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.AddAutomationRuleActivity;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.ViewPagerAdapter;
import com.rehivetech.beeeon.gui.adapter.automation.items.BaseItem;
import com.rehivetech.beeeon.gui.view.FloatingActionMenu;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;

/**
 * Created by xmrnus01 on 10/14/2016.
 */

public class AutomationPagerFragment extends BaseApplicationFragment
{
    private static final String KEY_GATE_ID = "gateId";

    public static final int SUMMER_RULES_INDEX = 0;
    public static final int WINTER_RULES_INDEX = 1;

    private static final int REQ_CODE_ADD_AUTOMATION_ITEM = 0;
    public static final String EXTRA_ADD_ITEM = "item";
    public static final String EXTRA_INDEX = "index";

    private String mGateId;


    @BindView(R.id.automation_pager_root_layout)
    CoordinatorLayout mRootView;
    @BindView(R.id.automation_pager_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.automation_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.automation_fab_menu)
    FloatingActionMenu mFloatingActionMenu;

    ViewPagerAdapter mViewsAdapter;
    @State
    int mSelectedViewIndex = 0;

    public static AutomationPagerFragment NewInstance(String gateId) {
        Bundle args = new Bundle();
        args.putString(KEY_GATE_ID, gateId);

        AutomationPagerFragment instance = new AutomationPagerFragment();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            mGateId = getArguments().getString(KEY_GATE_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Toolbar toolbar = mActivity.setupToolbar(R.string.nav_drawer_menu_menu_automation, BaseApplicationActivity.INDICATOR_MENU);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_automation, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mViewsAdapter = new ViewPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mViewsAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mFloatingActionMenu.setClosedOnTouchOutside(true);


        setupViewPager();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_CODE_ADD_AUTOMATION_ITEM){
            if(resultCode == Activity.RESULT_OK){
                BaseItem item = data.getParcelableExtra(EXTRA_ADD_ITEM);
                int index = data.getIntExtra(EXTRA_INDEX, 0);
                Fragment fragment = mViewsAdapter.getItem(index);
                ((AutomationFragment) fragment).addItem(item);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        mViewPager.setCurrentItem(mSelectedViewIndex);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mSelectedViewIndex = mViewPager.getCurrentItem();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.AUTOMATION_SCREEN);
    }

    @OnClick(R.id.automation_add_rule_fab)
    public void onAddAutomationRuleClicked() {
        Intent intent = AddAutomationRuleActivity.getAddAutomationRuleActivityIntent(getActivity(),
                mViewPager.getCurrentItem(),
                mGateId);
        mFloatingActionMenu.close(true);
        startActivityForResult(intent, REQ_CODE_ADD_AUTOMATION_ITEM);
    }

    private void setupViewPager() {
        mViewsAdapter.addFragment(
                AutomationFragment.NewInstance(SUMMER_RULES_INDEX,mGateId),
                getString(R.string.automation_tab_summer_rules)
        );
        mViewsAdapter.addFragment(
                AutomationFragment.NewInstance(WINTER_RULES_INDEX, mGateId),
                getString(R.string.automation_tab_winter_rules)
        );
    }
}
