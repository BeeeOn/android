package com.rehivetech.beeeon.household.device;

import android.support.annotation.StringDef;

/**
 * Annotation interface for status options (pretends to be enum)
 * @author Tomas Mlynaric
 */
@StringDef({Status.AVAILABLE, Status.UNAVAILABLE})
public @interface Status {
	String AVAILABLE = "available";
	String UNAVAILABLE = "unavailable";
}
