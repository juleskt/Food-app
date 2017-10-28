package ec601.aty.food_app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateAndTimeUtils {

    private static final int MILLISECONDS_IN_HOUR = 3600000;

    public static long getCurrentUnixTime() {
        return System.currentTimeMillis();
    }

    public static long addHoursToUnixTime(long unixTime, int hours) {
        return unixTime + (hours *  MILLISECONDS_IN_HOUR);
    }

    public static String getLocalFormattedDateFromUnixTime(long unixTime) {
       // Date date = new Date(unixTime);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(unixTime);
    }
}
