package cz.vutbr.fit.iha.activity.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;

/**
 * Class represents AlertDialog, precisely AlertDialog.Builder, for showing custom dialogs.
 * To open dialog call show() method and for close has to be called dismiss().
 * @author ThinkDeep
 *
 */
public class CustomAlertDialog extends Builder {

	private AlertDialog mParentDialog;
	
	private TextView mTitle;
	private TextView mMessage;
	private Button mNeutral;
	private Button mPositive;
	
	public CustomAlertDialog(Context cntx) {
		super(cntx);
		
		this.setInverseBackgroundForced(true);
		LayoutInflater layoutInflater = (LayoutInflater)cntx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.custom_alert_dialog, null);
		
		mTitle = (TextView)view.findViewById(R.id.custom_alert_title);
		mMessage = (TextView)view.findViewById(R.id.custom_alert_message);
		mNeutral = (Button)view.findViewById(R.id.custom_alert_neutral_button);
		mPositive = (Button)view.findViewById(R.id.custom_alert_positive_button);
		
		this.setView(view);
	}
	
	@Override
	public AlertDialog show() {
		mParentDialog = this.create();
		mParentDialog.show();
		return mParentDialog;
	}
	
	@Override
	public Builder setTitle(CharSequence title) {
		mTitle.setText(title);
		mTitle.setVisibility(View.VISIBLE);
		return this;
	}
	
	@Override
	public Builder setMessage(CharSequence message) {
		mMessage.setText(message);
		mMessage.setVisibility(View.VISIBLE);
		return this;
	}
	
	/**
	 * Method set name and action of left button
	 * @param text of button
	 * @param listener
	 * @return instance of this object
	 */
	public Builder setCustomNeutralButton(CharSequence text, OnClickListener listener) {
		mNeutral.setText(text);
		mNeutral.setVisibility(View.VISIBLE);
		mNeutral.setOnClickListener(listener);
		return this;
	}
	
	/**
	 * Method set name and action of right button
	 * @param text of button
	 * @param listener
	 * @return instance of this object
	 */
	public Builder setCustomPositiveButton(CharSequence text, OnClickListener listener) {
		mPositive.setText(text);
		mPositive.setVisibility(View.VISIBLE);
		mPositive.setOnClickListener(listener);
		return this;
	}
	
	/**
	 * Close the dialog
	 */
	public void dismiss(){
		if(mParentDialog != null && mParentDialog.isShowing())
			mParentDialog.dismiss();
	}
}
