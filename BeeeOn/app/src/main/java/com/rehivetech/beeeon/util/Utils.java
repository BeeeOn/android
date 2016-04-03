package com.rehivetech.beeeon.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

final public class Utils {
	private static final String TAG = Utils.class.getSimpleName();

	private static final int[] sGraphColors = new int[]{R.color.graph1, R.color.graph2, R.color.graph3, R.color.graph4, R.color.graph5,
			R.color.graph6, R.color.graph7, R.color.graph8, R.color.graph9, R.color.graph10, R.color.graph11, R.color.graph12,
			R.color.graph13, R.color.graph14, R.color.graph15, R.color.graph16, R.color.graph17, R.color.graph18,
			R.color.graph19, R.color.graph20};

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
		canvas.drawBitmap(scaleBitmapImage, new Rect(0, 0, scaleBitmapImage.getWidth(), scaleBitmapImage.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
		return targetBitmap;
	}

	/**
	 * Downloads image from URL address
	 * <p/>
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

	public static String getUtf8StringFromInputStream(InputStream stream) throws IOException {
		int n;
		char[] buffer = new char[1024 * 4];
		InputStreamReader reader = new InputStreamReader(stream, "UTF8");
		StringWriter writer = new StringWriter();
		while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
		return writer.toString();
	}

	/**
	 * Reads the response from the input stream and returns it as a string.
	 * <p/>
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
	 * <p/>
	 * This CAN'T be called on UI thread.
	 *
	 * @param requestUrl
	 * @param params
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONObject fetchJsonByPost(String requestUrl, Map<String, String> params) throws JSONException, IOException {
		URL url = new URL(requestUrl);

		String query = "";
		for (String key : params.keySet())
			query += key + "=" + params.get(key) + "&";

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		//connection.setRequestProperty("Cookie", cookie);
		//Set to POST
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setReadTimeout(10000);
		Writer writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(query);
		writer.flush();
		writer.close();

		return new JSONObject(getUtf8StringFromInputStream((InputStream) connection.getContent()));
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

	/**
	 * Checks if Google Play Services are available on this module.
	 * Automatically return false if this module is running BlackBerry.
	 *
	 * @param context
	 * @return true if available, false otherwise
	 */
	public static boolean isGooglePlayServicesAvailable(Context context) {
		if (isBlackBerry())
			return false;

		try {
			int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
			return resultCode == ConnectionResult.SUCCESS;
		} catch (Exception e) {
			// NOTE: Ignore exception (probably only class not found one), because we just want the true/false result
		}

		return false;
	}

	/**
	 * Checks if Internet connection is available.
	 *
	 * @param context
	 * @return true if available, false otherwise
	 */
	public static boolean isInternetAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Returns the type which actual network uses
	 *
	 * @param context
	 * @return ConnectivityManager#TYPE_xxxxxx
	 */
	public static int getNetworkConnectionType(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
			return -1; // the same as ConnectivityManager.TYPE_NONE which can't be used;
		}
		return activeNetworkInfo.getType();
	}

	/**
	 * Returns the consumer friendly module name.
	 * Taken from https://stackoverflow.com/questions/1995439/get-android-phone-model-programmatically/26117427#26117427
	 */
	public static String getPhoneName() {
		final String manufacturer = Build.MANUFACTURER;
		final String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		}
		if (manufacturer.equalsIgnoreCase("HTC")) {
			// make sure "HTC" is fully capitalized.
			return "HTC " + model;
		}
		return capitalize(manufacturer) + " " + model;
	}

	private static String capitalize(String str) {
		if (str.isEmpty()) {
			return str;
		}
		final char[] arr = str.toCharArray();
		boolean capitalizeNext = true;
		String phrase = "";
		for (final char c : arr) {
			if (capitalizeNext && Character.isLetter(c)) {
				phrase += Character.toUpperCase(c);
				capitalizeNext = false;
				continue;
			} else if (Character.isWhitespace(c)) {
				capitalizeNext = true;
			}
			phrase += c;
		}
		return phrase;
	}

	/**
	 * Helper for showing toasts from any thread.
	 *
	 * @param activity
	 * @param message
	 */
	public static void showToastOnUiThread(final Activity activity, final String message, final int duration) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activity, message, duration).show();
			}
		});
	}

	/**
	 * For getting index in array so that we can set selected object to spinner or etc.
	 *
	 * @param id      object Id
	 * @param objects list of objects with the same type
	 * @param <T>
	 * @return index or -1 if not found
	 */
	public static <T extends IIdentifier> int getObjectIndexFromList(String id, List<T> objects) {
		int index = 0;
		for (T tempObj : objects) {
			if (tempObj.getId().equals(id)) return index;
			index++;
		}
		return -1;
	}

	/**
	 * For getting objects from lists (Location / Device / etc)
	 *
	 * @param id
	 * @param objects
	 * @param <T>
	 * @return
	 */
	@Nullable
	public static <T extends IIdentifier> T getFromList(String id, List<T> objects) {
		if (id == null) {
			Log.i(TAG, "getFromList given NULL id");
			return null;
		}

		for (T tempObj : objects) {
			if (tempObj.getId().equals(id)) return tempObj;
		}
		return null;
	}

	/**
	 * Gets index and object from list of objects
	 *
	 * @param id
	 * @param objects
	 * @param <T>
	 * @return
	 */
	@Nullable
	public static <T extends IIdentifier> Pair<Integer, T> getIndexAndObjectFromList(String id, List<T> objects) {
		if (id == null) {
			Log.i(TAG, "getIndexAndObjectFromList given NULL id");
			return null;
		}
		int index = 0;
		for (T tempObj : objects) {
			if (tempObj.getId().equals(id)) {
				return new Pair<>(index, tempObj);
			}
			index++;
		}
		return null;
	}

	/**
	 * Converting array of Integer to array of primitive ints
	 *
	 * @param integers
	 * @return
	 */
	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next();
		}
		return ret;
	}

	public static <T extends Enum<T> & IIdentifier> T getEnumFromId(Class<T> enumClass, String id, T defaultItem) {
		for (T item : enumClass.getEnumConstants()) {
			if (item.getId().equalsIgnoreCase(id)) {
				return item;
			}
		}

		return defaultItem;
	}

	public static <T extends Enum<T> & IIdentifier> T getEnumFromId(Class<T> enumClass, String id) throws AppException {
		for (T item : enumClass.getEnumConstants()) {
			if (item.getId().equalsIgnoreCase(id)) {
				return item;
			}
		}

		throw new AppException(String.format("Unknown enum id '%s' for '%s'", id, enumClass.getSimpleName()), ClientError.UNEXPECTED_RESPONSE);
	}

	public static <T extends Enum<T> & INameIdentifier> T getEnumFromValue(Class<T> enumClass, String value, T defaultItem) {
		for (T item : enumClass.getEnumConstants()) {
			if (item.getName().equalsIgnoreCase(value)) {
				return item;
			}
		}

		return defaultItem;
	}

	/**
	 * Taken from https://gist.github.com/laaptu/7867851
	 */
	public static int convertPixelsToDp(float px) {
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return Math.round(dp);
	}

	/**
	 * Taken from https://gist.github.com/laaptu/7867851
	 */
	public static int convertDpToPixel(float dp) {
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return Math.round(px);
	}

	/**
	 * Set alpha channel of the given color.
	 *
	 * @param color
	 * @param alpha must be [0..255], otherwise color is undefined
	 * @return argb variant of given color and alpha
	 */
	public static int setColorAlpha(int color, int alpha) {
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}

	/**
	 * Enum for validation types
	 */
	@IntDef({INTEGER, DOUBLE, EMAIL, IP_ADDRESS})
	public @interface ValidationType {}
	public static final int INTEGER = 0;
	public static final int DOUBLE = 1;
	public static final int EMAIL = 2;
	public static final int IP_ADDRESS = 3;

	/**
	 * Helper function for validating TextInputLayout (EditText) input
	 *
	 * @param context of application (so that strings can be accessed)
	 * @param textInputLayout input layout for validation
	 * @param additional any additional information which can be checked
	 * @return success if input was validated correctly
	 */
	public static boolean validateInput(Context context, TextInputLayout textInputLayout, @ValidationType int... additional) {
		EditText editText = textInputLayout.getEditText();
		if (editText == null) return false;

		String inputText = editText.getText().toString().trim();
		if (inputText.length() == 0) {
			textInputLayout.requestFocus();
			textInputLayout.setError(context.getString(R.string.activity_utils_toast_field_must_be_filled));
			return false;
		}

		for (@ValidationType int type : additional) {
			switch (type) {
				case INTEGER:
					try {
						//noinspection ResultOfMethodCallIgnored
						Integer.parseInt(inputText);
					} catch (NumberFormatException e) {
						textInputLayout.requestFocus();
						textInputLayout.setError(context.getString(R.string.utils_toast_field_must_be_number));
						return false;
					}
					break;

				case DOUBLE:
					try {
						//noinspection ResultOfMethodCallIgnored
						Double.parseDouble(inputText);
					} catch (NumberFormatException e) {
						textInputLayout.requestFocus();
						textInputLayout.setError(context.getString(R.string.utils_toast_field_must_be_number));
						return false;
					}
					break;

				case EMAIL:
					if (!android.util.Patterns.EMAIL_ADDRESS.matcher(inputText).matches()) {
						textInputLayout.requestFocus();
						textInputLayout.setError(context.getString(R.string.utils_toast_field_must_be_email));
						return false;
					}
					break;

				case IP_ADDRESS:
					if(!Patterns.IP_ADDRESS.matcher(inputText).matches()){
						textInputLayout.requestFocus();
						textInputLayout.setError(context.getString(R.string.utils_toast_field_must_be_ip_address));
						return false;
					}
					break;
			}
		}

		return true;

	}

	@ColorInt
	public static int getGraphColor(Context context, int index) {
		if (index < sGraphColors.length) {
			return ContextCompat.getColor(context, sGraphColors[index]);
		}

		Random random = new Random(index);
		return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}

	public static int parseIntSafely(@Nullable String string, int defaultValue) {
		try {
			return (string != null && !string.isEmpty()) ? Integer.parseInt(string) : defaultValue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static double parseDoubleSafely(@Nullable String string, double defaultValue) {
		try {
			return (string != null && !string.isEmpty()) ? Double.parseDouble(string) : defaultValue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static int getToolbarHeight(Context context) {
		final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
				new int[]{R.attr.actionBarSize});
		int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();

		return toolbarHeight;
	}

	/**
	 * Safely gets units helper
	 * @param prefs shared preferences of logged user
	 * @param context existing! context of app
	 * @return helper or null
	 */
	public static @Nullable UnitsHelper getUnitsHelper(@Nullable SharedPreferences prefs, Context context){
		return (prefs == null) ? null : new UnitsHelper(prefs, context);
	}

	public static @Nullable UnitsHelper getUnitsHelper(Context context){
		Controller controller = Controller.getInstance(context);
		SharedPreferences prefs = controller.getUserSettings();
		return getUnitsHelper(prefs, context);
	}

	/**
	 * Safely gets time helper
	 * @param prefs shared preferences of logged user
	 * @return helper or null
	 */
	public static @Nullable TimeHelper getTimeHelper(@Nullable SharedPreferences prefs){
		return (prefs == null) ? null : new TimeHelper(prefs);
	}

	/**
	 * Create absolute module id string
	 * @param deviceId
	 * @param moduleId
	 * @return
	 */
	public static String getAbsoluteModuleId(String deviceId, String moduleId) {
		return String.format("%s---%s", deviceId, moduleId);
	}

	/**
	 * Parse absolute module id to device and module id
	 * @param absoluteModuleId
	 * @return array with device and module ids
	 */
	public static String[] parseAbsoluteModuleId(String absoluteModuleId) {
		return absoluteModuleId.split("---");
	}

	/**
	 * Change drawable color
	 * @param drawable drawable to be changed
	 * @param color color
	 * @return tinted drawable
	 */
	public static Drawable setDrawableTint(Drawable drawable, @ColorInt int color) {
		Drawable wrapedDrawable = DrawableCompat.wrap(drawable.mutate());
		DrawableCompat.setTint(wrapedDrawable, color);
		return wrapedDrawable;
	}

	@SuppressWarnings("deprecation")
	public static void setBackgroundImageDrawable(ImageView imageView, Drawable drawable) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			imageView.setBackground(drawable);
		} else {
			imageView.setBackgroundDrawable(drawable);
		}
	}
}
