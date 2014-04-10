/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.network;

import cz.vutbr.fit.intelligenthomeanywhere.User;
import cz.vutbr.fit.intelligenthomeanywhere.User.Gender;
import cz.vutbr.fit.intelligenthomeanywhere.User.Role;

/**
 * @author ThinkDeep
 *
 */
public class ActualUser extends User {

	/* (non-Javadoc)
	 * @see cz.vutbr.fit.intelligenthomeanywhere.User#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.fit.intelligenthomeanywhere.User#getEmail()
	 */
	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return super.getEmail();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.fit.intelligenthomeanywhere.User#getRole()
	 */
	@Override
	public Role getRole() {
		// TODO Auto-generated method stub
		return super.getRole();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.fit.intelligenthomeanywhere.User#getGender()
	 */
	@Override
	public Gender getGender() {
		// TODO Auto-generated method stub
		return super.getGender();
	}

	/* (non-Javadoc)
	 * @see cz.vutbr.fit.intelligenthomeanywhere.User#toDebugString()
	 */
	@Override
	public String toDebugString() {
		// TODO Auto-generated method stub
		return super.toDebugString();
	}

	private static ActualUser mActualUser;
	private String mToken;
	private String mPicture;
	private String mSessionId;
	
	/**
	 * @param name
	 * @param email
	 * @param role
	 * @param gender
	 */
	public ActualUser(String name, String email, Role role, Gender gender) {
		super(name, email, role, gender);
		mActualUser = this;
	}
	
	public static ActualUser getActualUser(){
		if(mActualUser == null){
			mActualUser = new ActualUser("", "", Role.Guest, Gender.Male);
		}
		return mActualUser;
	}

	/**
	 * @return the mToken
	 */
	public String getToken() {
		return mToken;
	}

	/**
	 * @param mToken the mToken to set
	 */
	public void setToken(String mToken) {
		this.mToken = mToken;
	}

	/**
	 * @return the mPicture
	 */
	public String getPicture() {
		return mPicture;
	}

	/**
	 * @param mPicture the mPicture to set
	 */
	public void setPicture(String mPicture) {
		this.mPicture = mPicture;
	}

	/**
	 * @return the mSessionId
	 */
	public String getSessionId() {
		return mSessionId;
	}

	/**
	 * @param mSessionId the mSessionId to set
	 */
	public void setSessionId(String mSessionId) {
		this.mSessionId = mSessionId;
	}
	
	

}
