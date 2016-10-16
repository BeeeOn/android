package com.rehivetech.beeeon.gui.adapter.automation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.gui.adapter.automation.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.automation.items.VentilationItem;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Mrnda on 10/14/2016.
 */

public class AutomationAdapter extends RecyclerViewSelectableAdapter {

    private static final int VENTILATION_VIEW_TYPE = 0;

    private final TimeHelper mTimeHelper;
    private final UnitsHelper mUnitsHelper;

    private Activity mActivity;
    private IItemClickListener mItemClickListener;
    private ActionModeCallback mActionModeCallback;
    private List<BaseItem> mItems = new ArrayList<>();
    private View mEmptyView;


    public AutomationAdapter(BaseApplicationActivity activity, IItemClickListener itemClickListener, ActionModeCallback actionModeCallback) {
        super(activity);
        mActivity = activity;
        mItemClickListener = itemClickListener;
        mActionModeCallback = actionModeCallback;

        SharedPreferences prefs = Controller.getInstance(mActivity).getUserSettings();
        mTimeHelper = Utils.getTimeHelper(prefs);
        mUnitsHelper = Utils.getUnitsHelper(prefs, mActivity);
    }

    @Override
    public int getItemViewType(int position) {
        BaseItem item = mItems.get(position);
        if(item instanceof VentilationItem){
            return VENTILATION_VIEW_TYPE;
        }
        return VENTILATION_VIEW_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == VENTILATION_VIEW_TYPE){
            View view = LayoutInflater.from(mContext).inflate(R.layout.automation_item_ventilation, parent, false);
            final AutomationVentilationViewHolder holder = new AutomationVentilationViewHolder(view);
            holder.mActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int position = holder.getAdapterPosition();
                    BaseItem item = mItems.get(position);
                    item.setActive(b);
                }
            });
            return holder;
        } else {
            return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseItem item = mItems.get(position);
        if(holder instanceof AutomationVentilationViewHolder) {
            ((AutomationVentilationViewHolder) holder).bind(
                    Controller.getInstance(mContext),
                    (VentilationItem) item,
                    position
            );
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setEmptyView(View emptyView){
        this.mEmptyView = emptyView;
    }

    public void addItem(BaseItem item) {
        mItems.add(item);
        setEmptyViewVisibility(mItems.isEmpty());
        notifyItemRangeInserted(0, mItems.size());
    }

    private void setEmptyViewVisibility(boolean empty) {
        if(mEmptyView != null){
            if(empty == false){
                mEmptyView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }

    }

    public void addItem(int position, BaseItem item) {
        mItems.add(position, item);
        setEmptyViewVisibility(mItems.isEmpty());
        notifyItemInserted(position);
    }

    public List<BaseItem> getItems() {
        return mItems;
    }

    public void setItems(List<BaseItem> items) {
        mItems.clear();
        mItems.addAll(items);
        setEmptyViewVisibility(mItems.isEmpty());
        notifyDataSetChanged();
    }

    public BaseItem getItem(int position) {
        return mItems.get(position);
    }


    public void moveItem(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        swapSelectedPosition(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void deleteItem(BaseItem item) {
        int position = mItems.indexOf(item);
        mItems.remove(item);
        setEmptyViewVisibility(mItems.isEmpty());
        notifyItemRemoved(position);
    }


    public class BaseAutomationViewHolder extends SelectableViewHolder implements View.OnLongClickListener {
        public final CardView mCardView;

        public BaseAutomationViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView;
        }

        @Override
        protected void setSelectedBackground(boolean isSelected) {
            if (isSelected) {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.gray_material_400));
            } else {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.white));
            }

        }

        protected boolean handleSelection() {
            if (getSelectedItemCount() > 0) {
                toggleSelection(getAdapterPosition());

                if (getSelectedItemCount() == 0) {
                    mActionModeCallback.finishActionMode();
                }
                return true;
            } else {
                return false;
            }
        }

        protected void handleGoogleAnalytics() {
            String analyticsItemName = null;
            switch (getItemViewType()) {

            }

            //GoogleAnalyticsManager.getInstance().logEvent(GoogleAnalyticsManager.EVENT_CATEGORY_DASHBOARD, GoogleAnalyticsManager.EVENT_ACTION_DETAIL_CLICK, analyticsItemName);
        }

        @Override
        public boolean onLongClick(View v) {
            return mItemClickListener != null && mItemClickListener.onRecyclerViewItemLongClick(getAdapterPosition(), getItemViewType());
        }
    }

    public class AutomationVentilationViewHolder extends BaseAutomationViewHolder implements View.OnClickListener {

        public TextView mRuleName;
        public TextView mInsideTemp;
        public TextView moutsideTemp;
        public ImageView mAdviceImage;
        public TextView mAdviceText;
        public SwitchCompat mActive;

        public AutomationVentilationViewHolder(View view){
            super(view);
            mRuleName = (TextView) view.findViewById(R.id.automation_item_ventilation_name);
            mInsideTemp = (TextView) view.findViewById(R.id.automation_item_ventilation_temp_inside);
            moutsideTemp = (TextView) view.findViewById(R.id.automation_item_ventilation_temp_outside);
            mAdviceImage = (ImageView) view.findViewById(R.id.automation_item_ventilation_iv);
            mAdviceText = (TextView) view.findViewById(R.id.automation_item_ventilation_advice);
            mActive = (SwitchCompat) view.findViewById(R.id.automation_item_ventilation_enabled);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public void bind(Controller controller, VentilationItem item, int position) {
            float outsideTemp = 0.0F;
            float insideTemp = 0.0F;

            if (item.getOutsideAbsoluteModuleId() == null) {
                outsideTemp = controller.getWeatherModel().getWeather(item.getGateId()).getTemp();

            } else {
                Module module = controller.getDevicesModel().getModule(item.getGateId(), item.getOutsideAbsoluteModuleId());

                if (module != null) {
                    outsideTemp = (float) module.getValue().getDoubleValue();
                }
            }

            Module insideModule = controller.getDevicesModel().getModule(item.getGateId(), item.getInSideAbsoluteModuleId());

            if (insideModule != null) {
                insideTemp = (float) insideModule.getValue().getDoubleValue();
                moutsideTemp.setText(String.format("%s %s", mUnitsHelper.getStringValue(insideModule.getValue(), outsideTemp), mUnitsHelper.getStringUnit(insideModule.getValue())));
                mInsideTemp.setText(mUnitsHelper.getStringValueUnit(insideModule.getValue()));
            }



            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.oval_primary);

            if (outsideTemp <= insideTemp) {
                mAdviceImage.setImageResource(R.drawable.ic_done_white_24dp);
                mAdviceText.setVisibility(View.GONE);
                drawable = Utils.setDrawableTint(drawable, ContextCompat.getColor(mContext, R.color.beeeon_primary));

            } else if (outsideTemp - 5 <= insideTemp){
                mAdviceImage.setImageResource(R.drawable.ic_val_win_open);
                mAdviceText.setVisibility(View.VISIBLE);
                mAdviceText.setText(R.string.automation_ventilation_item_advice_light);
                drawable = Utils.setDrawableTint(drawable, ContextCompat.getColor(mContext, R.color.beeeon_accent));
            } else {
                drawable = Utils.setDrawableTint(drawable, ContextCompat.getColor(mContext, R.color.red));
                mAdviceText.setVisibility(View.VISIBLE);
                mAdviceText.setText(R.string.automation_ventilation_item_advice_hard);
                mAdviceImage.setImageResource(R.drawable.ic_val_win_open);
            }

            Utils.setBackgroundImageDrawable(mAdviceImage, drawable);

            mActive.setChecked(item.isActive());
            mRuleName.setText(item.getName());

            setSelected(isSelected(position));
            
        }

        @Override
        public void onClick(View v) {
            if (!handleSelection() && mItemClickListener != null) {
                mItemClickListener.onRecyclerViewItemClick(getAdapterPosition(), AutomationAdapter.VENTILATION_VIEW_TYPE);
                handleGoogleAnalytics();
            }
        }
    }

    public interface ActionModeCallback {
        void finishActionMode();
    }

}
