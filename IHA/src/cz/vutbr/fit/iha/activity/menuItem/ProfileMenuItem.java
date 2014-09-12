package cz.vutbr.fit.iha.activity.menuItem;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

public class ProfileMenuItem extends AbstractMenuItem {
	private String mName;
	private String mEmail;
	private Bitmap mIcon;

	public ProfileMenuItem(String name, String email, Bitmap icon) {
		super(MenuItem.ID_UNDEFINED, MenuItemType.PROFILE);
		mName = name;
		mEmail = email;
		mIcon = icon;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.name);
		TextView emailView = (TextView) view.findViewById(cz.vutbr.fit.iha.R.id.email);
		ImageView iconView = (ImageView) view.findViewById(cz.vutbr.fit.iha.R.id.icon);

		nameView.setText(mName);
		emailView.setText(mEmail);
		iconView.setImageBitmap(mIcon);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_profile;
	}

}
