/**
 * 
 */
package cz.vutbr.fit.iha.network.xml.condition;

import java.util.ArrayList;

/**
 * @author ThinkDeep
 * 
 */
public class Condition {

	private String mId;
	private String mName;
	private String mType;

	private ArrayList<ConditionFunction> mFuncs;

	/**
	 * 
	 */
	public Condition(String id, String name, String type, ArrayList<ConditionFunction> functions) {
		mId = id;
		mName = name;
		mType = type;
		mFuncs = functions;
	}

	public Condition(String id, String name) {
		mId = id;
		mName = name;
	}

	/**
	 * @return the mId
	 */
	public String getId() {
		return mId;
	}

	/**
	 * @param mId
	 *            the mId to set
	 */
	public void setId(String Id) {
		this.mId = Id;
	}

	/**
	 * @return the mName
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param mName
	 *            the mName to set
	 */
	public void setName(String Name) {
		this.mName = Name;
	}

	/**
	 * @return the mType
	 */
	public String getType() {
		return mType;
	}

	/**
	 * @param mType
	 *            the mType to set
	 */
	public void setType(String Type) {
		this.mType = Type;
	}

	/**
	 * @return the mFuncs
	 */
	public ArrayList<ConditionFunction> getFuncs() {
		return mFuncs;
	}

	/**
	 * @param mFuncs
	 *            the mFuncs to set
	 */
	public void setFuncs(ArrayList<ConditionFunction> Funcs) {
		this.mFuncs = Funcs;
	}

}
