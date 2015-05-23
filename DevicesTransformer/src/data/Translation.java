package data;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Translation {
    private static final String RESOURCE_STRING = "R.string." + Language.TRANSLATION_PREFIX + "%s";

    private final String mTranslation;

    private final String[] mTranslationsIds;

    private final String[] mResourcesIds;

    public Translation(String translation) {
        String[] parts = translation.split(":");

        if (parts.length < 2) {
            throw new IllegalArgumentException(String.format("data.Translation string must have at least 2 parts. Given '%s'.", translation));
        }

        if (!parts[0].equals("T")) {
            throw new IllegalArgumentException(String.format("data.Translation string must start with 'T:'. Given '%s'.", translation));
        }

        mTranslation = translation;
        mTranslationsIds = new String[parts.length - 1];
        mResourcesIds = new String[parts.length - 1];

        for (int i = 1; i < parts.length; i++) {
            mTranslationsIds[i - 1] = parts[i];
            mResourcesIds[i - 1] = String.format(RESOURCE_STRING, parts[i].toLowerCase());
        }
    }

    public String getTranslation() {
        return mTranslation;
    }

    public String[] getTranslationIds() {
        return mTranslationsIds;
    }

    public String[] getResourceIds() {
        return mResourcesIds;
    }

}
