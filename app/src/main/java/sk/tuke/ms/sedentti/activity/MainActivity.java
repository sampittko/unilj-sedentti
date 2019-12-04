package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.SQLException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.CommonValues;
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
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setBottomMenu();

        Stetho.initializeWithDefaults(this);

        startForegroundService();

        performInitialSetup();

        sharedPreferencesHelper.setAppDefaultSettings();

        // checkForPendingSession();
    }

    private void startForegroundService() {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        intent.setAction(CommonValues.COMMAND_INIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        Log.d(TAG, "Activity recognition foreground service started");
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
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        ProfileHelper profileHelper = new ProfileHelper(this);

        try {
            activeProfile = profileHelper.getActiveProfile();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sessionHelper = new SessionHelper(this, activeProfile);

        Log.d(TAG, "Initial setup performed");
    }

    private void checkForPendingSession() {
        try {
            Session pendingSession = sessionHelper.getPendingSession();

            if (pendingSession != null) {
                sessionHelper.updateAsEndedSession(pendingSession);
                Log.d(TAG, "Pending session set to ended");
            }
            else {
                Log.d(TAG, "Last session is not pending");
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
