package com.rehivetech.beeeon.gui.adapter.dashboard;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 25.2.16.
 */
public class AddDashboardCardAdapter extends RecyclerView.Adapter<AddDashboardCardAdapter.CardViewHolder> {

	private List<CardItem> mCardItems = new ArrayList<>();
	private ItemClickListener mItemClickListener;

	public AddDashboardCardAdapter(ItemClickListener clickListener) {
		mItemClickListener = clickListener;
	}

	public void setItems(List<CardItem> items) {
		mCardItems = items;
		notifyDataSetChanged();
	}

	@Override
	public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_dashboard_card, parent, false);
		return new CardViewHolder(view);
	}

	@Override
	public void onBindViewHolder(CardViewHolder holder, int position) {
		CardItem item = mCardItems.get(position);

		holder.bind(item);
	}

	@Override
	public int getItemCount() {
		return mCardItems.size();
	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(position);
	}

	public class CardViewHolder extends RecyclerView.ViewHolder{

		public final View mRoot;
		public final TextView mLabel;
		public final ImageView mImage;

		public CardViewHolder(View itemView) {
			super(itemView);

			mRoot = itemView;
			mLabel = (TextView) itemView.findViewById(R.id.item_add_dashboard_card_label);
			mImage = (ImageView) itemView.findViewById(R.id.item_add_dashboard_card_image);
		}

		public void bind(final CardItem item) {
			mLabel.setText(item.mTitle);
			mImage.setImageResource(item.mCardImage);

			mRoot.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mItemClickListener != null) {
						mItemClickListener.onItemClick(item.mCardType);
					}
				}
			});
		}
	}


	public static class CardItem {

		public static final int CARD_ACTUAL_VALUE = 0;
		public static final int CARD_LINE_GRAPH = 1;
		public static final int CARD_BAR_GRAPH = 2;
		public static final int CARD_PIE_GRAPH = 3;
		public static final int CARD_VENTILATION = 4;

		@Retention(RetentionPolicy.CLASS)
		@IntDef({CARD_ACTUAL_VALUE, CARD_LINE_GRAPH, CARD_BAR_GRAPH, CARD_PIE_GRAPH, CARD_VENTILATION})

		public @interface CardType {}

		@CardType
		private final int mCardType;
		private final int mCardImage;
		private final int mTitle;

		public CardItem(@CardType int cardType, @DrawableRes int cardImage, @StringRes int title) {
			mCardType = cardType;
			mCardImage = cardImage;
			mTitle = title;
		}

		public int getCardType() {
			return mCardType;
		}

		public int getCardImage() {
			return mCardImage;
		}

		public int getTitle() {
			return mTitle;
		}
	}

	public interface ItemClickListener {
		void onItemClick(@CardItem.CardType int type);
	}
}
