package cz.vutbr.fit.iha.activity.menuItem;

public abstract class AbstractMenuItem implements MenuItem {
	private String mId = ID_UNDEFINED;
	private MenuItemType mType;

	public AbstractMenuItem(String id, MenuItemType type) {
		mId = id;
		mType = type;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public MenuItemType getType() {
		return mType;
	}
}
