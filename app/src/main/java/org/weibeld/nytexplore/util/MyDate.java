package org.weibeld.nytexplore.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dw on 23/02/17.
 */

public class MyDate {
    // Regex matching strings starting with a YYYY-MM-DD date (format of dates in API JSON response)
    private Pattern mApiJsonPattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}).*");
    // Format for parsing the YYYY-MM-DD date that was extracted with above pattern
    private SimpleDateFormat mApiJsonDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // Output date formats
    private SimpleDateFormat mOutFormat1 = new SimpleDateFormat("d MMM. yyyy", Locale.US);

    private Date mDate;

    // Create a MyDate object by passing a string starting with a YYYY-MM-DD date (this is the
    // format of dates in the JSON response of the API).
    public MyDate(String str) {
        Matcher matcher = mApiJsonPattern.matcher(str);
        if (matcher.find()) {
            try {
                mDate = mApiJsonDateFormat.parse(matcher.group(1));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // Format the date with output format 1
    public String format1() {
        if (mDate != null)
            return mOutFormat1.format(mDate);
        else
            return null;
    }
}
