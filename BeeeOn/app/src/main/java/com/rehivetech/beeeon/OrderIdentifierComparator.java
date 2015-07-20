package com.rehivetech.beeeon;

import java.util.Comparator;

public class OrderIdentifierComparator implements Comparator<IOrderIdentifier> {

	@Override
	public int compare(IOrderIdentifier lhs, IOrderIdentifier rhs) {
		Integer lsort = lhs.getSort();
		Integer rsort = rhs.getSort();

		// No sort preferences, sort by ids
		if (lsort == null && rsort == null)
			return lhs.getId().compareTo(rhs.getId());

		// Both sort preferences, sort by sort
		if (lsort != null && rsort != null)
			return lsort.compareTo(rsort);

		// Only one sort preferences, it has priority
		return lsort != null ? -1 : 1;
	}
}
