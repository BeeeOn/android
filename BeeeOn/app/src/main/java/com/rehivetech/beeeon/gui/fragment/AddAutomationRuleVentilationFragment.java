package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.automation.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.automation.items.VentilationItem;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rehivetech.beeeon.gui.fragment.ModuleSelectFragment.*;

/**
 * Created by Michal Mrnustik on 19/10/2016.
 */

public class AddAutomationRuleVentilationFragment extends BaseApplicationFragment
        implements AddAutomationRuleFragment.RuleSaveClickedListener, IOnModuleSelectListener{

    private static final String ARG_GATE_ID = "gate_id";
    private static final String ARG_INDEX = "index";

    private static final int REQUEST_CODE_INSIDE = 2048;
    private static final int REQUEST_CODE_OUTSIDE = 2049;

    public static AddAutomationRuleVentilationFragment NewInstance(String gateId, int index){
        Bundle args = new Bundle();
        args.putString(ARG_GATE_ID, gateId);
        args.putInt(ARG_INDEX, index);
        AddAutomationRuleVentilationFragment instance = new AddAutomationRuleVentilationFragment();
        instance.setArguments(args);
        return instance;
    }


    private String mGateId;
    private int mIndex;

    String mInsideModuleId;
    String mOutsideModuleId;

    @BindView(R.id.fragment_add_automation_ventilation_preferences_notifications_time)
    Spinner mNotificationSpin;
    @BindView(R.id.fragment_add_automation_ventilation_preferences_notifications)
    CheckBox mNotificationEnabled;
    @BindView(R.id.fragment_add_automation_ventilation_inside_select)
    TextView mInside;
    @BindView(R.id.fragment_add_automation_ventilation_outside_select)
    TextView mOutside;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mGateId = getArguments().getString(ARG_GATE_ID);
            mIndex = getArguments().getInt(ARG_INDEX);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_automation_rule_ventilation, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.automation_add_rule_preferences_notifications_time,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mNotificationSpin.setAdapter(adapter);

        return view;
    }


    protected void finishActivity(BaseItem item) {
        Intent data = new Intent();
        data.putExtra(AutomationPagerFragment.EXTRA_ADD_ITEM, item);
        data.putExtra(AutomationPagerFragment.EXTRA_INDEX, mIndex);
        mActivity.setResult(Activity.RESULT_OK, data);
        mActivity.finish();
    }

    @OnClick(R.id.fragment_add_automation_ventilation_outside_select)
    public void onOutsideSelectClicked(){
        showModuleSelectFragment(REQUEST_CODE_OUTSIDE, ModuleType.TYPE_TEMPERATURE);
    }

    @OnClick(R.id.fragment_add_automation_ventilation_inside_select)
    public void onInsideSelectClicked(){
        showModuleSelectFragment(REQUEST_CODE_INSIDE, ModuleType.TYPE_TEMPERATURE);
    }

    private void showModuleSelectFragment(int requestCode, ModuleType type) {
        ModuleSelectFragment fragment = ModuleSelectFragment.NewInstance(mGateId, type.getTypeId(), requestCode);
        getActivity().
                getSupportFragmentManager().
                beginTransaction().
                setCustomAnimations(R.anim.right_in, R.anim.right_out).
                add(R.id.activity_add_automation_rule_container, fragment).
                addToBackStack(null).
                commit();

    }

    @Override
    public void RuleSaveClicked(String ruleName) {


        if(ruleName == null || ruleName.isEmpty()){
            Toast.makeText(getActivity(),
                    R.string.automation_add_rule_error_missing_name,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(mOutsideModuleId == null ||
                mInsideModuleId == null){
            Toast.makeText(getActivity(),
                    R.string.automation_add_rule_error_missing_sensor,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        BaseItem item = new VentilationItem(ruleName,
                mGateId,
                true,
                null,
                mOutsideModuleId,
                mInsideModuleId);

        finishActivity(item);
    }

    @Override
    public void onModuleSelected(int requestCode, String absoluteModuleId) {
        Module module = Controller.getInstance(getActivity()).getDevicesModel().getModule(mGateId, absoluteModuleId);

        if(module != null) {
            String moduleName = module.getName(getActivity());
            switch (requestCode) {
                case REQUEST_CODE_INSIDE:
                    mInsideModuleId = absoluteModuleId;
                    mInside.setText(moduleName);
                    break;
                case REQUEST_CODE_OUTSIDE:
                    mOutsideModuleId = absoluteModuleId;
                    mOutside.setText(moduleName);
                    break;
            }
        }
    }
}