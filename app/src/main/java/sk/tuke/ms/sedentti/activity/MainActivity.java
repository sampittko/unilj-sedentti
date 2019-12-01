package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;
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
        // DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        // databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), databaseHelper.getConnectionSource(), 1, 2);

        Stetho.initializeWithDefaults(this);

        performInitialSetup();

        startForegroundService();

        sharedPreferencesHelper.setAppDefaultSettings();

        // checkForPendingSession();
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
        profileHelper = new ProfileHelper(this);
        updateActiveProfile();
        sessionHelper = new SessionHelper(this, activeProfile);

        Log.d(TAG, "Initial setup performed");
    }

    private void updateActiveProfile() {
        try {
            if (profileHelper.getNumberOfExistingProfiles() == 0) {
//                handleFirebaseAuthUI();
                activeProfile = profileHelper.createNewProfile(
                        "Janko Hrasko",
                        "janko.hrasko@gmail.com",
                        "https://hrasko.sk/profile.png",
                        "LOLOLOLOLOLOL");
            }
            else {
                activeProfile = profileHelper.getExistingProfile();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sharedPreferencesHelper.updateActiveProfile(activeProfile);

        Log.d(TAG, "Profile updated");
    }

    private void handleFirebaseAuthUI() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                CommonValues.RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CommonValues.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                try {
                    activeProfile = profileHelper.createNewProfile(
                            user.getDisplayName(),
                            user.getEmail(),
                            Objects.requireNonNull(user.getPhotoUrl()).getEncodedPath(),
                            user.getUid());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private void startForegroundService() {
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        Log.d(TAG, "Activity recognition foreground service started");
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
