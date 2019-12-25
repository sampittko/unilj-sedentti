package sk.tuke.ms.sedentti.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.work.upload.UploadWorkManager;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment(getResources().getString(R.string.app_settings_preference_screen), this))
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Context context;
        private String rootKey;
        private AppSPHelper appSPHelper;

        public SettingsFragment(String rootKey, Context context) {
            super();
            this.rootKey = rootKey;
            this.context = context;
            appSPHelper = new AppSPHelper(context);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, this.rootKey);
            PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(@NotNull SharedPreferences sharedPreferences, @NotNull String preference) {
            switch (preference) {
                case PredefinedValues.APP_SHARED_PREFERENCES_SYNC_INTERVAL:
                    Crashlytics.log(Log.DEBUG, TAG, "Sync interval changed");
                    try {
                        new UploadWorkManager(context).restartUploadWork();
                        Crashlytics.log(Log.DEBUG, TAG, "Upload work will be restarted");
                        Toast.makeText(context, "Sync interval updated", Toast.LENGTH_SHORT).show();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Crashlytics.log(Log.ERROR, TAG, "Unable to restart upload work");
                    }
                    break;
                case PredefinedValues.APP_SHARED_PREFERENCES_ACTIVE_LIMIT:
                    Toast.makeText(context, "Active limit updated", Toast.LENGTH_SHORT).show();
                case PredefinedValues.APP_SHARED_PREFERENCES_SEDENTARY_LIMIT:
                    Toast.makeText(context, "Sedentary limit updated", Toast.LENGTH_SHORT).show();
                case PredefinedValues.APP_SHARED_PREFERENCES_FIRST_NOTIF_STATE:
                    Crashlytics.log(Log.DEBUG, TAG, "First notification state changed");
                    if (!appSPHelper.getFirstNotifState() && appSPHelper.getSecondNotifState()) {
                        appSPHelper.setSecondNotifState(false);
                        SwitchPreferenceCompat switchPreferenceCompat = findPreference("second_notif_state");
                        assert switchPreferenceCompat != null;
                        switchPreferenceCompat.setChecked(false);
                    }
                    break;
                case PredefinedValues.APP_SHARED_PREFERENCES_SECOND_NOTIF_STATE:
                    Crashlytics.log(Log.DEBUG, TAG, "Second notification state changed");
                    if (!appSPHelper.getFirstNotifState() && appSPHelper.getSecondNotifState()) {
                        appSPHelper.setFirstNotifState(true);
                        SwitchPreferenceCompat switchPreferenceCompat = findPreference("first_notif_state");
                        assert switchPreferenceCompat != null;
                        switchPreferenceCompat.setChecked(true);
                    }
                    break;
            }
        }
    }
}