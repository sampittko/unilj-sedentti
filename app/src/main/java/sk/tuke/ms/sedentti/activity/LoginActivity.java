package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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
        try {
            if (profileHelper.getNumberOfExisting() == 0) {
                Crashlytics.log(Log.DEBUG, TAG, "Requesting profile information");
                handleFirebaseAuthUI();
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "Existing profile is being used");
                activeProfile = profileHelper.getExisting();
                setUpCrashlytics();
                startFollowingActivity();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                            user.getPhotoUrl() == null ? Configuration.PROFILE_UNKNOWN_PHOTO_URL : user.getPhotoUrl().getEncodedPath(),
                            user.getUid());
                    setUpCrashlytics();
                    startFollowingActivity();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                Crashlytics.log(Log.ERROR, TAG, "Login not successful and quitting app as a consequence");
                finish();
            }
        }
    }

    private void setUpCrashlytics() {
        Crashlytics.setUserIdentifier(activeProfile.getFirebaseAuthUid());
        Crashlytics.setUserEmail(activeProfile.getEmail());
        Crashlytics.setUserName(activeProfile.getName());
        Crashlytics.log(Log.DEBUG, TAG, "Crashlytics set up successfully");
    }

    private void startFollowingActivity() {
        profileSPHelper.updateActiveProfile(activeProfile);

        Intent intent;
        if (activeProfile.getPersonalityTest() != null) {
            Crashlytics.log(Log.DEBUG, TAG, "Starting main activity");
            intent = new Intent(this, MainActivity.class);
        }
        else {
            Crashlytics.log(Log.DEBUG, TAG, "Starting personality test");
            intent = new Intent(this, PersonalityTestActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
