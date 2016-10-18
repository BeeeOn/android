package com.rehivetech.beeeon;

import android.text.TextUtils;

import java.math.BigInteger;
import java.util.Comparator;

public class IdentifierComparator implements Comparator<IIdentifier> {

	@Override
	public int compare(IIdentifier lhs, IIdentifier rhs) {
		return compareNumericIds(lhs, rhs);
	}

	public static int compareNumericIds(IIdentifier lhs, IIdentifier rhs) {
		String lhsId = lhs.getId();
		String rhsId = rhs.getId();

		if (TextUtils.isDigitsOnly(lhsId) && TextUtils.isDigitsOnly(rhsId)) {
			// Numeric comparison
			return new BigInteger(lhsId).compareTo(new BigInteger(rhsId));
		} else {
			// String comparison
			return lhsId.compareTo(rhsId);
		}
	}

}
