package sk.tuke.ms.sedentti.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class FirstTimeStartupActivity extends AppCompatActivity {

    private static final String TAG = "FTStartupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // line that needs to be run after database scheme upgrade (firstly change version FROM and version TO)
//         DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
//         databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), databaseHelper.getConnectionSource(), 5, 6);

        Stetho.initializeWithDefaults(this);

        AppSPHelper appSPHelper1 = new AppSPHelper(this);

        appSPHelper1.setAppDefaultSettings();

        // TODO request all required permissions

        AppSPHelper appSPHelper = new AppSPHelper(this);
        boolean firstTimeStartupPerformed = appSPHelper.firstTimeStartupPerformed();

        if (firstTimeStartupPerformed) {
            Crashlytics.log(Log.DEBUG, TAG, "First time startup had already been performed before");

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Crashlytics.log(Log.DEBUG, TAG, "Performing first time startup now");

            setContentView(R.layout.activity_first_time_startup);

            appSPHelper.updateFirstTimeStartupPerformed(true);

            // TODO implement
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
