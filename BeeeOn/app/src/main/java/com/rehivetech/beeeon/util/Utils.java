package com.rehivetech.beeeon.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rehivetech.beeeon.BeeeOnApplication;
import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.exception.ClientError;
import com.rehivetech.beeeon.gui.view.CircleTransformation;
import com.squareup.picasso.Transformation;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import io.realm.Realm;
import timber.log.Timber;


final public class Utils {

	private static final int[] sGraphColors = new int[]{R.color.graph1, R.color.graph2, R.color.graph3, R.color.graph4, R.color.graph5,
			R.color.graph6, R.color.graph7, R.color.graph8, R.color.graph9, R.color.graph10, R.color.graph11, R.color.graph12,
			R.color.graph13, R.color.graph14, R.color.graph15, R.color.graph16, R.color.graph17, R.color.graph18,
			R.color.graph19, R.color.graph20};

	private static Transformation sCircleTransformation = new CircleTransformation();


	/**
	 * Private constructor to avoid instantiation.
	 */
	private Utils() {
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch(NameNotFoundException e) {
			// should never happen
		}

		return 0;
	}


	public static String uriEncode(final String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch(UnsupportedEncodingException e) {
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
		if(d == (long) d)
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
		if(isBlackBerry())
			return false;

		try {
			int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
			return resultCode == ConnectionResult.SUCCESS;
		} catch(Exception e) {
			// NOTE: Ignore exception (probably only class not found one), because we just want the true/false result
		}

		return false;
	}


	/**
	 * Checks if Internet connection is available.
	 *
	 * @return true if available, false otherwise
	 */
	public static boolean isInternetAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) BeeeOnApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}


	/**
	 * Returns the type which actual network uses
	 *
	 * @return ConnectivityManager#TYPE_xxxxxx
	 */
	public static int getNetworkConnectionType() {
		ConnectivityManager connectivityManager = (ConnectivityManager) BeeeOnApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if(activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
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
		if(model.startsWith(manufacturer)) {
			return capitalize(model);
		}
		if(manufacturer.equalsIgnoreCase("HTC")) {
			// make sure "HTC" is fully capitalized.
			return "HTC " + model;
		}
		return capitalize(manufacturer) + " " + model;
	}


	private static String capitalize(String str) {
		if(str.isEmpty()) {
			return str;
		}
		final char[] arr = str.toCharArray();
		boolean capitalizeNext = true;
		String phrase = "";
		for(final char c : arr) {
			if(capitalizeNext && Character.isLetter(c)) {
				phrase += Character.toUpperCase(c);
				capitalizeNext = false;
				continue;
			} else if(Character.isWhitespace(c)) {
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
	public static <T extends IIdentifier> int getObjectIndexFromList(Object id, List<T> objects) {
		int index = 0;
		for(T tempObj : objects) {
			if(tempObj.getId().equals(id)) return index;
			index++;
		}
		return -1;
	}


	/**
	 * Somehow manage to merge it with other function for gettin index
	 *
	 * @param id
	 * @param objects
	 * @param <T>
	 * @return
	 */
	public static <T extends com.rehivetech.beeeon.model.entity.IIdentifier> int getIndexFromList(Object id, List<T> objects) {
		int index = 0;
		for(T tempObj : objects) {
			if(tempObj.getId().equals(id)) return index;
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
		if(id == null) {
			Timber.i("getFromList given NULL id");
			return null;
		}

		for(T tempObj : objects) {
			if(tempObj.getId().equals(id)) return tempObj;
		}
		return null;
	}


	/**
	 * Temporary auto increment helper for realm database
	 *
	 * @param realm instance
	 * @param clazz for which will be generating max id
	 * @return id number
	 */
	public synchronized static long autoIncrement(Realm realm, Class clazz) {
		Number currentMax = realm.where(clazz).max("id"); // TODO "id" should have only identifiers or sth
		long nextId = 1;
		if(currentMax != null) {
			nextId = currentMax.longValue() + 1;
		}

		return nextId;
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
		if(id == null) {
			Timber.i("getIndexAndObjectFromList given NULL id");
			return null;
		}
		int index = 0;
		for(T tempObj : objects) {
			if(tempObj.getId().equals(id)) {
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
		for(int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next();
		}
		return ret;
	}


	public static <T extends Enum<T> & IIdentifier> T getEnumFromId(Class<T> enumClass, String id, T defaultItem) {
		for(T item : enumClass.getEnumConstants()) {
			if(item.getId().equalsIgnoreCase(id)) {
				return item;
			}
		}

		return defaultItem;
	}


	public static <T extends Enum<T> & IIdentifier> T getEnumFromId(Class<T> enumClass, String id) throws AppException {
		for(T item : enumClass.getEnumConstants()) {
			if(item.getId().equalsIgnoreCase(id)) {
				return item;
			}
		}

		throw new AppException(String.format("Unknown enum id '%s' for '%s'", id, enumClass.getSimpleName()), ClientError.UNEXPECTED_RESPONSE);
	}


	public static <T extends Enum<T> & INameIdentifier> T getEnumFromValue(Class<T> enumClass, String value, T defaultItem) {
		for(T item : enumClass.getEnumConstants()) {
			if(item.getName().equalsIgnoreCase(value)) {
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


	@ColorInt
	public static int getGraphColor(Context context, int index) {
		if(index < sGraphColors.length) {
			return ContextCompat.getColor(context, sGraphColors[index]);
		}

		Random random = new Random(index);
		return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}


	public static int parseIntSafely(@Nullable String string, int defaultValue) {
		try {
			return (string != null && !string.isEmpty()) ? Integer.parseInt(string) : defaultValue;
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}


	public static double parseDoubleSafely(@Nullable String string, double defaultValue) {
		try {
			return (string != null && !string.isEmpty()) ? Double.parseDouble(string) : defaultValue;
		} catch(NumberFormatException e) {
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
	 *
	 * @param prefs   shared preferences of logged user
	 * @param context existing! context of app
	 * @return helper or null
	 */
	public static
	@Nullable
	UnitsHelper getUnitsHelper(@Nullable SharedPreferences prefs, Context context) {
		return (prefs == null) ? null : new UnitsHelper(prefs, context);
	}


	public static
	@Nullable
	UnitsHelper getUnitsHelper(Context context) {
		Controller controller = Controller.getInstance(context);
		SharedPreferences prefs = controller.getUserSettings();
		return getUnitsHelper(prefs, context);
	}


	/**
	 * Safely gets time helper
	 *
	 * @param prefs shared preferences of logged user
	 * @return helper or null
	 */
	public static
	@Nullable
	TimeHelper getTimeHelper(@Nullable SharedPreferences prefs) {
		return (prefs == null) ? null : new TimeHelper(prefs);
	}


	/**
	 * Create absolute module id string
	 *
	 * @param deviceId
	 * @param moduleId
	 * @return
	 */
	public static String getAbsoluteModuleId(String deviceId, String moduleId) {
		return String.format("%s---%s", deviceId, moduleId);
	}


	/**
	 * Parse absolute module id to device and module id
	 *
	 * @param absoluteModuleId
	 * @return array with device and module ids
	 */
	public static String[] parseAbsoluteModuleId(String absoluteModuleId) {
		return absoluteModuleId.split("---");
	}


	/**
	 * Change drawable color
	 *
	 * @param drawable drawable to be changed
	 * @param color    color
	 * @return tinted drawable
	 */
	public static Drawable setDrawableTint(Drawable drawable, @ColorInt int color) {
		Drawable wrapedDrawable = DrawableCompat.wrap(drawable.mutate());
		DrawableCompat.setTint(wrapedDrawable, color);
		return wrapedDrawable;
	}


	@SuppressWarnings("deprecation")
	public static void setBackgroundImageDrawable(ImageView imageView, Drawable drawable) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			imageView.setBackground(drawable);
		} else {
			imageView.setBackgroundDrawable(drawable);
		}
	}


	@Nullable
	public static String convertInputStreamToString(InputStream stream) {
		Scanner s = new Scanner(stream).useDelimiter("\\A");
		if(!s.hasNext()) return null;

		return s.next();
	}


	public static Transformation getCircleTransformation() {
		return sCircleTransformation;
	}
}
