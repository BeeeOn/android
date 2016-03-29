package com.rehivetech.beeeon.model.weather;

import com.google.gson.annotations.SerializedName;
import com.rehivetech.beeeon.IIdentifier;

/**
 * Created by martin on 29.3.16.
 */
public class Weather implements IIdentifier {

	@SerializedName("main")
	public Main mMain;

	@SerializedName("name")
	public String mCity;

	public float getTemp() {
		return mMain.getTemp();
	}

	public String getCity() {
		return mCity;
	}

	@Override
	public String getId() {
		return "0";
	}

	private class Main {

		@SerializedName("temp")
		private float mTemp;

		@SerializedName("pressure")
		private float mPressure;

		@SerializedName("humidity")
		private float mHumidity;

		@SerializedName("temp_min")
		private float mTempMin;

		@SerializedName("temp_max")
		private float mTempMax;

		public float getTemp() {
			return mTemp;
		}

		public float getPressure() {
			return mPressure;
		}

		public float getHumidity() {
			return mHumidity;
		}

		public float getTempMin() {
			return mTempMin;
		}

		public float getTempMax() {
			return mTempMax;
		}
	}

}
