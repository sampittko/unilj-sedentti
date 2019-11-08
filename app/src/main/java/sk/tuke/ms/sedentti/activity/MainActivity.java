package sk.tuke.ms.sedentti.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.facebook.stetho.Stetho;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.CommonStrings;
import sk.tuke.ms.sedentti.model.PersonalityTest;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;
import sk.tuke.ms.sedentti.recognition.ActivityRecognitionService;

public class MainActivity extends AppCompatActivity {
    private Profile activeProfile;
    private Dao<Profile, Long> profileDao;
    private Dao<PersonalityTest, Long> personalityTestDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBottomMenu();

        // line that needs to be run after database scheme upgrade (firstly change version FROM and version TO)
//        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
//        databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), databaseHelper.getConnectionSource(), 4, 5);

        Stetho.initializeWithDefaults(this);

        databaseSetup();

        startForegroundService();

        setActiveProfile();
    }

    private void databaseSetup() {
        try {
            DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
            profileDao = databaseHelper.profileDao();
            personalityTestDao = databaseHelper.personalityTestDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setBottomMenu() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_statistics, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void startForegroundService() {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void setActiveProfile() {
        try {
            int numberOfProfiles = (int) profileDao.countOf();
            if (numberOfProfiles == 0) {
                activeProfile = getNewProfile();
            }
            else {
                activeProfile = getExistingProfile();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateProfileSharedPreferences();
    }

    private Profile getNewProfile() throws SQLException {
        PersonalityTest dummyPT = new PersonalityTest(0, 0, 0, 0, 0);
        Profile newProfile = new Profile("Janko Hrasko", dummyPT);

        personalityTestDao.create(dummyPT);
        profileDao.create(newProfile);
        return newProfile;
    }

    private Profile getExistingProfile() throws SQLException {
        return profileDao.queryForAll().get(0);
    }

    private void updateProfileSharedPreferences() {
        SharedPreferences profileShPr = getSharedPreferences(CommonStrings.PROFILE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor profileShPrEditor = profileShPr.edit();
        profileShPrEditor.putLong(CommonStrings.PROFILE_SHARED_PREFERENCES_ACTIVE_ID, activeProfile.getId());
        profileShPrEditor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
