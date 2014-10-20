/**
 * 
 */
package cz.vutbr.fit.iha.adapter.device.units;

import cz.vutbr.fit.iha.persistence.Persistence;

/**
 * @author Martin Doudera
 */
public class DefaultUnitPackages {
	
	private static void initUnits(Persistence persistence, String namespace, BaseUnit unit) {
		persistence.initializePreference(namespace, unit.getPersistenceKey(), String.valueOf(unit.getDefaultId()));
	}
	
	public static void setDefaultUnits(Persistence persistence, String namespace) {
		// TODO: udelat prepinac pro ruzne zeme a podle toho nastavit vychozi jednotky, prozatim davam jednotky, ktere pouzivame v CR
		// Locale locale = Locale.getDefault();

		initUnits(persistence, namespace, new TemperatureUnit());
		initUnits(persistence, namespace, new NoiseUnit());
	}

}
