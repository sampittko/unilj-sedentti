package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import sk.tuke.ms.sedentti.model.field.types.DateStringSQLiteType;

@DatabaseTable
public class Session {
    public static String COLUMN_ID = "id";
    public static String COLUMN_SEDENTARY = "sedentary";
    public static String COLUMN_START_TIMESTAMP = "startTimesamp";
    public static String COLUMN_END_TIMESAMP = "endTimestamp";
    public static String COLUMN_DATE = "date";
    public static String COLUMN_DURATION = "duration";
    public static String COLUMN_SUCCESSFUL = "successful";
    public static String COLUMN_PROFILE_ID = "profileId";

    @DatabaseField(generatedId = true, unique = true)
    private long id;
    @DatabaseField(canBeNull = false)
    private boolean sedentary;
    @DatabaseField(canBeNull = false)
    private int startTimestamp;
    @DatabaseField
    private int endTimestamp;
    @DatabaseField
    private int duration;
    @DatabaseField(canBeNull = false, persisterClass = DateStringSQLiteType.class)
    private Date date;
    @DatabaseField
    private boolean successful;
    @DatabaseField(canBeNull = false, foreign = true)
    private Profile profile;

    public Session() {

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

    public int getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(int startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public int getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(int endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
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