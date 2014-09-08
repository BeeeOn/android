/**
 * 
 */
package cz.vutbr.fit.iha.adapter.device.units;

import java.util.Locale;

import cz.vutbr.fit.iha.Constants;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * @author Martin Doudera
 *
 */
public class DefaultUnitPackages {
	public static void setDefaultUnits(Context context) {
		Locale locale = Locale.getDefault();
		
		//TODO udelat prepinac pro ruzne zeme a podle toho nastavit vychozi jednotky
		// prozatim davam jednotky, ktere pouzivame v CR
		
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		editor.putString(Constants.PREF_TEMPERATURE, Temperature.CELSIUS.getId());
		
		editor.commit();
	}
}
