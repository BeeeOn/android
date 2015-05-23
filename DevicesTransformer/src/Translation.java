/**
 * Created by Robert on 23. 5. 2015.
 */
public class Translation {

    private final String[] mTranslationsIds;

    private final String[] mResourcesIds;

    public Translation(String translation) {
        String[] parts = translation.split(":");

        if (parts.length < 2) {
            throw new IllegalArgumentException(String.format("Translation string must have at least 2 parts. Given '%s'.", translation));
        }

        if (!parts[0].equals("T")) {
            throw new IllegalArgumentException(String.format("Translation string must start with 'T:'. Given '%s'.", translation));
        }

        mTranslationsIds = new String[parts.length - 1];
        mResourcesIds = new String[parts.length - 1];

        for (int i = 1; i < parts.length; i++) {
            mTranslationsIds[i - 1] = parts[i];
            mResourcesIds[i - 1] = String.format("R.string.%s", parts[i].toLowerCase())
        }
    }

    public String[] getTranslationIds() {
        return mTranslationsIds;
    }

    public String[] getResourceIds() {
        return mResourcesIds;
    }

}
