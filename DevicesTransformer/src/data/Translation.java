package data;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Translation {
	private static final String RESOURCE_STRING = "R.string." + Language.TRANSLATION_PREFIX + "%s";

	private final String[] mTranslationsIds;

	public Translation(String translation) {
		String[] parts = translation.split(":");

		if (parts.length < 2) {
			throw new IllegalArgumentException(String.format("data.Translation string must have at least 2 parts. Given '%s'.", translation));
		}

		if (!parts[0].equals("T")) {
			throw new IllegalArgumentException(String.format("data.Translation string must start with 'T:'. Given '%s'.", translation));
		}

		mTranslationsIds = new String[parts.length - 1];
		for (int i = 1; i < parts.length; i++) {
			mTranslationsIds[i - 1] = parts[i];
		}
	}

	public Translation(String[] translationIds) {
		mTranslationsIds = translationIds;
	}

	public String[] getTranslationIds() {
		return mTranslationsIds;
	}

	public String[] getResourceIds() {
		String[] resourceIds = new String[mTranslationsIds.length];

		for (int i = 0; i < mTranslationsIds.length; i++) {
			resourceIds[i] = String.format(RESOURCE_STRING, mTranslationsIds[i].toLowerCase());
		}

		return resourceIds;
	}

}
