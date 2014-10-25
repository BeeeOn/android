/**
 * 
 */
package cz.vutbr.fit.iha.adapter.device;

import java.util.Comparator;

/**
 * @author ThinkDeep
 *
 */
public class AdapterAddressComparator implements Comparator<Facility>{
	public AdapterAddressComparator() {}

	@Override
	public int compare(Facility left, Facility right) {
		return Integer.valueOf(left.getAdapterId()).compareTo(Integer.valueOf(right.getAdapterId()));
	}
}
