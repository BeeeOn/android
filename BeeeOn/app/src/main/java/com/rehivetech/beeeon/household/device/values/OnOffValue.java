package com.rehivetech.beeeon.household.device.values;

import android.support.annotation.NonNull;

/**
 * Created by mrnda on 30/11/2016.
 */

public class OnOffValue extends BooleanValue {

    public OnOffValue(@NonNull Item falseItem, @NonNull Item trueItem) {
        super(falseItem, trueItem);
    }
}
