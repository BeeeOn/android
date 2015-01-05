package com.rehivetech.beeeon.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;

final public class Utils {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private Utils() {
	}

	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
		int targetWidth = 200;
		int targetHeight = 200;

		if (scaleBitmapImage == null) {
			return null;
		}

		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle((targetWidth - 1) / 2, (targetHeight - 1) / 2, (Math.min((targetWidth), (targetHeight)) / 2), Path.Direction.CCW);

		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
		return targetBitmap;
	}
	
	/**
	 * Downloads image from URL address
	 * 
	 * This CAN'T be called on UI thread.
	 * 
	 * @param requestUrl
	 * @return Bitmap or null
	 */
	public static Bitmap fetchImageFromUrl(String requestUrl) {
		Bitmap bitmap = null;

		try {
			URL url = new URL(requestUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			try {
				InputStream in = con.getInputStream();
				bitmap = BitmapFactory.decodeStream(in); // Convert to bitmap
			} finally {
				con.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bitmap;
	}
	
	public static String getUtf8StringFromInputStream(InputStream stream) throws IOException
	{
	    int n = 0;
	    char[] buffer = new char[1024 * 4];
	    InputStreamReader reader = new InputStreamReader(stream, "UTF8");
	    StringWriter writer = new StringWriter();
	    while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
	    return writer.toString();
	}
	
	/**
	 * Reads the response from the input stream and returns it as a string.
	 * 
	 * This CAN'T be called on UI thread.
	 * 
	 * @param requestUrl
	 * @return content from url or empty string
	 */
	public static String fetchStringFromUrl(String requestUrl) {
		String data = "";

		try {
			URL url = new URL(requestUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			try {
				InputStream in = con.getInputStream();
				data = getUtf8StringFromInputStream(in);
			} finally {
				con.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}

	/**
	 * Fetch JSON content by a HTTP POST request defined by the requestUrl and params given
	 * as a map of (key, value) pairs. Encoding is solved internally.
	 * 
	 * This CAN'T be called on UI thread.
	 * 
	 * @param requestUrl
	 * @param params
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONObject fetchJsonByPost(String requestUrl, Map<String, String> params) throws JSONException, IOException {
		final HttpClient client = new DefaultHttpClient();
		final HttpPost post = new HttpPost(requestUrl);
		final List<NameValuePair> pairs = new ArrayList<NameValuePair>();

		for(String key : params.keySet())
			pairs.add(new BasicNameValuePair(key, params.get(key)));

		post.setEntity(new UrlEncodedFormEntity(pairs));
		final HttpResponse resp = client.execute(post);

		return new JSONObject(getUtf8StringFromInputStream(resp.getEntity().getContent()));
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
		}

		return 0;
	}

	/**
	 * Check if this is debug version of application.
	 * 
	 * @return true if version in Manifest contains "debug", false otherwise
	 */
	public static boolean isDebugVersion(Context context) {
		try {
			String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			return version.contains("debug");
		} catch (NameNotFoundException e) {
			// should never happen
		}

		return false;
	}

	public static String uriEncode(final String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			/* will never happen for UTF-8 */
			throw new RuntimeException("failed call encode with UTF-8");
		}
	}

	/**
	 * Formats double value to String without trailing zeros
	 * 
	 * @param d
	 * @return
	 */
	public static String formatDouble(double d) {
		// NOTE: This trick won't work for values that can't fit into long
		if (d == (long) d)
			return String.format(Locale.getDefault(), "%d", (long) d);
		else
			return String.format(Locale.getDefault(), "%.2f", d);
	}

	public static boolean isBlackBerry() {
		final String osName = System.getProperty("os.name");
		return "qnx".equals(osName);
	}

}
