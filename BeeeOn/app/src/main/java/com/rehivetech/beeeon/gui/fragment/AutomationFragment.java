package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.automation.AutomationAdapter;
import com.rehivetech.beeeon.model.entity.automation.AutomationItem;
import com.rehivetech.beeeon.model.entity.automation.DewingItem;
import com.rehivetech.beeeon.model.entity.automation.VentilationItem;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 *  @author xmrnus01
 *
 */

public class AutomationFragment extends BaseApplicationFragment implements RecyclerViewSelectableAdapter.IItemClickListener, AutomationAdapter.ActionModeCallback {

    private static final String KEY_GATE_ID = "gateId";
    private static final String KEY_PAGE_INDEX = "pageIndex";

    private String mGateId;
    private int mPageIndex;
    private ActionMode mActionMode;

    @BindInt(R.integer.dashboard_span_count)
    public int mGridSpanCount;
    @BindView(R.id.automation_recycler_view)
    public RecyclerView mRecyclerView;
    @BindView(R.id.automation_empty_view)
    public TextView mEmptyView;

    private AutomationAdapter mAdapter;
    private Realm mRealm;

    /**
     * Fragment instance creation.
     *
     * @param gateId id of gate which rules relate to.
     * @return fragment instance for selected gateId.
     */
    public static AutomationFragment NewInstance(int pageIndex, String gateId)
    {
        Bundle args = new Bundle();
        args.putString(KEY_GATE_ID, gateId);
        args.putInt(KEY_PAGE_INDEX, pageIndex);
        AutomationFragment instance = new AutomationFragment();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if(args != null) {
            mGateId = args.getString(KEY_GATE_ID);
            mPageIndex = args.getInt(KEY_PAGE_INDEX);
        }

        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_automation, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        mAdapter = new AutomationAdapter(getContext(), this, this);
        mAdapter.setEmptyView(mEmptyView);

        RealmResults<AutomationItem> realmResults = mRealm.where(AutomationItem.class)
                .equalTo("automationType", mPageIndex)
                .equalTo("gateId", mGateId)
                .findAll();

        if (realmResults.isEmpty()) {
            mRealm.executeTransaction(new AutomationDemoDatabaseSeed());
        }

        mAdapter.setItems(realmResults);

        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(mGridSpanCount, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onRecyclerViewItemClick(int position, int viewType) {
        return;
    }

    @Override
    public boolean onRecyclerViewItemLongClick(int position, int viewType) {
        if(mActionMode == null){
            mActionMode = mActivity.startSupportActionMode(new ActionModeAutomation());
        }
        mAdapter.clearSelection();
        mAdapter.toggleSelection(position);
        return true;
    }

    @Override
    public void finishActionMode() {
        if(mActionMode != null){
            mActionMode.finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
    }

    public class ActionModeAutomation implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.actionmode_delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.action_delete){
                final List selectedItems = mAdapter.getSelectedItems();

                if(mActionMode != null){
                    mActionMode.finish();
                }

                final Map<Integer, AutomationItem> tempSelectedItems = new TreeMap<>();

                for (Object itemPosition : selectedItems) {
                    Integer position = (Integer) itemPosition;
                    tempSelectedItems.put(position, mAdapter.getItem(position));
                }

                //return if realm is in transaction
                if (mRealm.isInTransaction()) {
                    return false;
                }

                //begin realm transaction for item removing
                mRealm.beginTransaction();
                for (Map.Entry<Integer, AutomationItem> entry : tempSelectedItems.entrySet()) {
                    mAdapter.deleteItem(entry.getValue());
                }

                AutomationPagerFragment fragment = (AutomationPagerFragment) getParentFragment();

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //rollback realm transaction if user cancel it
                        mRealm.cancelTransaction();
                        RealmResults<AutomationItem> results = mRealm.where(AutomationItem.class).equalTo("gateId", mGateId).equalTo("automationType", mPageIndex).findAll();
                        mAdapter.setItems(results);
                    }
                };

                Snackbar.make(fragment.mRootView, getResources().getQuantityString(R.plurals.dashboard_delete_snackbar, selectedItems.size()), Snackbar.LENGTH_LONG)
                        .setAction(R.string.dashboard_undo, listener)
                        .setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                                // if user wont cancel deleting, than commit changes
                                if (mRealm.isInTransaction()) {
                                    mRealm.commitTransaction();
                                }
                            }
                        })
                        .show();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
            mActionMode = null;
        }
    }


	/**
     * Demo data for automation use cases
     */
    public class AutomationDemoDatabaseSeed implements Realm.Transaction {

        @Override
        public void execute(Realm realm) {
            AutomationItem item1 = new AutomationItem();
            item1.setGateId("64206");
            item1.setAutomationType(0); // summer type

            DewingItem dewingItem = new DewingItem();
            dewingItem.setName(BeeeOnApplication.getContext().getString(R.string.demo_automation_living_room_dewing));
            dewingItem.setActive(true);
            dewingItem.setInsideTempAbsoluteModuleId("1001---0");
            dewingItem.setOutsideTempAbsoluteModueId("1003---1");
            dewingItem.setHumidityAbsoluteModuleId("1003---2");

            item1.setDewingItem(dewingItem);

            AutomationItem item2 = new AutomationItem();
            item2.setAutomationType(0); // summer type
            item2.setGateId("64206");

            VentilationItem ventilationItem = new VentilationItem();
            ventilationItem.setName(BeeeOnApplication.getContext().getString(R.string.demo_automation_living_room_windows));
            ventilationItem.setInSideAbsoluteModuleId("1003---0");
            ventilationItem.setOutsideAbsoluteModuleId("1003---1");
            ventilationItem.setActive(true);

            item2.setVentilationItem(ventilationItem);

            realm.copyToRealm(item1);
            realm.copyToRealm(item2);
        }
    }
}
