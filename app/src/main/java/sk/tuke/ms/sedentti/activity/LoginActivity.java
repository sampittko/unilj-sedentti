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
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Profile activeProfile;

    private ProfileHelper profileHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profileHelper = new ProfileHelper(this);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        updateActiveProfile();
        setUpCrashlytics();
    }

    private void updateActiveProfile() {
        try {
            if (profileHelper.getNumberOfExistingProfiles() == 0) {
                Log.d(TAG, "Requesting profile information");
                handleFirebaseAuthUI();
            }
            else {
                Log.d(TAG, "Existing profile is being used");
                activeProfile = profileHelper.getExistingProfile();
                startFollowingActivity();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleFirebaseAuthUI() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        Log.d(TAG, "Starting FirebaseUI");

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

        Log.d(TAG, "FirebaseUI resulted");

        if (requestCode == PredefinedValues.FIREBASE_CODE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                assert user != null;
                Log.d(TAG, "Logged in with email " + user.getEmail());

                try {
                    activeProfile = profileHelper.createNewProfile(
                            user.getDisplayName(),
                            user.getEmail(),
                            Objects.requireNonNull(user.getPhotoUrl()).getEncodedPath(),
                            user.getUid());

                    startFollowingActivity();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Log.w(TAG, "User did not log in, quitting app");
                finish();
            }
        }
    }

    private void setUpCrashlytics() {
        Crashlytics.setUserIdentifier(activeProfile.getFirebaseAuthUid());
        Crashlytics.setUserEmail(activeProfile.getEmail());
        Crashlytics.setUserName(activeProfile.getName());
    }

    private void startFollowingActivity() {
        sharedPreferencesHelper.updateActiveProfile(activeProfile);

        Intent intent;
        if (activeProfile.getPersonalityTest() != null) {
            Log.d(TAG, "Starting main activity");
            intent = new Intent(this, MainActivity.class);
        }
        else {
            Log.d(TAG, "Starting personality test");
            intent = new Intent(this, PersonalityTestActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
