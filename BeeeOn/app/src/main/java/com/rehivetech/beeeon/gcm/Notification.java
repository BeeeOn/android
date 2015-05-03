/**
 *
 */
package com.rehivetech.beeeon.gcm;

import android.os.Bundle;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;

/**
 * @author ThinkDeep
 */
public class Notification {

	public static final String TAG = Notification.class.getSimpleName();
	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	// private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String SEPARATOR = "\\s+";
	private final Calendar mDate;
	private final String mMsgid;
	private final NotificationType mType;
	private DateTimeFormatter mFormatter = DateTimeFormat.forPattern(DATEFORMAT).withZoneUTC();
	private boolean mRead;
	private Action mAction;
	private String mUserId = null;
	private String mMessage;

	/**
	 * Constructor
	 */
	public Notification(String msgid, String time, String type, boolean read) throws IllegalArgumentException {
		mMsgid = msgid;
		// String[] parts = time.split(SEPARATOR);

		// if (parts.length != 2) {
		// Log.e(TAG, String.format("Wrong number of parts (%d) of data: %s", parts.length, time));
		// throw new IllegalArgumentException();
		// }

		/** FIXME opravit timezone */
		mDate = Calendar.getInstance();
		try {
			mDate.setTimeInMillis(Long.valueOf(time));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}

		mType = Utils.getEnumFromId(NotificationType.class, type);
		mRead = read;
	}

	protected static Notification parseBundle(Bundle bundle) {
		String msgid = bundle.getString(Xconstants.MSGID);
		// FIXME po predelani na nove notifikace s novym tagem pro userId predelat tag (prozatim se to posila v tagu)
		String userId = bundle.getString(Xconstants.EMAIL);
		String time = bundle.getString(Xconstants.TIME);
		String type = bundle.getString(Xconstants.TYPE);
		String message = bundle.getString(Xconstants.MESSAGE);
		String action = bundle.getString(Xconstants.ACTION);

		// control validity of message
		if (msgid == null || userId == null || time == null || type == null || action == null || message == null) {
			return null;
		}

		Notification notification = new Notification(msgid, time, type, false);
		// we need email in notification to check validity
		notification.setUserId(userId);
		notification.setMessage(message);
		notification.setAction(notification.new Action(action));

		return notification;
	}

	/**
	 * @return Email if notification was received by GCM. Null otherwise.
	 */
	public String getUserId() {
		return mUserId;
	}

	public void setUserId(String userId) {
		mUserId = userId;
	}

	/**
	 * @return the mDate
	 */
	public Calendar getDate() {
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

	public enum ActionType implements IIdentifier {
		WEB("web"), //
		APP("app"), //
		NONE("none"), //
		SETTINGS("sett"), //
		SETTINGSMAIN("settmain"), //
		SETTINGSACCOUNT("settaccount"), //
		SETTINGSADAPTER("settadapter"), //
		SETTINGSLOCATION("settlocation"), //
		OPENADAPTER("adapter"), //
		OPENLOCATION("location"), //
		OPENDEVICE("device");

		private final String mValue;

		ActionType(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
		}
	}

	public enum NotificationType implements IIdentifier {
		INFO("info"),
		ADVERT("advert"),
		ALERT("alert"),
		CONTROL("control");

		private final String mValue;

		NotificationType(String value) {
			mValue = value;
		}

		public String getId() {
			return mValue;
		}
	}

	public class Action {
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

		public Action(String masterType) {
			mMasterType = Utils.getEnumFromId(ActionType.class, masterType);
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

}
