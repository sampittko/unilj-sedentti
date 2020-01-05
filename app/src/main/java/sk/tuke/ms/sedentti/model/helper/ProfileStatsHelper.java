package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.ProfileStats;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ProfileStatsHelper {
    private static final String TAG = "ProfileStatsHelper";

    private Dao<ProfileStats, Long> profileStatsDao;

    private ProfileStats profileStats;

    private Profile profile;

    public ProfileStatsHelper(Context context, Profile profile) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            this.profileStatsDao = databaseHelper.profileStatsDao();
            this.profileStats = profileStatsDao.queryBuilder()
                    .where()
                    .eq(ProfileStats.COLUMN_PROFILE_ID, profile.getId())
                    .queryForFirst();
            this.profile = profile;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param streak
     */
    public void updateHighestStreak(int streak) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing updateHighestStreak");
        Crashlytics.log(Log.DEBUG, TAG, "@streak: " + streak);

        if (streak > profileStats.getHighestStreak()) {
            Crashlytics.log(Log.DEBUG, TAG, "Streak is higher, updating");
            profileStats.setHighestStreak(streak);
            update(profileStats);
        }
        else {
            Crashlytics.log(Log.DEBUG, TAG, "Streak is not higher, no need to update");
        }
    }

    private void update(ProfileStats profileStats) throws SQLException {
        profileStatsDao.update(profileStats);
    }

    /**
     * @throws SQLException
     */
    public void createNew() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing createNew");
        profileStatsDao.create(new ProfileStats(profile));
    }
}
