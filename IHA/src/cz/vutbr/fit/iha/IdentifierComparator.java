package cz.vutbr.fit.iha;

import java.util.Comparator;

public class IdentifierComparator implements Comparator<IIdentifier> {

	@Override
	public int compare(IIdentifier lhs, IIdentifier rhs) {
		return lhs.getId().compareTo(rhs.getId());
	}

}
