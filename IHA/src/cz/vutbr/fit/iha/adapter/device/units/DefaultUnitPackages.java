/**
 * 
 */
package cz.vutbr.fit.iha.adapter.device.units;

import java.util.Locale;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.persistence.Persistence;

/**
 * @author Martin Doudera
 * 
 */
public class DefaultUnitPackages {
	public static void setDefaultUnits(Persistence persistance, String namespace) {
		Locale locale = Locale.getDefault();

		// TODO udelat prepinac pro ruzne zeme a podle toho nastavit vychozi jednotky
		// prozatim davam jednotky, ktere pouzivame v CR

		persistance.initializePreference(namespace, Constants.PERSISTANCE_PREF_TEMPERATURE, Temperature.CELSIUS.getId());

	}
}
