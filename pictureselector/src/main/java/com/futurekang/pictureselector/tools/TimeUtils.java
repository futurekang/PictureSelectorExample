package com.futurekang.pictureselector.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class TimeUtils {

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public static String TimestampToDate(String time, String dateFormat) {
        if (isNumeric(time) && !time.equals("")) {
            int length = time.length();
            if (length == 13) {
                time = time.substring(0, 10);
            }
            long timel = (long) Integer.parseInt(time);
            return TimestampToDate(timel, dateFormat);
        } else {
            return time;
        }
    }

    public static String TimestampToDate(long time, String dateFormat) {
        if (String.valueOf(time).length() == 10) {
            time = time * 1000;
        }
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(date);
    }

    //判断选择的日期是否是本周
    public static boolean isThisWeek(long time) {
        if (String.valueOf(time).length() == 10) {
            time = time * 1000;
        }
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        calendar.setTime(new Date(time));
        int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        if (paramWeek == currentWeek) {
            return true;
        }
        return false;
    }

    //判断选择的日期是否是今天
    public static boolean isToday(long time) {
        return isThisTime(time, "yyyy-MM-dd");
    }

    //判断选择的日期是否是本月
    public static boolean isThisMonth(long time) {
        return isThisTime(time, "yyyy-MM");
    }

    private static boolean isThisTime(long time, String pattern) {
        if (String.valueOf(time).length() == 10) {
            time = time * 1000;
        }
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String param = sdf.format(date);//参数时间
        String now = sdf.format(new Date());//当前时间
        if (param.equals(now)) {
            return true;
        }
        return false;
    }
}
