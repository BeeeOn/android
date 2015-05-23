import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Translation {

    private final String mCode;

    private final List<Item> mItems = new ArrayList<>();

    public Translation(String code) {
        mCode = code;
    }

    private class Item {
        public final String key;
        public final String value;

        public Item(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public void addItem(String key, String value) {
        mItems.add(new Item(key, value));
    }

    public List<Item> getItems() {
        return mItems;
    }
    
}
