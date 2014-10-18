/**
 * 
 */
package cz.vutbr.fit.iha.adapter.device.units;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.persistence.Persistence;

/**
 * @author Martin Doudera
 * 
 */
public class DefaultUnitPackages {
	public static void setDefaultUnits(Persistence persistance, String namespace) {
		// TODO: udelat prepinac pro ruzne zeme a podle toho nastavit vychozi jednotky, prozatim davam jednotky, ktere pouzivame v CR
		// Locale locale = Locale.getDefault();

		persistance.initializePreference(namespace, Constants.PERSISTENCE_PREF_TEMPERATURE, TemperatureUnit.getDefault().getId());
		persistance.initializePreference(namespace, Constants.PERSISTENCE_PREF_NOISE, NoiseUnit.getDefault().getId());
	}
}
