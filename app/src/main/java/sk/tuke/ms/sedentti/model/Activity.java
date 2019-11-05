package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable
public class Activity {

    @DatabaseField(generatedId = true, unique = true)
    long id;

    @DatabaseField
    int activityType;

    @DatabaseField
    long elapsedRealTimeNanos;

    @DatabaseField
    int transitionType;

    @DatabaseField
    Date timestamp;

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

    public long getElapsedRealTimeNanos() {
        return elapsedRealTimeNanos;
    }

    public void setElapsedRealTimeNanos(long elapsedRealTimeNanos) {
        this.elapsedRealTimeNanos = elapsedRealTimeNanos;
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
