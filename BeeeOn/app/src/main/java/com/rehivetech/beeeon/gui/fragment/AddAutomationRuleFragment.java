package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mrnda on 20/10/2016.
 */

public class AddAutomationRuleFragment extends BaseApplicationFragment
    implements AdapterView.OnItemSelectedListener, ModuleSelectFragment.IOnModuleSelectListener {

    private static final String ARG_INDEX   = "index";
    private static final String ARG_GATE_ID = "gate_id";

    public static final int VENTILATON_POSITION     = 0;
    public static final int WINDOW_DEWING_POSITION  = 1;

    public static AddAutomationRuleFragment NewInstance(int index, String gateId){
        Bundle args = new Bundle();
        args.putString(ARG_GATE_ID, gateId);
        args.putInt(ARG_INDEX, index);

        AddAutomationRuleFragment fragment = new AddAutomationRuleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String mGateId;
    private int mTabIndex;

    @BindView(R.id.automation_add_rule_type)
    public Spinner mType;
    @BindView(R.id.automation_add_rule_name)
    public EditText mName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity.setupToolbar(R.string.automation_add_rule_title, BaseApplicationActivity.INDICATOR_BACK);
        Bundle args = getArguments();
        if(args != null){
            mTabIndex = args.getInt(ARG_INDEX);
            mGateId = args.getString(ARG_GATE_ID);
        }
    }


    public String getRuleName(){
        return mName.getText().toString();
    }

    @OnClick(R.id.fragment_add_automation_item_done)
    public void onDoneClicked()
    {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragment_add_automation_container);
        if(fragment instanceof RuleSaveClickedListener){
            ((RuleSaveClickedListener)fragment).RuleSaveClicked(mName.getText().toString());
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_automation_rule, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.automation_add_rule_types_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mType.setAdapter(adapter);
        mType.setOnItemSelectedListener(this);
        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        Fragment fragment = null;
        if(VENTILATON_POSITION == position)
        {
            fragment = AddAutomationRuleVentilationFragment.NewInstance(mGateId, mTabIndex);
        } else {
            fragment = AddAutomationRuleDewingFragment.NewInstance(mGateId, mTabIndex);
        }
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_add_automation_container, fragment)
                .commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        adapterView.setSelection(0);
    }

    @Override
    public void onModuleSelected(int requestCode, String absoluteModuleId) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragment_add_automation_container);
        if(fragment != null && fragment instanceof ModuleSelectFragment.IOnModuleSelectListener){
            ((ModuleSelectFragment.IOnModuleSelectListener) fragment).onModuleSelected(requestCode, absoluteModuleId);
        }
    }


    public interface RuleSaveClickedListener{
        void RuleSaveClicked(String ruleName);
    }
}
