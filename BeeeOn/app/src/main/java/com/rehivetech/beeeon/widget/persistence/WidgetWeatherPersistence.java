package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.HumidityValue;
import com.rehivetech.beeeon.household.device.values.PressureValue;
import com.rehivetech.beeeon.household.device.values.TemperatureValue;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.service.WeatherProvider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tomáš on 26. 4. 2015.
 */
public class WidgetWeatherPersistence extends WidgetPersistence implements IIdentifier {

	public static final int DEFAULT_WEATHER_ICON = R.string.weather_sunny;
	public static final String TAG = WidgetWeatherPersistence.class.getSimpleName();

	private static final String PREF_ID = "id";
	private static final String PREF_TIMESTAMP = "timestamp";
	private static final String PREF_CITY_NAME = "city_name";
	private static final String PREF_COUNTRY = "country";
	private static final String PREF_TEMPERATURE = "temperature";
	private static final String PREF_TEMPERATURE_UNIT = "temperature_unit";
	private static final String PREF_ICON_RESOURCE = "icon_resource";
	private static final String PREF_HUMIDITY = "w_humidity";
	private static final String PREF_PRESSURE = "w_pressure";

	private static final String DEFAULT_TEMPERATURE_UNIT = "°C";
	private static final String DEFAULT_HUMIDITY_UNIT = "%";
	private static final String DEFAULT_PRESSURE_UNIT = "hPa";

	private static Typeface sWeatherFont;
	private Resources mResources;

	// persistent data
	public String id;
	public String cityName;
	public String country;
	public int iconResource = DEFAULT_WEATHER_ICON;
	public long temperature;
	public String temperatureUnit;
	private long timestamp;
	private int humidity;
	private int pressure;

	// object data
	public Bitmap generatedIcon;
	private int oldIconResource;
	private final Rect mIconBounds = new Rect();
	private int oldIconSize;
	private TemperatureValue mTemperatureValue;
	private HumidityValue mHumidityValue;
	private PressureValue mPressureValue;

	public WidgetWeatherPersistence(Context context, int widgetId, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		super(context, widgetId, 0, 0, unitsHelper, timeHelper, settings);

		if(sWeatherFont == null){
			sWeatherFont = Typeface.createFromAsset(context.getAssets(), "weather_icons.ttf");
		}

		mTemperatureValue = new TemperatureValue();
		mHumidityValue = new HumidityValue();
		mPressureValue = new PressureValue();

		mResources = mContext.getResources();
	}

	@Override
	public void load() {
		id = mPrefs.getString(getProperty(PREF_ID), "");
		cityName = mPrefs.getString(getProperty(PREF_CITY_NAME), "");
		iconResource = mPrefs.getInt(getProperty(PREF_ICON_RESOURCE), DEFAULT_WEATHER_ICON);
		country = mPrefs.getString(getProperty(PREF_COUNTRY), "");

		// loads persist temperature + set object temperatur
		temperature = mPrefs.getLong(getProperty(PREF_TEMPERATURE), 0);
		temperatureUnit = mPrefs.getString(getProperty(PREF_TEMPERATURE_UNIT), mUnitsHelper != null ? mUnitsHelper.getStringUnit(mTemperatureValue) : DEFAULT_TEMPERATURE_UNIT);
		mTemperatureValue.setValue(String.valueOf(temperature));

		// loads persist humidity + set object humidity
		humidity = mPrefs.getInt(getProperty(PREF_HUMIDITY), 0);
		mHumidityValue.setValue(String.valueOf(humidity));

		pressure = mPrefs.getInt(getProperty(PREF_PRESSURE), 0);
		mPressureValue.setValue(String.valueOf(pressure));

		timestamp = mPrefs.getLong(getProperty(PREF_TIMESTAMP), 0);
	}

	@Override
	public void save() {
		mPrefs.edit()
				.putString(getProperty(PREF_ID), id)
				.putString(getProperty(PREF_CITY_NAME), cityName)
				.putString(getProperty(PREF_COUNTRY), country)
				.putLong(getProperty(PREF_TEMPERATURE), temperature)
				.putInt(getProperty(PREF_ICON_RESOURCE), iconResource)
				.putString(getProperty(PREF_TEMPERATURE_UNIT), temperatureUnit)
				.putInt(getProperty(PREF_HUMIDITY), humidity)
				.putInt(getProperty(PREF_PRESSURE), pressure)
				.putLong(getProperty(PREF_TIMESTAMP), timestamp)
				.apply();
	}

	@Override
	public void delete() {
		mPrefs.edit()
				.remove(getProperty(PREF_ID))
				.remove(getProperty(PREF_CITY_NAME))
				.remove(getProperty(PREF_COUNTRY))
				.remove(getProperty(PREF_TEMPERATURE))
				.remove(getProperty(PREF_ICON_RESOURCE))
				.remove(getProperty(PREF_TEMPERATURE_UNIT))
				.remove(getProperty(PREF_HUMIDITY))
				.remove(getProperty(PREF_PRESSURE))
				.remove(getProperty(PREF_TIMESTAMP))
				.apply();
	}

	@Override
	public void configure(Object obj1, Object obj2){
		JSONObject json = (JSONObject) obj1;
		if(json == null) return;
		try {
			JSONObject jsonWeather = json.getJSONArray("weather").getJSONObject(0);
			JSONObject main = json.getJSONObject("main");

			id = json.getString("id");
			cityName = json.getString("name") + ", " + json.getJSONObject("sys").getString("country");

			iconResource = WeatherProvider.parseWeatherIconResource(
					jsonWeather.getInt("id"),
					json.getJSONObject("sys").getLong("sunrise") * 1000,
					json.getJSONObject("sys").getLong("sunset") * 1000);

			temperature = (long) main.getDouble("temp");
			temperatureUnit = DEFAULT_TEMPERATURE_UNIT;
			mTemperatureValue.setValue(String.valueOf(temperature));
			
			humidity = main.getInt("humidity");
			pressure = (int) main.getDouble("pressure");
			timestamp = json.getLong("dt");

			// generates new bitmap but only IF NEEDED
			getBitmapIcon(false, (int) mContext.getResources().getDimension(R.dimen.widget_weather_icon));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------- //
	// ---------------------- GETTERS ---------------------------- //
	// ----------------------------------------------------------- //

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
			oldIconSize = size;
		}
		return generatedIcon;
	}


	public String getTemperature(){
		if(mUnitsHelper != null){
			return  mUnitsHelper.getStringValueUnit(mTemperatureValue);
		}
		else{
			return String.format("%d%s", temperature, temperatureUnit);
		}
	}

	public String getHumidity(){
		if(mUnitsHelper != null){
			return  mUnitsHelper.getStringValueUnit(mHumidityValue);
		}
		else{
			return String.format("%d%s", humidity, DEFAULT_HUMIDITY_UNIT);
		}
	}

	public String getPressure() {
		if(mUnitsHelper != null){
			return  mUnitsHelper.getStringValueUnit(mPressureValue);
		}
		else{
			return String.format("%d%s", pressure, DEFAULT_PRESSURE_UNIT);
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getPropertyPrefix() {
		return "weather";
	}
}
