package org.bentokit.flywire.util;

import java.util.Calendar;

import org.bentokit.flywire.config.Config;

/**
 * A special number format class to format times consistently.
 * @author tudor
 *
 */
public class TimeNumberFormat {
    ///////////////////////////////////////////////////////////////////////////
    // Static utility methods
    ///////////////////////////////////////////////////////////////////////////

    static java.text.NumberFormat timeNumberFormat;
    static
    {
        timeNumberFormat = java.text.NumberFormat.getInstance();
        timeNumberFormat.setMinimumIntegerDigits(2);
        timeNumberFormat.setMaximumIntegerDigits(2);
        timeNumberFormat.setMaximumFractionDigits(0);
    }

    static java.text.SimpleDateFormat DOWFormat = new java.text.SimpleDateFormat("EEE");
    static java.text.SimpleDateFormat DateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");

    /**
     * Returns a time as a String in time format.
     * @param time - The time to convert to a String.
     * @return
     */
    public static String formatTimeString(javax.media.Time time) {
        //System.err.println(time.getNanoseconds()+" => "+formatTimeString(time.getNanoseconds()));
        return(formatTimeString(time.getNanoseconds()));
    }

    /**
     * Returns a time as a String in time format.
     * @param time - The time in nanoseconds.
     * @return
     */
    public static String formatTimeString(long nanos)
    {
        long length = nanos / 1000000000;
        int hours = (int) length / 3600;
        length -= hours * 3600;
        int mins = (int) length / 60;
        length -= mins * 60;

        String string = timeNumberFormat.format(mins) + ":" + timeNumberFormat.format(length);
        if (Config.getConfig().alwaysShowHours() || hours > 0)
            string = timeNumberFormat.format(hours) + ":" + string;

        assert string != null;

        return string;
    }

    public static String formatTimeString(Calendar time) {
        int hours = time.get(Calendar.HOUR_OF_DAY);
        int mins = time.get(Calendar.MINUTE);
        String string = timeNumberFormat.format(hours) + ":" + timeNumberFormat.format(mins);

        Calendar yesterday = Calendar.getInstance();  yesterday.roll(Calendar.DATE,-1);
        Calendar weekago = Calendar.getInstance();  weekago.roll(Calendar.WEEK_OF_YEAR,-1);
        if (time.after(yesterday)) return(string);
        if (time.after(weekago)) {
            string = DOWFormat.format(time.getTime()) + " " + string;
        } else {
            string = DateFormat.format(time.getTime()) + " " + string;
        }
        return(string);
    }
}
