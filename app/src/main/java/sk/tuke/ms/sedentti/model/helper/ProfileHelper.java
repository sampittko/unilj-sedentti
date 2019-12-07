package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.helper.shared_preferences.ProfileSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class ProfileHelper {
    private static final String TAG = "ProfileHelper";

    private Dao<Profile, Long> profileDao;

    private Context context;

    public ProfileHelper(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            profileDao = databaseHelper.profileDao();
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
     * @return Some existing profile
     * @throws SQLException In case that communication with DB was not successful
     */
    public Profile getExisting() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getExisting");

        return profileDao.queryForAll().get(0);
    }

    /**
     * @return Number of existing profiles in database
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getNumberOfExisting() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getNumberOfExisting");

        return (int) profileDao.countOf();
    }
}
