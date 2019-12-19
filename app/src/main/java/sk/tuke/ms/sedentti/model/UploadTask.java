package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import sk.tuke.ms.sedentti.model.helper.DateHelper;

@DatabaseTable
public class UploadTask {
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_START_TIMESTAMP = "startTimestamp";
    public final static String COLUMN_END_TIMESTAMP = "endTimestamp";
    public final static String COLUMN_DURATION = "duration";
    public final static String COLUMN_DATE = "date";
    public final static String COLUMN_BYTES_TRANSFERRED = "bytesTransferred";
    public final static String COLUMN_BYTES_TOTAL = "bytesTotal";
    public final static String COLUMN_DB_FILE_PATH = "dbFilePath";
    public final static String COLUMN_INCLUDED_SESSIONS = "includedSessions";
    public final static String COLUMN_PROCESSED = "processed";
    public final static String COLUMN_ERROR = "error";
    public final static String COLUMN_SESSION_URI_STRING = "sessionUriString";
    public final static String COLUMN_PROFILE_ID = "profile_id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(canBeNull = false, columnName = COLUMN_START_TIMESTAMP)
    private long startTimestamp;
    @DatabaseField(columnName = COLUMN_END_TIMESTAMP)
    private long endTimestamp;
    @DatabaseField(columnName = COLUMN_DURATION)
    private long duration;
    @DatabaseField(canBeNull = false, columnName = COLUMN_DATE)
    private Date date;
    @DatabaseField(canBeNull = false, columnName = COLUMN_BYTES_TRANSFERRED)
    private long bytesTransferred;
    @DatabaseField(canBeNull = false, columnName = COLUMN_BYTES_TOTAL)
    private long bytesTotal;
    @DatabaseField(canBeNull = false, columnName = COLUMN_DB_FILE_PATH)
    private String dbFilePath;
    @DatabaseField(canBeNull = false, columnName = COLUMN_INCLUDED_SESSIONS)
    private String includedSessions;
    @DatabaseField(columnName = COLUMN_PROCESSED)
    private boolean processed;
    @DatabaseField(columnName = COLUMN_ERROR)
    private String error;
    @DatabaseField(columnName = COLUMN_SESSION_URI_STRING)
    private String sessionUriString;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_PROFILE_ID)
    private Profile profile;

    public UploadTask() {

    }

    public UploadTask(long startTimestamp, long bytesTotal, String dbFilePath, String includedSessions, Profile profile) {
        this.startTimestamp = startTimestamp;
        this.date = DateHelper.getNormalized(new Date());
        this.bytesTotal = bytesTotal;
        this.bytesTransferred = 0L;
        this.dbFilePath = dbFilePath;
        this.includedSessions = includedSessions;
        this.processed = false;
        this.profile = profile;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public void setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public String getDbFilePath() {
        return dbFilePath;
    }

    public void setDbFilePath(String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }

    public String getIncludedSessions() {
        return includedSessions;
    }

    public void setIncludedSessions(String includedSessions) {
        this.includedSessions = includedSessions;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSessionUriString() {
        return sessionUriString;
    }

    public void setSessionUriString(String sessionUriString) {
        this.sessionUriString = sessionUriString;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
