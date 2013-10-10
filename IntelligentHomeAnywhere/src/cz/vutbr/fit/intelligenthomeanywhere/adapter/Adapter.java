package cz.vutbr.fit.intelligenthomeanywhere.adapter;

public interface Adapter {

	public String toString();
	
	public void setInit(boolean init);
	public boolean getInit();
	
	public void setLocation(String location);
	public String getLocation();
	
	public void setName(String name);
	public String getName();
	
	public void setRefresh(int refresh);
	public int getRefresh();
	
	public void setBattery(int battery);
	public int getBattery();
	
	public void setAddress(String address);
	public String getAddress();
	
	public void setQuality(int quality);
	public int getQuality();
	
	public void setLog(String log);
	public String getLog();
	
	public void setType(int type);
	public int getType();
	
	public void setInvolveTime(String time);
	public String getInvolveTime();
}
