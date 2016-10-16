package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.automation.AutomationAdapter;
import com.rehivetech.beeeon.gui.adapter.automation.items.DewingItem;
import com.rehivetech.beeeon.gui.adapter.automation.items.VentilationItem;
import com.rehivetech.beeeon.gui.adapter.automation.items.BaseItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;

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

    }

    private void createDemoData()
    {
        BaseItem item = null;
        Controller controller = Controller.getInstance(getActivity());
        List devices = controller.getDevicesModel().getDevicesByGate(mGateId);
        Module insideTemp = null, outsideTemp = null, humid = null;
        for(Object object : devices){
            if(object instanceof Device){
                Device device = (Device)object;
                Module module = device.getFirstModuleByType(ModuleType.TYPE_TEMPERATURE);
                Module humidM = device.getFirstModuleByType(ModuleType.TYPE_HUMIDITY);
                if(module != null){
                    if(insideTemp == null){
                        insideTemp = module;
                    } else {
                        outsideTemp = module;
                    }


                }
                if(humidM != null){
                    humid = module;
                }

                if(insideTemp != null && outsideTemp != null && humid != null){
                    item = new DewingItem("Living room dewing",
                            mGateId,
                            true,
                            insideTemp.getModuleId().absoluteId,
                            outsideTemp.getModuleId().absoluteId,
                            humid.getModuleId().absoluteId);
                    mAdapter.addItem(item);
                }

                if(insideTemp != null && outsideTemp != null){
                    item = new VentilationItem("Living room windows",
                            mGateId,
                            true,
                            null,
                            outsideTemp.getModuleId().absoluteId,
                            insideTemp.getModuleId().absoluteId);
                    insideTemp = null;
                    outsideTemp = null;
                    mAdapter.addItem(item);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_automation, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAdapter = new AutomationAdapter((BaseApplicationActivity) getActivity(), this, this);
        mAdapter.setEmptyView(mEmptyView);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(mGridSpanCount, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        if(Controller.getInstance(getActivity()).isDemoMode())
            createDemoData();

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
                final Map<Integer, BaseItem> tempSelectedItems = new TreeMap<>();
                for (Object itemPosition : selectedItems) {
                    Integer position = (Integer) itemPosition;
                    tempSelectedItems.put(position, mAdapter.getItem(position));
                }

                for (Map.Entry<Integer, BaseItem> entry : tempSelectedItems.entrySet()) {
                    mAdapter.deleteItem(entry.getValue());
                }

                AutomationPagerFragment fragment = (AutomationPagerFragment) getParentFragment();

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (Map.Entry<Integer, BaseItem> entry : tempSelectedItems.entrySet()) {
                            mAdapter.addItem(entry.getKey(), entry.getValue());
                        }
                    }
                };

                Snackbar.make(fragment.mRootView, getResources().getQuantityString(R.plurals.dashboard_delete_snackbar, selectedItems.size()), Snackbar.LENGTH_LONG)
                        .setAction(R.string.dashboard_undo, listener)
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

}
