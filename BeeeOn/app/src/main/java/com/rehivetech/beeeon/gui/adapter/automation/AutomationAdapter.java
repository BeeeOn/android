package com.rehivetech.beeeon.gui.adapter.automation;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import com.rehivetech.beeeon.gui.adapter.RecyclerViewSelectableAdapter;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.model.entity.automation.AutomationItem;
import com.rehivetech.beeeon.model.entity.automation.DewingItem;
import com.rehivetech.beeeon.model.entity.automation.IAutomationItem;
import com.rehivetech.beeeon.model.entity.automation.VentilationItem;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * @author Mrnda
 * @author Martin Matejcik
 * @since 10/14/2016.
 */

public class AutomationAdapter extends RecyclerViewSelectableAdapter {

    private static final int VENTILATION_VIEW_TYPE      = 0;
    private static final int WINDOW_DEWING_VIEW_TYPE    = 1;

    private final TimeHelper mTimeHelper;
    private final UnitsHelper mUnitsHelper;

    private IItemClickListener mItemClickListener;
    private ActionModeCallback mActionModeCallback;
	private RealmResults<AutomationItem> mItems;
	private String mGateId;
    private View mEmptyView;

    public AutomationAdapter(Context context, IItemClickListener itemClickListener, ActionModeCallback actionModeCallback) {
        super(context);
        mItemClickListener = itemClickListener;
        mActionModeCallback = actionModeCallback;

        SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
        mTimeHelper = Utils.getTimeHelper(prefs);
        mUnitsHelper = Utils.getUnitsHelper(prefs, mContext);
    }

    @Override
    public int getItemViewType(int position) {
		if (mItems.get(position).getDewingItem() != null) {
			return WINDOW_DEWING_VIEW_TYPE;
		} else {
			return VENTILATION_VIEW_TYPE;
		}
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		BaseAutomationViewHolder holder;

		if (viewType == VENTILATION_VIEW_TYPE) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.automation_item_ventilation, parent, false);
			holder = new AutomationVentilationViewHolder(view);
		} else {
			View view = LayoutInflater.from(mContext).inflate(R.layout.automation_item_dewing, parent, false);
			holder = new AutomationDewingViewHolder(view);
		}

		final BaseAutomationViewHolder finalHolder = holder;
		finalHolder.mActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				final IAutomationItem item = getItemInPosition(finalHolder.getAdapterPosition());
				Realm realm = Realm.getDefaultInstance();

				if (realm.isInTransaction()) {
					return;
				}

				realm.executeTransaction(new Realm.Transaction() {
					@Override
					public void execute(Realm realm) {
						item.setActive(isChecked);
					}
				});
				realm.close();
			}
		});

		return finalHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		((BaseAutomationViewHolder) holder).bind(Controller.getInstance(mContext), getItemInPosition(position), position);
	}

    @Override
    public int getItemCount() {
		return mItems == null ? 0 : mItems.size();
	}

	private IAutomationItem getItemInPosition(int position) {
		IAutomationItem item;

		if ((item = mItems.get(position).getDewingItem()) == null) {
			item = mItems.get(position).getVentilationItem();
		}

		return item;
	}

    public void setEmptyView(View emptyView){
        this.mEmptyView = emptyView;
    }

    private void setEmptyViewVisibility(boolean empty) {
        if(mEmptyView != null){
            if(!empty){
                mEmptyView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setItems(RealmResults<AutomationItem> items) {
		if (mItems != null) {
			mItems.removeChangeListeners();
		}

		mItems = items;

		if (mItems.size() != 0) {
			mGateId = mItems.get(0).getGateId();
		}

		mItems.addChangeListener(new RealmChangeListener<RealmResults<AutomationItem>>() {
			@Override
			public void onChange(RealmResults<AutomationItem> element) {
				setEmptyViewVisibility(mItems.isEmpty());
				notifyDataSetChanged();
			}
		});

		setEmptyViewVisibility(mItems.isEmpty());
        notifyDataSetChanged();
    }

	public AutomationItem getItem(int position) {
		return mItems.get(position);
    }

	public RealmResults<AutomationItem> getItems() {
		return mItems;
	}

    public void deleteItem(AutomationItem item) {
		int position = mItems.indexOf(item);
		mItems.deleteFromRealm(position);
        setEmptyViewVisibility(mItems.isEmpty());
        notifyItemRemoved(position);
    }

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		super.onDetachedFromRecyclerView(recyclerView);
		if (mItems != null) {
			mItems.removeChangeListeners();
		}
	}

	public abstract class BaseAutomationViewHolder extends SelectableViewHolder implements View.OnLongClickListener {
        public final CardView mCardView;
		public SwitchCompat mActive;

        public BaseAutomationViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView;
        }

		public abstract void bind(Controller controller, IAutomationItem item, int position);

        @Override
        protected void setSelectedBackground(boolean isSelected) {
            if (isSelected) {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.gray_material_400));
            } else {
                mCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
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

		@BindView(R.id.automation_item_ventilation_name)
        public TextView mRuleName;
		@BindView(R.id.automation_item_ventilation_temp_inside)
        public TextView mInsideTemp;
		@BindView(R.id.automation_item_ventilation_temp_outside)
        public TextView moutsideTemp;
		@BindView(R.id.automation_item_ventilation_iv)
        public ImageView mAdviceImage;
		@BindView(R.id.automation_item_ventilation_advice)
        public TextView mAdviceText;

		public AutomationVentilationViewHolder(View view) {
			super(view);
			ButterKnife.bind(this, view);
			mActive = ButterKnife.findById(view, R.id.automation_item_ventilation_enabled);
			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
		}

		@Override
        public void bind(Controller controller, IAutomationItem item, int position) {
            float outsideTemp = 0.0f;
            float insideTemp = 0.0f;
			VentilationItem ventilationItem = (VentilationItem) item;

			Module module = controller.getDevicesModel().getModule(mGateId, ventilationItem.getOutsideAbsoluteModuleId());

			if (module != null) {
				outsideTemp = (float) module.getValue().getDoubleValue();
			}

            Module insideModule = controller.getDevicesModel().getModule(mGateId, ventilationItem.getInSideAbsoluteModuleId());

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

    public class AutomationDewingViewHolder extends BaseAutomationViewHolder implements View.OnClickListener {

		@BindView(R.id.automation_item_dewing_name)
        public TextView mRuleName;
		@BindView(R.id.automation_item_dewing_probability_text)
        public TextView mProbabilityText;
		@BindView(R.id.automation_item_dewing_probability_image)
		public ImageView mProbabilityImage;
		@BindView(R.id.automation_item_dewing_advice)
        public TextView mAdvice;

        public AutomationDewingViewHolder(View view){
            super(view);
			ButterKnife.bind(this, view);
			mActive = ButterKnife.findById(view, R.id.automation_item_dewing_active);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

		@Override
        public void bind(Controller controller, IAutomationItem item, int position) {
            float outsideTemp = 0.0f;
            float insideTemp = 0.0f;
            float humidity = 0.0f;

			DewingItem dewingItem = (DewingItem) item;
            if (dewingItem.getOutsideTempAbsoluteModueId() == null) {
                outsideTemp = controller.getWeatherModel().getWeather(mGateId).getTemp();

            } else {
                Module module = controller.getDevicesModel().getModule(mGateId, dewingItem.getOutsideTempAbsoluteModueId());

                if (module != null) {
                    outsideTemp = (float) module.getValue().getDoubleValue();
                }
            }

            Module insideModule = controller.getDevicesModel().getModule(mGateId, dewingItem.getInsideTempAbsoluteModuleId());

            if (insideModule != null) {
                insideTemp = (float) insideModule.getValue().getDoubleValue();
            }

            Module humidityModule = controller.getDevicesModel().getModule(mGateId, dewingItem.getHumidityAbsoluteModuleId());

            if(humidityModule != null){
                humidity = (float) humidityModule.getValue().getDoubleValue();
            }

            if((humidity * insideTemp * outsideTemp) % 2 == 0){
                mAdvice.setVisibility(View.VISIBLE);
                DrawableCompat.setTint(mProbabilityImage.getDrawable(), ContextCompat.getColor(mContext, R.color.beeeon_accent));
                mProbabilityText.setText(R.string.automation_dewing_item_probability_very_high);
            } else {
                mAdvice.setVisibility(View.GONE);
                DrawableCompat.setTint(mProbabilityImage.getDrawable(), ContextCompat.getColor(mContext, R.color.beeeon_primary_dark));
                mProbabilityText.setText(R.string.automation_dewing_item_probability_low);
            }

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


}
