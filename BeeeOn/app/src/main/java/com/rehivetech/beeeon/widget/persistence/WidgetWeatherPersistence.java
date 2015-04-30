package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

/**
 * Created by Tomáš on 26. 4. 2015.
 */
public class WidgetWeatherPersistence extends WidgetPersistence {

	public static final int DEFAULT_WEATHER_ICON = R.string.weather_sunny;
	public static final String TAG = WidgetWeatherPersistence.class.getSimpleName();

	private static final String PREF_CITY_NAME = "city_name";
	private static final String PREF_TEMPERATURE = "temperature";
	private static final String PREF_ICON_RESOURCE = "icon_resource";

	private static Typeface sWeatherFont;
	private Resources mResources;

	// persistent data
	public String cityName;
	public String temperature;
	public int iconResource = DEFAULT_WEATHER_ICON;

	// object data
	public Bitmap generatedIcon;
	private int oldIconResource;
	private final Rect mIconBounds = new Rect();
	private int oldIconSize;

	public WidgetWeatherPersistence(Context context, int widgetId, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, 0, 0, unitsHelper, timeHelper, settings);

		if(sWeatherFont == null){
			sWeatherFont = Typeface.createFromAsset(context.getAssets(), "weather_icons.ttf");
		}

		mResources = mContext.getResources();
	}


	/**
	 * If needed, creates new bitmap of weather
	 */
	public Bitmap getBitmapIcon(boolean forceReload, int size){
		if(generatedIcon == null || forceReload || oldIconResource != iconResource || oldIconSize != size) {
			Log.v(TAG, "Generating new weather icon");

			Bitmap iconBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
			Canvas myCanvas = new Canvas(iconBitmap);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setSubpixelText(true);
			paint.setTypeface(sWeatherFont);
			paint.setStyle(Paint.Style.FILL);
			// makes text size little bit smaller then size of the canvas
			paint.setTextSize(size - ((float) 0.17 * size));
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setColor(mResources.getColor(mWidgetSettings.colorPrimary));

			String iconText = mContext.getString(iconResource);
			paint.getTextBounds(iconText, 0, iconText.length(), mIconBounds);
			myCanvas.drawText(iconText, (size / 2), (size / 2) - mIconBounds.exactCenterY(), paint);

			generatedIcon = iconBitmap;
			oldIconResource = iconResource;
		}
		return generatedIcon;
	}

	/**
	 * Parse widget icon based on id which were get from the server
	 * @param actualId
	 * @param sunrise
	 * @param sunset
	 * @return string icon resource
	 */
	public static int parseWeatherIconResource(int actualId, long sunrise, long sunset){
		int iconRes;

		// if its exactly 800, its clear sky
		if(actualId == 800){
			long currentTime = new Date().getTime();
			if(currentTime >= sunrise && currentTime < sunset) {
				iconRes = R.string.weather_sunny;
			} else {
				iconRes = R.string.weather_clear_night;
			}

			return iconRes;
		}
		// else we simplify that to some groups
		int id = actualId / 100;
		switch(id) {
			case 2 :
				iconRes = R.string.weather_thunder;
				break;
			case 3 :
				iconRes = R.string.weather_drizzle;
				break;
			case 5 :
				iconRes = R.string.weather_rainy;
				break;
			case 6 :
				iconRes = R.string.weather_snowy;
				break;
			case 7 :
				iconRes = R.string.weather_foggy;
				break;
			case 8 :
				iconRes = R.string.weather_cloudy;
				break;

			// default is nice day
			default:
				iconRes = WidgetWeatherPersistence.DEFAULT_WEATHER_ICON;
				break;
		}

		return iconRes;
	}

	@Override
	public String getPropertyPrefix() {
		return "weather";
	}

	@Override
	public void load() {
		cityName = mPrefs.getString(getProperty(PREF_CITY_NAME), "");
		temperature = mPrefs.getString(getProperty(PREF_TEMPERATURE), "");
		iconResource = mPrefs.getInt(getProperty(PREF_ICON_RESOURCE), DEFAULT_WEATHER_ICON);
	}

	@Override
	public void save() {
		mPrefs.edit()
				.putString(getProperty(PREF_CITY_NAME), cityName)
				.putString(getProperty(PREF_TEMPERATURE), temperature)
				.putInt(getProperty(PREF_ICON_RESOURCE), iconResource)
				.apply();
	}

	@Override
	public void delete() {
		mPrefs.edit()
				.remove(getProperty(PREF_CITY_NAME))
				.remove(getProperty(PREF_TEMPERATURE))
				.remove(getProperty(PREF_ICON_RESOURCE))
				.apply();
	}

	@Override
	public void configure(Object obj1, Object obj2){
		JSONObject json = (JSONObject) obj1;
		if(json == null) return;
		try {
			JSONObject details = json.getJSONArray("weather").getJSONObject(0);
			JSONObject main = json.getJSONObject("main");

			//DateFormat df = DateFormat.getDateTimeInstance();
			//String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
			cityName = json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country");

			iconResource = WidgetWeatherPersistence.parseWeatherIconResource(details.getInt("id"),
					json.getJSONObject("sys").getLong("sunrise") * 1000,
					json.getJSONObject("sys").getLong("sunset") * 1000);

			// TODO ziskat a vypsat hodnotu podle unitshelperu
			temperature = String.format("%d %s", (long) main.getDouble("temp"), "℃");

			// generates new bitmap but only IF NEEDED
			getBitmapIcon(false, (int) mContext.getResources().getDimension(R.dimen.widget_weather_icon));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
