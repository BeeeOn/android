package cz.vutbr.fit.intelligenthomeanywhere;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * @brief Methods for fixing various compatibility issues
 * @author Robyer
 *
 */
public class Compatibility {

	/**
	 * Set background of View with correct API method
	 * @param view
	 * @param background
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void setBackground(View view, Drawable background) {	
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(background);
	    } else {
	    	view.setBackgroundDrawable(background);
	    }
	}

}
