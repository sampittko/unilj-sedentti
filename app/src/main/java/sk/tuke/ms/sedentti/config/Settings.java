package sk.tuke.ms.sedentti.config;

/**
 * Values that the user is able to change in the app settings
 */
public abstract class Settings {
    public static final int SEDENTARY_MILLISECONDS_LIMIT = 1800000;
    public static final int ACTIVE_MILLISECONDS_LIMIT = 60000;
    public static final int ACTIVE_MOVEMENT_MILLISECONDS_THRESHOLD = 60000;
    public static final int STOP_SENSING_RELATIVE_VALUE = 8;
}
