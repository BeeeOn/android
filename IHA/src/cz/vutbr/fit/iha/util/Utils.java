package cz.vutbr.fit.iha.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.format.Time;

public class Utils {
	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
	    int targetWidth = 200;
	    int targetHeight = 200;
	    Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, 
	                        targetHeight,Bitmap.Config.ARGB_8888);

	    Canvas canvas = new Canvas(targetBitmap);
	    Path path = new Path();
	    path.addCircle(((float) targetWidth - 1) / 2,
	        ((float) targetHeight - 1) / 2,
	        (Math.min(((float) targetWidth), 
	        ((float) targetHeight)) / 2),
	        Path.Direction.CCW);

	    canvas.clipPath(path);
	    Bitmap sourceBitmap = scaleBitmapImage;
	    canvas.drawBitmap(sourceBitmap, 
	        new Rect(0, 0, sourceBitmap.getWidth(),
	        sourceBitmap.getHeight()), 
	        new Rect(0, 0, targetWidth, targetHeight), null);
	    return targetBitmap;
	}
	
	/**
	 * Return offset from UTC in milliseconds
	 * @return
	 */
	private static int getLocalUtcOffset() {
		TimeZone tz = TimeZone.getDefault();
		Date now = new Date();
		return tz.getOffset(now.getTime());
	}
	
	private static Time applyUtcOffset(Time time) {
		// TODO: respect application settings
		boolean useLocalTime = true;
		
		int utcOffset = useLocalTime ? getLocalUtcOffset() : 0; // : adapter.getUtcOffset() * 60 * 60 * 1000; // in milliseconds
		Time result = new Time();
		result.set(time.toMillis(true) + utcOffset);
		return result;
	}
	
	public static String formatLastUpdate(Time lastUpdate) {
		// Apply utcOffset
		lastUpdate = applyUtcOffset(lastUpdate);
		
		// Last update time data
		Time yesterday = new Time();
		yesterday.setToNow();
		yesterday.set(yesterday.toMillis(true) - 24 * 60 * 60 * 1000); // -24 hours
		
		// If sync time is more that 24 ago, show only date. Show time otherwise.
		DateFormat dateFormat = yesterday.before(lastUpdate) ? DateFormat.getTimeInstance() : DateFormat.getDateInstance();
		
		Date lastUpdateDate = new Date(lastUpdate.toMillis(true));
		return dateFormat.format(lastUpdateDate);
	}
	
}
