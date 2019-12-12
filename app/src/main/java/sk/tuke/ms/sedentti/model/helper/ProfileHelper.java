package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.helper.shared_preferences.ProfileSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ProfileHelper {
    private static final String TAG = "ProfileHelper";

    private Dao<Profile, Long> profileDao;
    private QueryBuilder<Profile, Long> profileDaoQueryBuilder;

    private Context context;

    public ProfileHelper(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            profileDao = databaseHelper.profileDao();
            profileDaoQueryBuilder = profileDao.queryBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.context = context;
    }

    /**
     * @return Currently active profile based on shared preferences
     * @throws SQLException In case that communication with DB was not successful
     */
    public Profile getActive() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getActive");

        return profileDao.queryForId(
                new ProfileSPHelper(context).getActiveProfileId()
        );
    }

    /**
     * @param name String containing the name of future profile
     * @return New profile object
     * @throws SQLException In case that communication with DB was not successful
     */
    public Profile createNew(String name, String email, String photoUrl, String firebaseAuthUid) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing createNew");
        Crashlytics.log(Log.DEBUG, TAG, "@name: " + name);
        Crashlytics.log(Log.DEBUG, TAG, "@email: " + email);
        Crashlytics.log(Log.DEBUG, TAG, "@photoUrl: " + photoUrl);
        Crashlytics.log(Log.DEBUG, TAG, "@firebaseAuthUid: " + firebaseAuthUid);

        Profile profile = new Profile(name, email, photoUrl, firebaseAuthUid);
        profileDao.create(profile);
        return profile;
    }

    /**
     * @return Some existing profile which is also not artificial
     * @throws SQLException In case that communication with DB was not successful
     */
    public Profile getRealProfile() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getRealProfile");

        profileDaoQueryBuilder.reset();

        return profileDaoQueryBuilder
                .orderBy(Profile.COLUMN_REGISTERED_DATE, false)
                .where()
                .ne(Profile.COLUMN_FIREBASE_AUTH_UID, Configuration.PROFILE_ARTIFICIAL_FIREBASE_AUTH_ID)
                .and()
                .ne(Profile.COLUMN_FIREBASE_AUTH_UID, "")
                .queryForFirst();
    }

    /**
     * @return Whether the real profile exists or not
     * @throws SQLException In case that communication with DB was not successful
     */
    public boolean realProfileExists() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing realProfileExists");

        return getRealProfile() != null;
    }

    /**
     * @return
     */
    public Profile getArtificialProfile() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getArtificialProfile");

        profileDaoQueryBuilder.reset();

        Profile artificialProfile = profileDaoQueryBuilder
                .where()
                .eq(Profile.COLUMN_FIREBASE_AUTH_UID, Configuration.PROFILE_ARTIFICIAL_FIREBASE_AUTH_ID)
                .queryForFirst();

        if (artificialProfile == null) {
            return getNewArtificialProfile();
        }
        return artificialProfile;
    }

    private Profile getNewArtificialProfile() throws SQLException {
        return createNew(
                Configuration.PROFILE_ARTIFICIAL_NAME,
                Configuration.PROFILE_ARTIFICIAL_EMAIL,
                Configuration.PROFILE_ARTIFICIAL_PHOTO_URL,
                Configuration.PROFILE_ARTIFICIAL_FIREBASE_AUTH_ID
        );
    }
}
