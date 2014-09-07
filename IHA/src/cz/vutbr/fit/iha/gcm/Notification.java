/**
 * 
 */
package cz.vutbr.fit.iha.gcm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

/**
 * @author ThinkDeep
 *
 */
public class Notification {
	
	public static final String TAG = Notification.class.getSimpleName();
	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String SEPARATOR = "\\s+";
	
	private SimpleDateFormat mFormatter = new SimpleDateFormat(FORMAT, Locale.getDefault());

	private final Date mDate;
	private final String mMsgid;
	private boolean mRead;
	private final NotificationType mType;
	private Action mAction;
	private String mMessage;
	
	public class Action{
		// web or app
		private final ActionType mMasterType;
		// app sub types
		private ActionType mSlaveType = ActionType.NONE;
		// if web
		private String mURL;
		// if app + adapter
		private String mAdapterId;
		// if app + adapter + location
		private String mLocationId;
		// if app + adapter + device
		private String mDeviceId;
		
		public Action(String masterType){
			mMasterType = ActionType.fromValue(masterType);
		}

		/**
		 * @return the mMasterType
		 */
		public ActionType getMasterType() {
			return mMasterType;
		}

		/**
		 * @return the mSlaveType
		 */
		public ActionType getSlaveType() {
			return mSlaveType;
		}

		/**
		 * @param mSlaveType the mSlaveType to set
		 */
		public void setSlaveType(ActionType SlaveType) {
			this.mSlaveType = SlaveType;
		}

		/**
		 * @return the mURL
		 */
		public String getURL() {
			return mURL;
		}

		/**
		 * @param mURL the mURL to set
		 */
		public void setURL(String URL) {
			this.mURL = URL;
		}

		/**
		 * @return the mAdapterId
		 */
		public String getAdapterId() {
			return mAdapterId;
		}

		/**
		 * @param mAdapterId the mAdapterId to set
		 */
		public void setAdapterId(String AdapterId) {
			this.mAdapterId = AdapterId;
		}

		/**
		 * @return the mLocationId
		 */
		public String getLocationId() {
			return mLocationId;
		}

		/**
		 * @param mLocationId the mLocationId to set
		 */
		public void setLocationId(String LocationId) {
			this.mLocationId = LocationId;
		}

		/**
		 * @return the mDeviceId
		 */
		public String getDeviceId() {
			return mDeviceId;
		}

		/**
		 * @param mDeviceId the mDeviceId to set
		 */
		public void setDeviceId(String DeviceId) {
			this.mDeviceId = DeviceId;
		}
		
	}
	
	public enum ActionType{
		WEB("web"),
		APP("app"),
		NONE("none"),
		SETTINGS("settings"),
		SETTINGSMAIN("settingsmain"),
		SETTINGSACCOUNT("settingsaccount"),
		SETTINGSADAPTER("settingsadapter"),
		SETTINGSLOCATION("settingslocation"),
		OPENADAPTER("adapter"),
		OPENLOCATION("location"),
		OPENDEVICE("device");
		
		
		private final String mValue;
		
		ActionType(String value){
			mValue = value;
		}
		
		public String getValue() {
			return mValue;
		}
		
		public static ActionType fromValue(String value) {
			for (ActionType item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}
	}
	
	public enum NotificationType{
		INFO("info"),
		ADVERT("advert"),
		ALERT("alert"),
		CONTROL("control");
		
		private final String mValue;
		
		NotificationType(String value){
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}
		
		public static NotificationType fromValue(String value) {
			for (NotificationType item : values()) {
				if (value.equalsIgnoreCase(item.getValue()))
					return item;
			}
			throw new IllegalArgumentException("Invalid State value");
		}
	}
	
	
	/**
	 * Constructor
	 */
	public Notification(String msgid, String time, String type, boolean read){
		mMsgid = msgid;
		String[] parts = time.split(SEPARATOR);
		
		if (parts.length != 2) {
			Log.e(TAG, String.format("Wrong number of parts (%d) of data: %s", parts.length, time));
			throw new IllegalArgumentException();
		}
		
		try { //TODO: check this
			mDate = mFormatter.parse(String.format("%s %s", parts[0], parts[1]));
		} catch (ParseException e) {
			Log.e(TAG, String.format("Wrong date format: %s", parts[0]));
			throw new IllegalArgumentException(e);
		} catch (NumberFormatException e) {
			Log.e(TAG, String.format("Wrong value format: %s", parts[1]));
			throw new IllegalArgumentException(e);
		}
		
		mType = NotificationType.fromValue(type);
		mRead = read;
	}

	/**
	 * @return the mDate
	 */
	public Date getDate() {
		return mDate;
	}

	/**
	 * @return the mMsgid
	 */
	public String getMsgid() {
		return mMsgid;
	}

	/**
	 * @return the mRead
	 */
	public boolean isRead() {
		return mRead;
	}

	/**
	 * @param mRead the mRead to set
	 */
	public void setRead(boolean Read) {
		this.mRead = Read;
	}

	/**
	 * @return the mType
	 */
	public NotificationType getType() {
		return mType;
	}

	/**
	 * @return the mAction
	 */
	public Action getAction() {
		return mAction;
	}

	/**
	 * @param mAction the mAction to set
	 */
	public void setAction(Action Action) {
		this.mAction = Action;
	}

	/**
	 * @return the mMessage
	 */
	public String getMessage() {
		return mMessage;
	}

	/**
	 * @param mMessage the mMessage to set
	 */
	public void setMessage(String Message) {
		this.mMessage = Message;
	}

}
