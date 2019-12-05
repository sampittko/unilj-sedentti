package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.firebase.uploader.UploadWorker;
import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;
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

        performInitialSetup();

        startForegroundService();

        sharedPreferencesHelper.setAppDefaultSettings();

        // checkForPendingSession();

         activateUploadWorker();
    }

    private void startForegroundService() {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        intent.setAction(PredefinedValues.COMMAND_INIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        Crashlytics.log(Log.DEBUG, TAG, "Activity recognition foreground service started");
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

        Crashlytics.log(Log.DEBUG, TAG, "Initial setup performed");
    }

    private void checkForPendingSession() {
        try {
            Session pendingSession = sessionHelper.getPendingSession();

            if (pendingSession != null) {
                sessionHelper.updateAsEndedSession(pendingSession);
                Crashlytics.log(Log.DEBUG, TAG, "Pending session set to ended");
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "Last session is not pending");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void activateUploadWorker() {
        // TODO check for last work and upload if needed
        // TODO delay upload request (do not perform it now)
//        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
//        DatabaseHelper.exportDatabase(databaseHelper.getWritableDatabase());

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest uploadRequest =
                new PeriodicWorkRequest.Builder(UploadWorker.class, 10, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueue(uploadRequest);
    }
}
