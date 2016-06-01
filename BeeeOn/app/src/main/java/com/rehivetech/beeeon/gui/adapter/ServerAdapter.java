package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.adapter.base.ClickableRecyclerViewAdapter;
import com.rehivetech.beeeon.model.entity.Server;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public class ServerAdapter extends RealmRecyclerViewAdapter<Server, ServerAdapter.ServerViewHolder> {
	private ClickableRecyclerViewAdapter.OnItemClickListener mOnItemClickListener;
	private ClickableRecyclerViewAdapter.OnItemLongClickListener mOnItemLongClickListener;

	private int mSelectedPosition = 0;

	public void setOnItemClickListener(ClickableRecyclerViewAdapter.OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}

	public void setOnItemLongClickListener(ClickableRecyclerViewAdapter.OnItemLongClickListener onItemLongClickListener) {
		mOnItemLongClickListener = onItemLongClickListener;
	}

	public ServerAdapter(Context context, OrderedRealmCollection<Server> data) {
		super(context, data, true);
	}

	/**
	 * Refreshes all binded view except the one on position.
	 * Serves for refreshing radio buttons!!
	 *
	 * @param position this viewholder will not bind again
	 */
	public void setSelectedPosition(int position) {
		mSelectedPosition = position;
		notifyItemRangeChanged(0, position);
		notifyItemRangeChanged(position + 1, getItemCount() - position);
	}

	/**
	 * Returns actually selected position (radio button is checked)
	 *
	 * @return index
	 */
	public int getSelectedPosition() {
		return mSelectedPosition;
	}

	@Override
	public ServerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = inflater.inflate(R.layout.item_list_server, parent, false);
		ServerViewHolder viewHolder = new ServerViewHolder(v);
		viewHolder.setOnItemClickListener(mOnItemClickListener);
		viewHolder.setOnItemLongClickListener(mOnItemLongClickListener);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ServerViewHolder holder, int position) {
		Server server = getData().get(position);
		holder.name.setText(server.toString());
		holder.name.setTextColor(ContextCompat.getColor(context, server.isEditable() ? R.color.beeeon_secondary_text : R.color.beeeon_primary_text));
		holder.radio.setChecked(position == mSelectedPosition);
	}

	public static class ServerViewHolder extends ClickableRecyclerViewAdapter.ViewHolder {
		@Bind(R.id.server_radio) public RadioButton radio;
		@Bind(R.id.server_name) public TextView name;

		public ServerViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			radio.setOnClickListener(this);    // the same click listener as whole item
		}
	}
}
