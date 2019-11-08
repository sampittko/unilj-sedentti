package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import sk.tuke.ms.sedentti.model.helper.DateHelper;

@DatabaseTable
public class Session {
    public static String COLUMN_ID = "id";
    public static String COLUMN_SEDENTARY = "sedentary";
    public static String COLUMN_START_TIMESTAMP = "startTimestamp";
    public static String COLUMN_END_TIMESTAMP = "endTimestamp";
    public static String COLUMN_DATE = "date";
    public static String COLUMN_DURATION = "duration";
    public static String COLUMN_SUCCESSFUL = "successful";
    public static String COLUMN_PROFILE_ID = "profile_id";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private boolean sedentary;
    @DatabaseField(canBeNull = false)
    private long startTimestamp;
    @DatabaseField
    private long endTimestamp;
    @DatabaseField
    private long duration;
    @DatabaseField(canBeNull = false)
    private Date date;
    @DatabaseField
    private boolean successful;
    @DatabaseField(canBeNull = false, foreign = true)
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