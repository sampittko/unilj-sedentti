package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

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
    @DatabaseField(canBeNull = false)
    private int transitionType;
    @DatabaseField(canBeNull = false)
    private Date timestamp;

    public Activity() {
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

    public int getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(int transitionType) {
        this.transitionType = transitionType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
