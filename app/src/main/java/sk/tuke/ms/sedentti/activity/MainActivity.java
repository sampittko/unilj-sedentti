package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.recognition.ActivityRecognitionService;

public class MainActivity extends AppCompatActivity {
    private Profile activeProfile;

    private final String TAG = this.getClass().getSimpleName();

    private SessionHelper sessionHelper;
    private ProfileHelper profileHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setBottomMenu();

        // line that needs to be run after database scheme upgrade (firstly change version FROM and version TO)
//        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
//        databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), databaseHelper.getConnectionSource(), 4, 5);

        Stetho.initializeWithDefaults(this);

        performInitialSetup();

        startForegroundService();

        sharedPreferencesHelper.updateAppSettings();

        verifyLastSession();
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

    private void performInitialSetup() {
        profileHelper = new ProfileHelper(this);
        updateActiveProfile();
        sessionHelper = new SessionHelper(this, activeProfile);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
    }

    private void updateActiveProfile() {
        try {
            profileHelper = new ProfileHelper(this);
            if (profileHelper.getNumberOfExistingProfiles() == 0) {
                activeProfile = profileHelper.createNewProfile("Jánošík");
            }
            else {
                activeProfile = profileHelper.getExistingProfile();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sharedPreferencesHelper.updateActiveProfile(activeProfile);
    }

    private void startForegroundService() {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void verifyLastSession() {
        try {
            Session pendingSession = sessionHelper.getPendingSession();

            if (pendingSession != null) {
                sessionHelper.updateAsEndedSession(pendingSession);
                Log.i(TAG, "Pending session set to ended.");
            }
            else {
                Log.i(TAG, "Last session is not pending.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
