package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.AddAutomationRuleFragment.RuleSaveClickedListener;
import com.rehivetech.beeeon.gui.fragment.ModuleSelectFragment.IOnModuleSelectListener;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.model.entity.automation.AutomationItem;
import com.rehivetech.beeeon.model.entity.automation.DewingItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * Created by mrnda on 20/10/2016.
 */

public class AddAutomationRuleDewingFragment extends BaseApplicationFragment implements
        RuleSaveClickedListener, IOnModuleSelectListener{

    private static final int REQUEST_CODE_INSIDE     = 1002;
    private static final int REQUEST_CODE_OUTSIDE    = 1003;
    private static final int REQUEST_CODE_HUMIDITY   = 1004;

    private static final String ARG_GATE_ID = "gate_id";
    private static final String ARG_INDEX = "index";

    public static Fragment NewInstance(String gateId, int index){
        Bundle args = new Bundle();
        args.putString(ARG_GATE_ID, gateId);
        args.putInt(ARG_INDEX, index);

        Fragment instance = new AddAutomationRuleDewingFragment();
        instance.setArguments(args);
        return instance;
    }

    private String mGateId;
    private int mIndex;


    String mInsideModuleId;
    String mOutsideModuleId;
    String mHumidityModuleId;


    @BindView(R.id.fragment_add_automation_dewing_preferences_notifications_time)
    Spinner mNotificationSpin;
    @BindView(R.id.fragment_add_automation_dewing_inside_select)
    TextView mInside;
    @BindView(R.id.fragment_add_automation_dewing_outside_select)
    TextView mOutside;
    @BindView(R.id.fragment_add_automation_dewing_humidity_select)
    TextView mHumidity;

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
        View view = inflater.inflate(R.layout.fragment_add_automation_rule_dewing, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.automation_add_rule_preferences_notifications_time,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mNotificationSpin.setAdapter(adapter);

        return view;
    }


    protected void finishActivity() {
        mActivity.setResult(Activity.RESULT_OK);
        mActivity.finish();
    }


    @OnClick(R.id.fragment_add_automation_dewing_inside_select)
    public void onInsideSensorClicked(View view){
        showModuleSelectFragment(REQUEST_CODE_INSIDE, ModuleType.TYPE_TEMPERATURE);
        hideKeyboard(view);
    }

    @OnClick(R.id.fragment_add_automation_dewing_outside_select)
    public void onOutsideSensorClicked(View view){
        showModuleSelectFragment(REQUEST_CODE_OUTSIDE, ModuleType.TYPE_TEMPERATURE);
        hideKeyboard(view);
    }



    @OnClick(R.id.fragment_add_automation_dewing_humidity_select)
    public void onHumiditySensorClicked(View view){
        showModuleSelectFragment(REQUEST_CODE_HUMIDITY, ModuleType.TYPE_HUMIDITY);
        hideKeyboard(view);
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
    public void RuleSaveClicked(final String ruleName) {

        if(ruleName == null || ruleName.isEmpty()){
            Toast.makeText(getActivity(),
                    R.string.automation_add_rule_error_missing_name,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(mOutsideModuleId == null ||
                mInsideModuleId == null ||
                mHumidityModuleId == null){
            Toast.makeText(getActivity(),
                    R.string.automation_add_rule_error_missing_sensor,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AutomationItem item = new AutomationItem();
                item.setGateId(mGateId);
                item.setAutomationType(mIndex);
                DewingItem dewingItem = new DewingItem();
                dewingItem.setName(ruleName);
                dewingItem.setOutsideTempAbsoluteModueId(mOutsideModuleId);
                dewingItem.setInsideTempAbsoluteModuleId(mInsideModuleId);
                dewingItem.setHumidityAbsoluteModuleId(mHumidityModuleId);
                dewingItem.setActive(true);
                item.setDewingItem(dewingItem);
                realm.copyToRealm(item);
            }
        });
        realm.close();

        finishActivity();
    }

    @Override
    public void onModuleSelected(int requestCode, String absoluteModuleId) {
        Module module = Controller.getInstance(getActivity()).getDevicesModel().getModule(mGateId, absoluteModuleId);

        if(module != null) {
            String moduleName = module.getName(getActivity());
            switch (requestCode) {
                case REQUEST_CODE_HUMIDITY:
                    mHumidityModuleId = absoluteModuleId;
                    mHumidity.setText(moduleName);
                    break;
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

    private void hideKeyboard(View view) {
        InputMethodManager imm =(InputMethodManager)getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
