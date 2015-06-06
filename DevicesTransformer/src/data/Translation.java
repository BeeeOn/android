package data;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Translation {
	private static final String RESOURCE_STRING = "R.string." + Language.TRANSLATION_PREFIX + "%s";

	private final String mTranslationId;

	public Translation(String translation) {
		String[] parts = translation.split(":");

		if (parts.length != 2) {
			throw new IllegalArgumentException(String.format("data.Translation string must have 2 parts (separated by ':'). Given '%s'.", translation));
		}

		if (!parts[0].equals("T")) {
			throw new IllegalArgumentException(String.format("data.Translation string must start with 'T:'. Given '%s'.", translation));
		}

		mTranslationId = parts[1];
	}

	public String getTranslationId() {
		return mTranslationId;
	}

	public String getResourceId() {
		return String.format(RESOURCE_STRING, mTranslationId.toLowerCase());
	}

}
