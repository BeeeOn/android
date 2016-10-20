package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter;
import com.rehivetech.beeeon.gui.adapter.dashboard.DashboardModuleSelectAdapter.ModuleItem;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.UnavailableModules;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mrnda on 20/10/2016.
 */

public class ModuleSelectFragment extends BaseApplicationFragment
    implements DashboardModuleSelectAdapter.ItemClickListener{

    public static final String ARG_MODULE_TYPE_ID  = "type_id";
    public static final String ARG_GATE_ID  = "gate_id";
    public static final String ARG_REQUEST_CODE = "item_tag";

    public static ModuleSelectFragment NewInstance(String gateId, int moduleTypeId, int requestCode){
        Bundle args = new Bundle();
        args.putInt(ARG_MODULE_TYPE_ID, moduleTypeId);
        args.putString(ARG_GATE_ID, gateId);
        args.putInt(ARG_REQUEST_CODE, requestCode);

        ModuleSelectFragment instance = new ModuleSelectFragment();
        instance.setArguments(args);
        return instance;
    }


    private int mTypeId = -1;
    private String mGateId;
    private int mRequestCode;

    @BindView(R.id.fragment_module_select_rv)
    public RecyclerView mRecycler;

    private DashboardModuleSelectAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null){
            mTypeId = args.getInt(ARG_MODULE_TYPE_ID);
            mGateId = args.getString(ARG_GATE_ID);
            mRequestCode = args.getInt(ARG_REQUEST_CODE);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_module_select, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAdapter = new DashboardModuleSelectAdapter(getActivity(), this);
        mRecycler.setAdapter(mAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemViewType(position) == DashboardModuleSelectAdapter.LAYOUT_TYPE_MODULE ? 1 : 2;
            }
        });
        mRecycler.setLayoutManager(layoutManager);
        fillAdapter();
        return view;
    }


    protected void fillAdapter() {
        Controller controller = Controller.getInstance(mActivity);
        boolean withoutUnavailable = UnavailableModules.fromSettings(controller.getUserSettings());
        List<Device> devices = controller.getDevicesModel().getDevicesByGate(mGateId);

        List<Object> items = new ArrayList<>();

        for (Device device : devices) {

            List<String> groups = device.getModulesGroups(mActivity);
            List<Object> subItems = new ArrayList<>();

            if (groups.size() > 1) {

                for (String group : groups) {
                    List<Module> modules = device.getModulesByGroupName(mActivity, group, withoutUnavailable);
                    List<Object> subList = new ArrayList<>();

                    for (Module module : modules) {

                        if (mTypeId != -1 && module.getType().getTypeId() != mTypeId) {
                            continue;
                        }

                        String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
                        subList.add(new ModuleItem(moduleAbsoluteId, mGateId));
                    }

                    if (subList.size() > 0) {
                        subItems.add(new DashboardModuleSelectAdapter.HeaderItem(group, DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_GROUP));
                        subItems.addAll(subList);
                    }
                }
            } else {
                List<Module> modules = device.getVisibleModules(withoutUnavailable);

                for (Module module : modules) {

                    if (mTypeId != -1 && module.getType().getTypeId() != mTypeId) {
                        continue;
                    }

                    String moduleAbsoluteId = Utils.getAbsoluteModuleId(device.getId(), module.getId());
                    subItems.add(new ModuleItem(moduleAbsoluteId, mGateId));
                }
            }

            if (subItems.size() > 0) {
                items.add(new DashboardModuleSelectAdapter.HeaderItem(device.getName(mActivity), DashboardModuleSelectAdapter.HeaderItem.ITEM_TYPE_DEVICE_NAME));
                items.addAll(subItems);
            }
        }

        mAdapter.setItems(items);
    }

    @OnClick(R.id.fragment_module_select_done_btn)
    public void onDoneClicked(){
        IOnModuleSelectListener listener = (IOnModuleSelectListener) getActivity();
        ModuleItem selected = (ModuleItem) mAdapter.getItem(mAdapter.getFirstSelectedItem());
        if(selected != null) {
            String absoluteId = selected.getAbsoluteId();
            listener.onModuleSelected(mRequestCode,absoluteId);
        }
    }

    @Override
    public void onItemClick(String absoluteModuleId) {
    }

    public interface IOnModuleSelectListener {
        void onModuleSelected(int requestCode, String absoluteModuleId);
    }
}
