package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

// TODO fix table generation - not working
@DatabaseTable
public class Activity {
    public static String COLUMN_ID = "id";
    public static String COLUMN_ACTIVITY_TYPE = "activityType";
    public static String COLUMN_TRANSITION_TYPE = "transitionType";
    public static String COLUMN_TIMESTAMP = "timestamp";

    @DatabaseField(generatedId = true, unique = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private int activityType;
    @DatabaseField
    private long duration;
    @DatabaseField(canBeNull = false)
    private long timestamp;
    @DatabaseField(canBeNull = false, foreign = true)
    private Session session;

    public Activity() {

    }

    public Activity(int activityType, long timestamp, Session session) {
        this.activityType = activityType;
        this.timestamp = timestamp;
        this.session = session;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
