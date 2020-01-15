package sk.tuke.ms.sedentti.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {

    public static String formatDateTime(long timestamp) {
        return SimpleDateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(timestamp);
    }

    public static String formatTime(long timestamp) {
        return SimpleDateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(timestamp);
    }

    public static String formatTimeWithSeconds(long timestamp) {
        timestamp = timestamp / 1000L;
        int HH = (int) (timestamp / 3600);
        timestamp = timestamp % 3600;

        int MM = (int) (timestamp / 60);
        timestamp = timestamp % 60; // v time su teraz zvysne sekundy

        return (String.format("%02d:%02d:%02d", HH, MM, timestamp));
    }


    public static String formatDuration(long timestamp) {
        timestamp = timestamp / 1000L;
        int HH = (int) (timestamp / 3600);
        timestamp = timestamp % 3600;

        int MM = (int) (timestamp / 60);
        timestamp = timestamp % 60; // v time su teraz zvysne sekundy

        String duration = "";

        if (MM > 0 || HH > 0 || timestamp > 0) {
            duration = "lasted ";
            if (HH > 1) {
                duration += HH + " hours ";
            } else if (HH == 1) {
                duration += HH + " hour ";
            }

            if (MM > 1) {
                duration += MM + " mins ";
            } else if (MM == 1) {
                duration += MM + " min ";
            }

            if (timestamp > 0) {
                duration += timestamp + " secs";
            }
        }

        return duration;
    }

    public static String formatTimeString(Long timestamp) {
        timestamp = timestamp / 1000L;
        int HH = (int) (timestamp / 3600);
        timestamp = timestamp % 3600;

        int MM = (int) (timestamp / 60);
        timestamp = timestamp % 60; // v time su teraz zvysne sekundy

        String duration = "";

//        if (MM > 0 || HH > 0) {
//            if (HH > 1) {
//                duration += HH + " hours ";
//            } else if (HH == 1) {
//                duration += HH + " hour ";
//            }
//
//            if (MM > 1) {
//                duration += MM + " mins";
//            } else if (MM == 1) {
//                duration += MM + " min";
//            }
//        }

        if (MM > 0 || HH > 0) {
            if (HH > 0) {
                duration += HH + " h ";
            }
            if (MM > 1) {
                duration += MM + " mins";
            } else if (MM == 1) {
                duration += MM + " min";
            }
        }

        if (duration.equals("")) {
            return "0 min";
        }
        return duration;
    }

    public static String formatDate(Date date) {
        return SimpleDateFormat.getDateInstance(java.text.DateFormat.SHORT).format(date);
    }
}
