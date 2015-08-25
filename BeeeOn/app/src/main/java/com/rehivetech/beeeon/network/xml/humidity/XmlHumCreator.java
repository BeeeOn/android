package com.rehivetech.beeeon.network.xml.humidity;

import android.util.Xml;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.network.xml.XmlCreator;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for creating XML request for humidity purposes
 * Created by ThinkDeep on 6.5.2015.
 */
public class XmlHumCreator extends XmlCreator{

	// states

	public static final String GETHUMIDITYOVERVIEW = "gethumoverview";
	public static final String GETHUMIDITYMESSAGEDETAIL = "gethummsgdetail";
	public static final String GETHUMIDITYLOCATIONDETAIL = "gethumlocdetail";
	public static final String GETHUMIDITYLOCATIONSTATISTICS = "gethumlocstats";
	public static final String SETHUMIDITYNOTIFICATIONS = "sethumnotifs";
	public static final String GETHUMIDITYNOTIFICATIONS = "gethumnotifs";
	public static final String SETHUMIDITYTHRESHOLD = "sethumthreshold";
	public static final String GETHUMIDITYTHRESHOLD = "gethumthreshold";

	// end of states

	private static final String APP = "app";
	private static final String HUMIDITY = "hum";

	///////////////////////////////

	protected static XmlSerializer beginXml(StringWriter writer) throws IOException {
		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", null);

		serializer.startTag(ns, Xconstants.COM_ROOT);
		serializer.attribute(ns, Xconstants.COM_VERSION, Constants.PROTOCOL_VERSION); // every time use version
		serializer.attribute(ns, APP, HUMIDITY); // every time use name of "humidity namespace"

		return serializer;
	}

	protected static String createComAttribsVariant(String... args) {
		if (0 != (args.length % 2)) { // odd
			throw new RuntimeException("Bad params count");
		}

		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = beginXml(writer);

			for (int i = 0; i < args.length; i += 2) { // take pair of args
				serializer.attribute(ns, args[i], args[i + 1]);
			}

			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	////////////////////////////////

	/**
	 * Method create xml request for getting humidity overview
	 * @param bt beeeon token
	 * @param aid adapter id
	 * @param date of start
	 * @param interval should be one of {day, week, month}
	 * @return xml with request
	 */
	public static String createGetHumidityOverview(String bt, String aid, String date, String interval){
		return createComAttribsVariant(Xconstants.COM_STATE, GETHUMIDITYOVERVIEW, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid, Xconstants.DATE, date, Xconstants.INTERVAL, interval);
	}

	/**
	 * Method create xml request for getting humidity message detail
	 * @param bt beeeon token
	 * @param aid adapter id
	 * @param mid message id
	 * @return xml with request
	 */
	public static String createGetHumidityMessageDetail(String bt, String aid, String mid){
		return createComAttribsVariant(Xconstants.COM_STATE, GETHUMIDITYMESSAGEDETAIL, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid, Xconstants.MSGID, mid);
	}

	/**
	 * Method crate xml request for getting humidity location detail
	 * @param bt beeeon token
	 * @param aid adapter id
	 * @param lid location id
	 * @param date of start
	 * @param interval should be one of {day, week, month}
	 * @return xml with request
	 */
	public static String createGetHumidityLocationDetail(String bt, String aid, String lid, String date, String interval){
		return createComAttribsVariant(Xconstants.COM_STATE, GETHUMIDITYLOCATIONDETAIL, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid, Xconstants.LID, lid, Xconstants.DATE, date, Xconstants.INTERVAL, interval);
	}

	/**
	 * Method create xml request for getting humidity location statistics
	 * @param bt beeeon token
	 * @param aid adapter id
	 * @param lid location id
	 * @param date of start
	 * @param interval should be one of {day, week, month}
	 * @return xml with request
	 */
	public static String createGetHumidityLocationStatistics(String bt, String aid, String lid, String date, String interval){
		return createComAttribsVariant(Xconstants.COM_STATE, GETHUMIDITYLOCATIONSTATISTICS, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid, Xconstants.LID, lid, Xconstants.DATE, date, Xconstants.INTERVAL, interval);
	}

	/**
	 * Method create xml request for setting humidity notification setting
	 * @param bt beeeon token
	 * @param aid adapter id
	 * @param notifs map of setting consists of type of notification as key and state (enabled=1, disabled=0) as value
	 * @return xml with request
	 */
	public static String createSetHumidityNotifications(String bt, String aid, HashMap<String, String> notifs){
		StringWriter writer = new StringWriter();
		if(notifs.size() < 1)
			throw new IllegalArgumentException("Expected more than zero notifications");

		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.COM_STATE, SETHUMIDITYNOTIFICATIONS);

			for(Map.Entry<String, String> entry : notifs.entrySet()){
				serializer.startTag(ns, Xconstants.NOTIFICATION);

				serializer.attribute(ns, Xconstants.TYPE, entry.getKey());
				serializer.attribute(ns, Xconstants.ENABLE, entry.getValue());

				serializer.endTag(ns, Xconstants.NOTIFICATION);
			}
			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create xml request for getting humidity notifications setting
	 * @param bt beeeon token
	 * @param aid adapter id
	 * @return xml with request
	 */
	public static String createGetHumidityNotificatons(String bt, String aid){
		return createComAttribsVariant(Xconstants.COM_STATE, GETHUMIDITYNOTIFICATIONS, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
	}

	/**
	 * Method create xml request for setting hummidity threshold
	 * @param bt beeeon token
	 * @param aid adapter id
	 *
	 *
	 * @return xml with request
	 */
	//FIXME: how will be the threshold data passed inside method?
	public static String createSetHumidityThreshold(String bt, String aid){
		StringWriter writer = new StringWriter();
//		if(location.size() < 1)
//			throw new IllegalArgumentException("Expected more than zero location");

		try {
			XmlSerializer serializer = beginXml(writer);

			serializer.attribute(ns, Xconstants.COM_SESSION_ID, bt);
			serializer.attribute(ns, Xconstants.COM_STATE, SETHUMIDITYTHRESHOLD);


			return endXml(writer, serializer);
		} catch (Exception e) {
			throw AppException.wrap(e, ClientError.XML);
		}
	}

	/**
	 * Method create xml request for getting humidity threshold
	 * @param bt beeeon token
	 * @param aid adapter id
	 * @return xml with request
	 */
	public static String createGetHumidityThreshold(String bt, String aid) {
		return createComAttribsVariant(Xconstants.COM_STATE, GETHUMIDITYTHRESHOLD, Xconstants.COM_SESSION_ID, bt, Xconstants.AID, aid);
	}

}
