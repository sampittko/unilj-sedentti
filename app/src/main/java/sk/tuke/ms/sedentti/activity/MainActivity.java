package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.firebase.uploader.UploadScheduler;
import sk.tuke.ms.sedentti.firebase.uploader.UploadWorker;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;
import sk.tuke.ms.sedentti.activity_recognition.ActivityRecognitionService;

public class MainActivity extends AppCompatActivity {
    private Profile activeProfile;

    private final String TAG = this.getClass().getSimpleName();

    private SessionHelper sessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setBottomMenu();

        performInitialSetup();

        startForegroundService();

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
        try {
            ProfileHelper profileHelper = new ProfileHelper(this);
            activeProfile = profileHelper.getActive();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sessionHelper = new SessionHelper(this, activeProfile);

        AppSPHelper appSPHelper = new AppSPHelper(this);
        appSPHelper.setAppDefaultSettings();

        Crashlytics.log(Log.DEBUG, TAG, "Initial setup performed");
    }

    private void checkForPendingSession() {
        try {
            Session pendingSession = sessionHelper.getPending();

            if (pendingSession != null) {
                sessionHelper.end(pendingSession);
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
        long initialMillisecondsDelay = 0;
        try {
            initialMillisecondsDelay = new UploadScheduler(this, activeProfile).getInitialMillisecondsDelay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Crashlytics.log(Log.DEBUG, TAG, "Initial milliseconds delay for upload work is " + initialMillisecondsDelay);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(Configuration.UPLOAD_WORK_NETWORK_TYPE)
                .build();

        PeriodicWorkRequest uploadRequest =
                new PeriodicWorkRequest.Builder(UploadWorker.class, Configuration.UPLOAD_WORK_WAITING_MINUTES, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .setInitialDelay(
                                initialMillisecondsDelay,
                                TimeUnit.MILLISECONDS
                        )
                        .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        Configuration.UPLOAD_WORK_NAME,
                        Configuration.UPLOAD_WORK_POLICY,
                        uploadRequest
                );

        Crashlytics.log(Log.DEBUG, TAG, "Upload work scheduled");
    }
}
