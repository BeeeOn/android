package cz.vutbr.fit.intelligenthomeanywhere.adapter;

public interface Adapter {

	public String toString();
	
	public void SetInit(boolean init);
	public boolean GetInit();
	
	public void SetLocation(String location);
	public String GetLocation();
	
	public void SetName(String name);
	public String GetName();
	
	public void SetRefresh(int refresh);
	public int GetRefresh();
	
	public void SetBattery(int battery);
	public int GetBattery();
	
	public void SetAddress(String address);
	public String GetAddress();
	
	public void SetQuality(int quality);
	public int GetQuality();
	
	public void SetLog(String log);
	public String GetLog();
	
	public void SetType(int type);
	public int GetType();
	
	public void SetInvolveTime(String time);
	public String GetInvolveTime();
}
