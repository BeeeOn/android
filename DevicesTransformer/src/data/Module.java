package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Module {
    public static final String PREFIX_VALUE_SEPARATOR = "_";

    private final int mId;
    private final int mType;
    private final int mOffset;
    private final int mOrder;
    private double mMin;
    private double mMax;
    private double mGranularity;
    private boolean mEnumValues;

    private Translation mName;
    private final List<Value> mValues = new ArrayList<>();
    private final List<Hide> mHides = new ArrayList<>();

    public static class Value {
        public final int state;
        public final Translation translation;

        public Value(int state, Translation translation) {
            this.state = state;
            this.translation = translation;
        }
    }

    public static class Hide {
        public final int state;
        public final int[] hideModulesIds;

        public Hide(int state, int[] hideModulesIds) {
            this.state = state;
            this.hideModulesIds = hideModulesIds;
        }
    }

    public Module(int id, int type, int offset, int order) {
        mId = id;
        mType = type;
        mOffset = offset;
        mOrder = order;
    }

    public int getId() {
        return mId;
    }

    public int getType() {
        return mType;
    }

    public int getOffset() {
        return mOffset;
    }

    public int getOrder() {
        return mOrder;
    }


    public double getMin() {
        return mMin;
    }

    public void setMin(double min) {
        mMin = min;
    }

    public double getMax() {
        return mMax;
    }

    public void setMax(double max) {
        mMax = max;
    }

    public double getGranularity() {
        return mGranularity;
    }

    public void setGranularity(double granularity) {
        mGranularity = granularity;
    }

    public boolean isEnumValues() {
        return mEnumValues;
    }

    public void setEnumValues(boolean enumValues) {
        mEnumValues = enumValues;
    }

    public Translation getName() {
        return mName;
    }

    public void setName(Translation name) {
        mName = name;
    }

    public List<Value> getValues() {
        return mValues;
    }

    public void setValues(Translation name, List<Value> values) {
        mValues.clear();
        for (Value value : values) {
            String prefix = "";
            for (String id : name.getTranslationIds()) {
                prefix += id + PREFIX_VALUE_SEPARATOR;
            }

            // Append value translation
            List<String> translationIds = new LinkedList<>();
            for (String id : name.getTranslationIds()) {
                translationIds.add(prefix + PREFIX_VALUE_SEPARATOR + id);
            }
            Translation translation = new Translation(translationIds.toArray(new String[translationIds.size()]));

            mValues.add(new Value(value.state, translation));
        }
    }

    public List<Hide> getHides() {
        return mHides;
    }

    public void setHides(List<Hide> hides) {
        mHides.clear();
        mHides.addAll(hides);
    }
}
