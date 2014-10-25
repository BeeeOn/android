/**
 * 
 */
package cz.vutbr.fit.iha.util;

import android.util.Log;

/**
 * @author ThinkDeep
 *
 */
public class Ilog{
	
	private boolean mEnable;
	private String mTag;
	
	public Ilog(boolean enable){
		mEnable = enable;
	}
	
	public Ilog(boolean enable, String tag){
		mEnable = enable;
		mTag = tag;
	}
	
	public void i(String tag, String msg){
		if(mEnable)
			Log.i(tag, msg);
	}
	
	public void d(String tag, String msg){
		if(mEnable)
			Log.d(tag, msg);
	}
	
	public void e(String tag, String msg){
		if(mEnable)
			Log.e(tag, msg);
	}
	
	public void i(String msg){
		if(mEnable)
			Log.i(mTag, msg);
	}
	
	public void d(String msg){
		if(mEnable)
			Log.d(mTag, msg);
	}
	
	public void e(String msg){
		if(mEnable)
			Log.e(mTag, msg);
	}
}
