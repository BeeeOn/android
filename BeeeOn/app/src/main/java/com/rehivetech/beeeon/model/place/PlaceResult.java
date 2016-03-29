package com.rehivetech.beeeon.model.place;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by martin on 26.3.16.
 */
public class PlaceResult {

	@SerializedName("results")
	private List<Place> mResults;

	public List<Place> getResults() {
		return mResults;
	}
}
