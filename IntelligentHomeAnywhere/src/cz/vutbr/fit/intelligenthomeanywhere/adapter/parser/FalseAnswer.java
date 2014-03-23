/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter.parser;

/**
 * @author ThinkDeep
 *
 */
public class FalseAnswer {

	private String mAdditionalInfo;
	
	public Object data;
	
	/**
	 * Constructor
	 */
	public FalseAnswer() {}
	
	/**
	 * Constructor
	 * @param additionalInfo previous state where exception appear
	 * @param data of the error message
	 */
	public FalseAnswer(String additionalInfo, Object data){
		mAdditionalInfo = additionalInfo;
		this.data = data;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getInfo(){
		return mAdditionalInfo;
	}

}
