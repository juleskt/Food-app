package ec601.aty.food_app;

import java.text.DateFormat;
import java.util.TimeZone;

import static java.text.DateFormat.getDateTimeInstance;

public class DateAndTimeUtils
{

    private static final int MILLISECONDS_IN_HOUR = 3600000;
    private static final int SECONDS_IN_HOUR = 3600;
    private static final int MILLISECONDS_TO_SECONDS = 3600;

    public static long getCurrentUnixTime()
    {
        return System.currentTimeMillis();
    }

    public static long addHoursToUnixTime(long unixTime, int hours)
    {
        return unixTime + (hours * MILLISECONDS_IN_HOUR);
    }

    public static String getLocalFormattedDateFromUnixTime(long unixTime)
    {
        DateFormat sdf = getDateTimeInstance();
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(unixTime);
    }

    public static boolean checkIfUnixTimeIsExpired(long unixTime)
    {
        return getCurrentUnixTime() > unixTime;
    }
}
