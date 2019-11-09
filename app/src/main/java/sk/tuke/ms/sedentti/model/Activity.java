package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Activity {
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_ACTIVITY_TYPE = "activityType";
    public final static String COLUMN_DURATION = "duration";
    public final static String COLUMN_TIMESTAMP = "timestamp";
    public final static String COLUMN_SESSION_ID = "session_id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(canBeNull = false, columnName = COLUMN_ACTIVITY_TYPE)
    private int activityType;
    @DatabaseField(columnName = COLUMN_DURATION)
    private long duration;
    @DatabaseField(canBeNull = false, columnName = COLUMN_TIMESTAMP)
    private long timestamp;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_SESSION_ID)
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
