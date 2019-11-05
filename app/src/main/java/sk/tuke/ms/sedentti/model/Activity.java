package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Activity {

    @DatabaseField(generatedId = true, unique = true)
    private long id;
    @DatabaseField
    private int activityType;
    @DatabaseField
    private long elapsedRealTimeNanos;
    @DatabaseField
    private int transitionType;

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
}
