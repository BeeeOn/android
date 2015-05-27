package data;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 23. 5. 2015.
 */
public class Language {
	public static final String TRANSLATION_PREFIX = "devices__";
	public static final String VALUES_SEPARATOR = "_";

	private final String mCode;

	private final List<Item> mItems = new ArrayList<>();

	public Language(String code) {
		mCode = code;
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

	public void printLog(PrintStream stream) {
		stream.println(String.format("--- LISTING OF LANGUAGE '%s' ---", mCode));

		for (Language.Item item : mItems) {
			String name = item.key;
			String value = item.value;

			System.out.println(String.format("%s = \"%s\"", name, value));
		}
	}

	public void printAndroidXml(PrintWriter writer) {
		writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		writer.println("<resources>");

		for (Language.Item item : mItems) {
			String name = item.key;
			String value = item.value;

			writer.println(String.format("\t<string name=\"%s\">%s</string>", name, value));
		}

		writer.println("</resources>");
	}

	public static class Item {
		public final String key;
		public final String value;

		public Item(String key, String value) {
			this.key = TRANSLATION_PREFIX + key;
			this.value = value;
		}

		public Item(String prefix, String key, String value) {
			this.key = prefix + VALUES_SEPARATOR + key;
			this.value = value;
		}
	}

}
