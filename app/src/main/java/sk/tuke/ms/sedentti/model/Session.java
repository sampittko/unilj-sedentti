package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import sk.tuke.ms.sedentti.model.day.DayModel;
import sk.tuke.ms.sedentti.model.helper.DateHelper;

@DatabaseTable
public class Session extends DayModel {
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_SEDENTARY = "sedentary";
    public final static String COLUMN_START_TIMESTAMP = "startTimestamp";
    public final static String COLUMN_END_TIMESTAMP = "endTimestamp";
    public final static String COLUMN_DATE = "date";
    public final static String COLUMN_DURATION = "duration";
    public final static String COLUMN_SUCCESSFUL = "successful";
    public final static String COLUMN_PROFILE_ID = "profile_id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(canBeNull = false, columnName = COLUMN_SEDENTARY)
    private boolean sedentary;
    @DatabaseField(canBeNull = false, columnName = COLUMN_START_TIMESTAMP)
    private long startTimestamp;
    @DatabaseField(columnName = COLUMN_END_TIMESTAMP)
    private long endTimestamp;
    @DatabaseField(columnName = COLUMN_DURATION)
    private long duration;
    @DatabaseField(canBeNull = false, columnName = COLUMN_DATE)
    private Date date;
    @DatabaseField(columnName = COLUMN_SUCCESSFUL)
    private boolean successful;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_PROFILE_ID)
    private Profile profile;

    public Session() {

    }

    public Session(boolean sedentary, long startTimestamp, Profile profile) {
        this.sedentary = sedentary;
        this.startTimestamp = startTimestamp;
        this.date = DateHelper.getNormalizedDate(new Date());
        this.profile = profile;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSedentary() {
        return sedentary;
    }

    public void setSedentary(boolean sedentary) {
        this.sedentary = sedentary;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}