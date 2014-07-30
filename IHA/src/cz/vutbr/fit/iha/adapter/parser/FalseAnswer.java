/**
 * 
 */
package cz.vutbr.fit.iha.adapter.parser;


/**
 * 
 * @author ThinkDeep
 * @see <a href="https://merlin.fit.vutbr.cz/wiki-iot/index.php/Smarthome_cloud#Info_-_tabulky">err cods</a>
 */
public class FalseAnswer {

	private String mAdditionalInfo;
	private int mErrCode;
	private String mErrMessage;
	
	public Object troubleMakers;
	public static final String START_TAG = "<start>";
	public static final String END_TAG = "</start>";
	
	/**
	 * Constructor
	 */
	public FalseAnswer() {}
	
	/**
	 * Constructor
	 * @param additionalInfo previous state where exception appear
	 * @param data of the error message
	 */
	public FalseAnswer(String additionalInfo, int errCode, String errMessage){
		mAdditionalInfo = additionalInfo;
		mErrCode = errCode;
		try{
			switch(errCode){
				case 6:
					troubleMakers = XmlParsers.getFalseMessage6(START_TAG + errMessage + END_TAG);
					break;
				case 13:
					troubleMakers = XmlParsers.getFalseMessage6(START_TAG + errMessage + END_TAG);
					break;
				default:
					mErrMessage = errMessage;
					break;
			}
		}catch(Exception e){
			e.printStackTrace();
			mErrMessage = errMessage;
		}
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getInfo(){
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
