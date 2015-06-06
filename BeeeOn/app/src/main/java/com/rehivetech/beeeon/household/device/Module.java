package com.rehivetech.beeeon.household.device;

import android.content.Context;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.IOrderIdentifier;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.List;

public final class Module implements IOrderIdentifier {
	public static final String ID_SEPARATOR = "---";

	/**
	 * Properties inherited from device's specification table.
	 */
	private final String mId;
	private final ModuleType mType; // type defines what BaseValue should be created and also allows searching/comparing by type + offset
	private final int mOffset;

	private final Integer mSort;
	private final int mGroupRes;
	private final int mNameRes;
	private final boolean mIsActuator;
	// private final Constraints mConstraints; // FIXME: implement later
	// private final Values mValues; // this is not needed, as needed is BaseValue property
	// private final Rules mRules; // FIXME: implement later

	private final Device mDevice; // parent device
	private final BaseValue mValue;

	public static Module createUnknownModule(Device device, String id) {
		return new Module(device, id, ModuleType.TYPE_UNKNOWN.getTypeId(), 0, null, null, null, false);
	}

	public Module(Device device, String id, int typeId, int offset, Integer sort, Integer groupRes, Integer nameRes, boolean isActuator) {
		mDevice = device;
		mId = id;
		mSort = sort;
		mGroupRes = groupRes != null ? groupRes : 0;
		mNameRes = nameRes != null ? nameRes : 0;
		mIsActuator = isActuator;
		mOffset = offset;

		mType = ModuleType.fromTypeId(typeId);
		if (mType.getValueClass() == EnumValue.class) {
			throw new IllegalArgumentException("ValueClass received from ModuleType is EnumValue, but constructor was called without enumValues.");
		}
		mValue = BaseValue.createFromModuleType(mType);
	}

	public Module(Device device, String id, int typeId, int offset, Integer sort, Integer groupRes, Integer nameRes, boolean isActuator, List<EnumValue.Item> enumValues) {
		mDevice = device;
		mId = id;
		mSort = sort;
		mGroupRes = groupRes != null ? groupRes : 0;
		mNameRes = nameRes != null ? nameRes : 0;
		mIsActuator = isActuator;
		mOffset = offset;

		mType = ModuleType.fromTypeId(typeId);
		if (mType.getValueClass() != EnumValue.class) {
			throw new IllegalArgumentException("ValueClass received from ModuleType is not EnumValue, but constructor was called with enumValues.");
		}
		mValue = new EnumValue(enumValues);
	}


	/**
	 * Represents settings of module which could be saved to server
	 */
	public enum SaveModule {
		SAVE_NAME, // change name of module
		SAVE_LOCATION, // change location of mDevice
		SAVE_VISIBILITY, // change visibility of module
		SAVE_REFRESH, // change refresh interval of mDevice
		SAVE_VALUE, // change value of actor module
		SAVE_INITIALIZED,
	}

	public ModuleType getType() {
		return mType;
	}

	public BaseValue getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue.setValue(value);
	}

	/**
	 * Get resource for human readable string representing type of this module
	 *
	 * @return
	 */
	public int getTypeStringResource() {
		return mType.getStringResource();
	}

	public int getIconResource(){
		return getIconResource(IconResourceType.DARK);
	}

	public int getIconResource(IconResourceType type){
		return mIsActuator ? mValue.getActorIconResource(type) : mValue.getIconResource(type);
	}

	public Device getDevice() {
		return mDevice;
	}

	/**
	 * Get unique identifier of module (address of mDevice + raw type id containing offset)
	 *
	 * @return id
	 */
	public String getId() {
		if (mDevice == null)
			throw new RuntimeException("Module's mDevice is null!");

		return mDevice.getAddress() + ID_SEPARATOR + mId;
	}

	public int getOffset() {
		return mOffset;
	}

	/**
	 * @param context
	 * @return name of group
	 */
	public String getGroupName(Context context) {
		return mGroupRes > 0 ? context.getString(mGroupRes) : "";
	}

	/**
	 * @param context
	 * @param withGroup
	 * @return name of module, optionally prefixed with name of group
	 */
	public String getName(Context context, boolean withGroup) {
		String group = mGroupRes > 0 ? context.getString(mGroupRes) : "";
		String name = mNameRes > 0 ? context.getString(mNameRes) : "";

		return withGroup ? String.format("%s %s", group, name).trim() : name;
	}

	/**
	 * @param context
	 * @return name of module
	 */
	public String getName(Context context) {
		return getName(context, false);
	}

	/**
	 * @return true if module should be visible to the user at this moment
	 */
	public boolean isVisible() {
		// FIXME: real check based on rules
		return true;
	}

	public boolean isActuator() {
		return mIsActuator;
	}

	@Nullable
	@Override
	public Integer getSort() {
		return mSort;
	}

}
