package com.rehivetech.beeeon;

import java.util.Comparator;

public class NameIdentifierComparator implements Comparator<INameIdentifier> {

	@Override
	public int compare(INameIdentifier lhs, INameIdentifier rhs) {
		int result = lhs.getName().compareTo(rhs.getName());

		// In case names are same, compare Ids
		if (result == 0)
			result = lhs.getId().compareTo(rhs.getId());

		return result;
	}
}
