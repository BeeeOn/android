package com.rehivetech.beeeon.network.xml;

/**
 * @author ThinkDeep
 * @see <a href="https://merlin.fit.vutbr.cz/wiki-iot/index.php/Smarthome_cloud#Info_-_tabulky">err cods</a>
 */
public class FalseAnswer {

	private String mAdditionalInfo;
	private int mErrCode;
	private String mErrMessage;

	/**
	 * Constructor
	 */
	public FalseAnswer() {
	}

	/**
	 * Constructor
	 *  @param additionalInfo previous state where exception appear
	 * @param errCode        code of error
	 */
	public FalseAnswer(String additionalInfo, int errCode) {
		mAdditionalInfo = additionalInfo;
		mErrCode = errCode;
	}

	/**
	 * Getter
	 *
	 * @return
	 */
	public String getInfo() {
		return mAdditionalInfo;
	}

	/**
	 * @return the mErrCode
	 */
	public int getErrCode() {
		return mErrCode;
	}

	/**
	 * @param mErrCode the mErrCode to set
	 */
	public void setErrCode(int mErrCode) {
		this.mErrCode = mErrCode;
	}

	public String getErrMessage() {
		return mErrMessage;
	}

	public void setErrMessage(String mErrMessage) {
		this.mErrMessage = mErrMessage;
	}

}
