package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import sk.tuke.ms.sedentti.model.day.DayModel;
import sk.tuke.ms.sedentti.model.helper.DateHelper;

@DatabaseTable
public class Session extends DayModel {
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_SEDENTARY = "sedentary";
    public final static String COLUMN_IN_VEHICLE = "inVehicle";
    public final static String COLUMN_START_TIMESTAMP = "startTimestamp";
    public final static String COLUMN_END_TIMESTAMP = "endTimestamp";
    public final static String COLUMN_DATE = "date";
    public final static String COLUMN_DURATION = "duration";
    public final static String COLUMN_SUCCESSFUL = "successful";
    public final static String COLUMN_UPLOADED = "uploaded";
    public final static String COLUMN_EXPORTED = "exported";
    public final static String COLUMN_PROFILE_ID = "profile_id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(canBeNull = false, columnName = COLUMN_SEDENTARY)
    private boolean sedentary;
    @DatabaseField(canBeNull = false, columnName = COLUMN_IN_VEHICLE)
    private boolean inVehicle;
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
    @DatabaseField(canBeNull = false, columnName = COLUMN_UPLOADED)
    private boolean uploaded;
    @DatabaseField(canBeNull = false, columnName = COLUMN_EXPORTED)
    private boolean exported;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_PROFILE_ID)
    private Profile profile;

    public Session() {

    }

    public Session(boolean sedentary, boolean inVehicle, long startTimestamp, Profile profile) {
        this.sedentary = sedentary;
        this.inVehicle = inVehicle;
        this.startTimestamp = startTimestamp;
        this.date = DateHelper.getNormalized(new Date());
        this.uploaded = false;
        this.exported = false;
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

    public boolean isInVehicle() {
        return inVehicle;
    }

    public void setInVehicle(boolean inVehicle) {
        this.inVehicle = inVehicle;
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

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @NotNull
    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", date=" + date +
                '}';
    }

    /**
     * @param session1
     * @param session2
     * @return boole
     */
    public boolean isEqual(@NotNull Session session) {
        return this.getId() == session.getId();
    }
}