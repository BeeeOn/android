package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.ViewsBuilder;

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

	public String cityName;
	public String temperature;
	public int iconResource = DEFAULT_WEATHER_ICON;
	public Bitmap generatedIcon;
	private int oldIconResource;

	public WidgetWeatherPersistence(Context context, int widgetId, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, 0, 0, unitsHelper, timeHelper, settings);

		if(sWeatherFont == null){
			sWeatherFont = Typeface.createFromAsset(context.getAssets(), "weather_icons.ttf");
		}

		generateBitmapIcon();
	}

	/**
	 * If needed, creates new bitmap of weather
	 */
	public void generateBitmapIcon(){
		if(generatedIcon != null && oldIconResource == iconResource) return;
		Log.v(TAG, "Generating new weather icon");

		Bitmap iconBitmap = Bitmap.createBitmap(160, 84, Bitmap.Config.ARGB_4444);
		Canvas myCanvas = new Canvas(iconBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setTypeface(sWeatherFont);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		paint.setTextSize(65);
		paint.setTextAlign(Paint.Align.CENTER);
		myCanvas.drawText(mContext.getString(iconResource), 80, 60, paint);

		generatedIcon = iconBitmap;
		oldIconResource = iconResource;
	}

	/**
	 * Parse widget icon based on id which were get from the server
	 * @param actualId
	 * @param sunrise
	 * @param sunset
	 * @return string icon resource
	 */
	public static int parseWeatherIconResource(int actualId, long sunrise, long sunset){
		int id = actualId / 100;
		int iconRes;
		if(actualId == 800){
			long currentTime = new Date().getTime();
			if(currentTime >= sunrise && currentTime<sunset) {
				iconRes = R.string.weather_sunny;
			} else {
				iconRes = R.string.weather_clear_night;
			}
		} else {
			switch(id) {
				case 2 :
					iconRes = R.string.weather_thunder;
					break;
				case 3 :
					iconRes = R.string.weather_drizzle;
					break;
				case 7 :
					iconRes = R.string.weather_foggy;
					break;
				case 8 :
					iconRes = R.string.weather_cloudy;
					break;
				case 6 :
					iconRes = R.string.weather_snowy;
					break;
				case 5 :
					iconRes = R.string.weather_rainy;
					break;

				// default is nice day
				default:
					iconRes = WidgetWeatherPersistence.DEFAULT_WEATHER_ICON;
					break;
			}
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
			temperature = main.getDouble("temp") + " ℃";

			// generates new bitmap but only IF NEEDED
			generateBitmapIcon();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initView() {
		return;
	}

	@Override
	public void renderView(ViewsBuilder parentBuilder) {
		super.renderView(parentBuilder);
		parentBuilder.setTextViewText(R.id.widget_weather_city, cityName);
		parentBuilder.setTextViewText(R.id.widget_weather_temperature, temperature);
		parentBuilder.setImage(R.id.widget_weather_icon, generatedIcon);
	}
}
