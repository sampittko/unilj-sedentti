package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.helper.shared_preferences.ProfileSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Profile activeProfile;

    private ProfileHelper profileHelper;
    private ProfileSPHelper profileSPHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profileHelper = new ProfileHelper(this);
        profileSPHelper = new ProfileSPHelper(this);

        updateActiveProfile();
    }

    private void updateActiveProfile() {
        if (Configuration.USING_ARTIFICIAL_PROFILE) {
            Crashlytics.log(Log.DEBUG, TAG, "Using artificial profile");
            try {
                activeProfile = profileHelper.getArtificialProfile();
                finalizeActiveProfileUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Crashlytics.log(Log.DEBUG, TAG, "Using the real profile");
            try {
                if (!profileHelper.realProfileExists()) {
                    Crashlytics.log(Log.DEBUG, TAG, "Requesting profile information");
                    handleFirebaseAuthUI();
                } else {
                    Crashlytics.log(Log.DEBUG, TAG, "Existing profile is being used");
                    activeProfile = profileHelper.getRealProfile();
                    finalizeActiveProfileUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void finalizeActiveProfileUpdate() {
        profileHelper.setCrashlyticsUser(activeProfile);
        startFollowingActivity();
    }

    private void handleFirebaseAuthUI() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        Crashlytics.log(Log.DEBUG, TAG, "Starting FirebaseUI");

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                PredefinedValues.FIREBASE_CODE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Crashlytics.log(Log.DEBUG, TAG, "FirebaseUI resulted");

        if (requestCode == PredefinedValues.FIREBASE_CODE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                Crashlytics.log(Log.DEBUG, TAG, "Logged in user ID: " + user.getUid());
                try {
                    activeProfile = profileHelper.createNew(
                            user.getDisplayName() == null ? Configuration.PROFILE_UNKNOWN_DISPLAY_NAME : user.getDisplayName(),
                            user.getEmail() == null ? Configuration.PROFILE_UNKNOWN_EMAIL : user.getEmail(),
                            user.getPhotoUrl() == null ? Configuration.PROFILE_UNKNOWN_PHOTO_URL : user.getPhotoUrl().toString(),
                            user.getUid());
                    finalizeActiveProfileUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                assert response != null;
                // No internet connection error
                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Crashlytics.log(Log.DEBUG, TAG, "Offline mode");
                    handleUserOffline();
                } else {
                    Crashlytics.log(Log.ERROR, TAG, "Login not successful and quitting app as a consequence");
                    finish();
                }

            }
        }
    }

    private void handleUserOffline() {
        Crashlytics.log(Log.DEBUG, TAG, "Handling user offline");
        try {
            Profile profile = profileHelper.getRealProfile();
            if (profile == null) {
                Toast.makeText(this, "For the initial start of Sedentti you have to be online", Toast.LENGTH_LONG).show();
                Crashlytics.log(Log.ERROR, TAG, "There is no existing user, quitting app as a consequence");
                finish();
            } else {
                Crashlytics.log(Log.DEBUG, TAG, "Using existing user");
                activeProfile = profile;
                finalizeActiveProfileUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startFollowingActivity() {
        profileSPHelper.updateActiveProfile(activeProfile);

        Intent intent;
        if (activeProfile.getPersonalityTest() != null) {
            Crashlytics.log(Log.DEBUG, TAG, "Starting main activity");
            intent = new Intent(this, MainActivity.class);
        } else {
            Crashlytics.log(Log.DEBUG, TAG, "Starting personality test");
            intent = new Intent(this, PersonalityTestActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
