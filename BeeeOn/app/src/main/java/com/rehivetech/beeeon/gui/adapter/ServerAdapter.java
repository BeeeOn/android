package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
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

	public void setOnItemClickListener(ClickableRecyclerViewAdapter.OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}

	public ServerAdapter(Context context, OrderedRealmCollection<Server> data) {
		super(context, data, true);
	}

	@Override
	public ServerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = inflater.inflate(R.layout.item_list_server, parent, false);
		ServerViewHolder viewHolder = new ServerViewHolder(v);
		viewHolder.setOnItemClickListener(mOnItemClickListener);
		return viewHolder;
	}


	@Override
	public void onBindViewHolder(ServerViewHolder holder, int position) {
		Server server = getData().get(position);
		holder.data = server;
		holder.name.setText(server.toString());
//		holder.
	}

	public static class ServerViewHolder extends ClickableRecyclerViewAdapter.ViewHolder {
		@Bind(R.id.server_radio) public RadioButton radio;
		@Bind(R.id.server_name) public TextView name;

		public Server data;

		public ServerViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
