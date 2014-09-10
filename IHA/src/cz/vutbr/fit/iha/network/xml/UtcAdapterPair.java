/**
 * 
 */
package cz.vutbr.fit.iha.network.xml;

import java.util.List;

import cz.vutbr.fit.iha.adapter.device.Facility;

/**
 * @author ThinkDeep
 *
 */
public class UtcAdapterPair{
	public List<Facility> Facilities;
	public int UTC = 0;
	
	public UtcAdapterPair(List<Facility> facilities, int UTC){
		Facilities = facilities;
		this.UTC = UTC;
	}
}
