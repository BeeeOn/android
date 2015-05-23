import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Language {
    public static final String VALUES_SEPARATOR = "_";

    private final String mCode;

    private final List<Item> mItems = new ArrayList<>();

    public Language(String code) {
        mCode = code;
    }

    public static class Item {
        public final String key;
        public final String value;

        public Item(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public Item(String prefix, String key, String value) {
            this.key = prefix + VALUES_SEPARATOR + key;
            this.value = value;
        }
    }

    public void addItem(String key, String value) {
        mItems.add(new Item(key, value));
    }

    public void addItem(Item item) {
        mItems.add(item);
    }

    public void addItems(List<Item> items) {
        for (Item item : items) {
            mItems.add(item);
        }
    }

    public String getCode() {
        return mCode;
    }

    public List<Item> getItems() {
        return mItems;
    }
    
}
