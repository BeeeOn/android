package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddAutomationRuleDewingFragment;
import com.rehivetech.beeeon.gui.fragment.AddAutomationRuleFragment;
import com.rehivetech.beeeon.gui.fragment.AddAutomationRuleVentilationFragment;
import com.rehivetech.beeeon.gui.fragment.ModuleSelectFragment;
import com.rehivetech.beeeon.gui.fragment.ModuleSelectFragment.IOnModuleSelectListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by mrnda on 19/10/2016.
 */

public class AddAutomationRuleActivity extends BaseApplicationActivity
    implements IOnModuleSelectListener{

    public static final String EXTRA_INDEX      = "idx";
    public static final String EXTRA_GATE_ID    = "gate_id";



    public static Intent getAddAutomationRuleActivityIntent(Context context, int index, String gateId){
        Intent intent = new Intent(context, AddAutomationRuleActivity.class);
        intent.putExtra(EXTRA_INDEX, index);
        intent.putExtra(EXTRA_GATE_ID, gateId);
        return intent;
    }

    private String mGateId;
    private int mTabIndex;

    @BindView(R.id.automation_add_rule_type)
    public Spinner mType;
    @BindView(R.id.automation_add_rule_name)
    public EditText mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_automation_item);
        GetExtrasFromIntent();

        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.activity_add_automation_rule_container, AddAutomationRuleFragment.NewInstance(mTabIndex, mGateId)).
                commit();
    }

    private void GetExtrasFromIntent() {
        Intent i = getIntent();
        if(i != null) {
            mTabIndex = i.getIntExtra(EXTRA_INDEX, 0);
            mGateId = i.getStringExtra(EXTRA_GATE_ID);
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }


    @Override
    public void onModuleSelected(int requestCode, String absoluteModuleId) {
        getSupportFragmentManager().popBackStackImmediate();
        getSupportFragmentManager().executePendingTransactions();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_add_automation_rule_container);
        if(fragment != null && fragment instanceof IOnModuleSelectListener){
            ((IOnModuleSelectListener) fragment).onModuleSelected(requestCode, absoluteModuleId);
        }
    }
}
