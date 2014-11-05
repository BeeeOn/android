package cz.vutbr.fit.iha.asynctask;

import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.RegisterAdapterPair;

public class RegisterAdapterTask extends CallbackTask<RegisterAdapterPair> {

	private Context mContext;
	private Controller mController;

	public RegisterAdapterTask(Context context) {
		super();
		mContext = context;
	}

	private String getUniqueAdapterName() {
		Vector<String> adapterNames = new Vector<String>();

		for (Adapter adapter : mController.getAdapters()) {
			adapterNames.add(adapter.getName());
		}

		String name = "";

		int number = 1;
		do {
			name = mContext.getString(R.string.adapter_default_name, number++);
		} while (adapterNames.contains(name));

		return name;
	}

	private String getHexaAdapterName(String id) {
		try {
			int number = Integer.parseInt(id);
			return Integer.toHexString(number).toUpperCase(Locale.getDefault());
		} catch (NumberFormatException e) {
			return getUniqueAdapterName();
		}
	}

	@Override
	protected Boolean doInBackground(RegisterAdapterPair pair) {
		mController = Controller.getInstance(mContext);

		String serialNumber = pair.adapterId;
		String name = pair.adapterName;

		// Set default name for this adapter, if user didn't filled any
		if (name.isEmpty()) {
			// name = getUniqueAdapterName();
			name = getHexaAdapterName(serialNumber);
		}

		return mController.registerAdapter(serialNumber, name);
	}

}
