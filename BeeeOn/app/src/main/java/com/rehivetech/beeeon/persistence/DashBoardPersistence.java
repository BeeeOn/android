package com.rehivetech.beeeon.persistence;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.ActualValueItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.GraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.OverviewGraphItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 19.1.16.
 */
public class DashBoardPersistence {
	private static GsonBuilder sGsonBuilder = new GsonBuilder();
	private static Type sListType = new TypeToken<ArrayList<ArrayList<BaseItem>>>() {}.getType();

	static {
		sGsonBuilder.registerTypeAdapter(BaseItem.class, new CustomDeserializer());
	}

	public static void save(SharedPreferences preferences, String key, List<List<BaseItem>> items) {
		Gson gson = DashBoardPersistence.sGsonBuilder.create();

		String jsonString = gson.toJson(items);


		preferences.edit().putString(key, jsonString).apply();
	}

	@Nullable public static List<List<BaseItem>> load(SharedPreferences preferences, String key) {
		String jsonString = preferences.getString(key, "");

		Gson gson = DashBoardPersistence.sGsonBuilder.create();
		return gson.fromJson(jsonString, sListType);
	}

	private static class CustomDeserializer implements JsonDeserializer<BaseItem> {

		@Override
		public BaseItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			Type typeClass;

			if (((JsonObject) json).get("dataRange") != null) {
				typeClass = GraphItem.class;
			} else if (((JsonObject) json).get("dataType") != null) {
				typeClass = OverviewGraphItem.class;
			} else if (((JsonObject)json).get("insideAbsoluteModuleId") != null) {
				typeClass = VentilationItem.class;
			} else {
				typeClass = ActualValueItem.class;
			}

			return new Gson().fromJson(json, typeClass);
		}
	}
}
