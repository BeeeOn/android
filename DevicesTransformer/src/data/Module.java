package data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Module {
	public static final String PREFIX_VALUE_SEPARATOR = "_";

	private final int mId;
	private final String mType;
	private final int mOffset;
	private final List<Value> mValues = new ArrayList<>();
	private final List<Rule> mRules = new ArrayList<>();
	private int mOrder;
	private Translation mGroup;
	private Translation mName;
	private boolean mActuator;
	private Constraints mConstraints;

	public Module(int id, String type, int offset) {
		mId = id;
		mType = type;
		mOffset = offset;
	}

	public int getId() {
		return mId;
	}

	public String getType() {
		return mType;
	}

	public int getOffset() {
		return mOffset;
	}

	public int getOrder() {
		return mOrder;
	}

	public void setOrder(int order) {
		mOrder = order;
	}

	public Translation getGroup() {
		return mGroup;
	}

	public void setGroup(Translation group) {
		mGroup = group;
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

	public List<Rule> getRules() {
		return mRules;
	}

	public void setRules(List<Rule> rules) {
		mRules.clear();
		mRules.addAll(rules);
	}

	public boolean isActuator() {
		return mActuator;
	}

	public void setActuator(boolean actuator) {
		mActuator = actuator;
	}

	public Constraints getConstraints() {
		return mConstraints;
	}

	public void setConstraints(Constraints constraints) {
		mConstraints = constraints;
	}

	public static class Constraints {
		private double mMin;
		private double mMax;
		private double mGranularity;

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
	}

	public static class Value {
		public final int state;
		public final Translation translation;

		public Value(int state, Translation translation) {
			this.state = state;
			this.translation = translation;
		}
	}

	public static class Rule {
		public final int state;
		public final int[] hideModulesIds;

		public Rule(int state, int[] hideModulesIds) {
			this.state = state;
			this.hideModulesIds = hideModulesIds;
		}
	}
}
