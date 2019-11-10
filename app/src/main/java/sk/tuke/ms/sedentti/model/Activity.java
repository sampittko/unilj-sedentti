package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Activity {
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_TYPE = "type";
    public final static String COLUMN_TIMESTAMP = "timestamp";
    public final static String COLUMN_SESSION_ID = "session_id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(canBeNull = false, columnName = COLUMN_TYPE)
    private int type;
    @DatabaseField(canBeNull = false, columnName = COLUMN_TIMESTAMP)
    private long timestamp;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_SESSION_ID)
    private Session session;

    public Activity() {

    }

    public Activity(int type, long timestamp, Session session) {
        this.type = type;
        this.timestamp = timestamp;
        this.session = session;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
