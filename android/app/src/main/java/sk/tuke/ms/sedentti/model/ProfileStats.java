package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ProfileStats {
    public final static String COLUMN_ID = "id";
    public final static String HIGHEST_STREAK = "highestStreak";
    public final static String COLUMN_PROFILE_ID = "profile_id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(columnName = HIGHEST_STREAK)
    private int highestStreak;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_PROFILE_ID)
    private Profile profile;

    public ProfileStats() {

    }

    public ProfileStats(Profile profile) {
        this.profile = profile;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public void setHighestStreak(int highestStreak) {
        this.highestStreak = highestStreak;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
