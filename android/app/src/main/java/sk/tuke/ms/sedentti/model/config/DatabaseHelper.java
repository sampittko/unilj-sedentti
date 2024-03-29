package sk.tuke.ms.sedentti.model.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.PersonalityTest;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.ProfileStats;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.UploadTask;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private Dao<PersonalityTest, Long> personalityTestDao;
    private Dao<Profile, Long> profileDao;
    private Dao<Session, Long> sessionDao;
    private Dao<Activity, Long> activityDao;
    private Dao<UploadTask, Long> uploadTaskDao;
    private Dao<ProfileStats, Long> profileStatsDao;

    public DatabaseHelper(Context context) {
        super(context, Configuration.LOCAL_DATABASE_NAME, null, Configuration.LOCAL_DATABASE_VERSION,
                R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PersonalityTest.class);
            TableUtils.createTable(connectionSource, Profile.class);
            TableUtils.createTable(connectionSource, Session.class);
            TableUtils.createTable(connectionSource, Activity.class);
            TableUtils.createTable(connectionSource, UploadTask.class);
            TableUtils.createTable(connectionSource, ProfileStats.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, PersonalityTest.class, true);
            TableUtils.dropTable(connectionSource, Profile.class, true);
            TableUtils.dropTable(connectionSource, Session.class, true);
            TableUtils.dropTable(connectionSource, Activity.class, true);
            TableUtils.dropTable(connectionSource, UploadTask.class, true);
            TableUtils.dropTable(connectionSource, ProfileStats.class, true);
            onCreate(database, connectionSource);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an instance of the data access object
     * @return
     * @throws SQLException
     */
    public Dao<PersonalityTest, Long> personalityTestDao() throws SQLException {
        if(personalityTestDao == null) {
            personalityTestDao = getDao(PersonalityTest.class);
        }
        return personalityTestDao;
    }

    /**
     * Returns an instance of the data access object
     * @return
     * @throws SQLException
     */
    public Dao<Profile, Long> profileDao() throws SQLException {
        if(profileDao == null) {
            profileDao = getDao(Profile.class);
        }
        return profileDao;
    }

    /**
     * Returns an instance of the data access object
     * @return
     * @throws SQLException
     */
    public Dao<Session, Long> sessionDao() throws SQLException {
        if(sessionDao == null) {
            sessionDao = getDao(Session.class);
        }
        return sessionDao;
    }

    /**
     * Returns an instance of the data access object
     *
     * @return
     * @throws SQLException
     */
    public Dao<Activity, Long> activityDao() throws SQLException {
        if (activityDao == null) {
            activityDao = getDao(Activity.class);
        }
        return activityDao;
    }

    /**
     * Returns an instance of the data access object
     *
     * @return
     * @throws SQLException
     */
    public Dao<UploadTask, Long> uploadTaskDao() throws SQLException {
        if (uploadTaskDao == null) {
            uploadTaskDao = getDao(UploadTask.class);
        }
        return uploadTaskDao;
    }

    /**
     * Returns an instance of the data access object
     *
     * @return
     * @throws SQLException
     */
    public Dao<ProfileStats, Long> profileStatsDao() throws SQLException {
        if (profileStatsDao == null) {
            profileStatsDao = getDao(ProfileStats.class);
        }
        return profileStatsDao;
    }
}
