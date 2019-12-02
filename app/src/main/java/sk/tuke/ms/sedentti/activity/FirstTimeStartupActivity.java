package sk.tuke.ms.sedentti.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class FirstTimeStartupActivity extends AppCompatActivity {

    private static final String TAG = "FTStartupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // line that needs to be run after database scheme upgrade (firstly change version FROM and version TO)
        // DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        // databaseHelper.onUpgrade(databaseHelper.getWritableDatabase(), databaseHelper.getConnectionSource(), 1, 2);

        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);
        boolean firstTimeStartupPerformed = sharedPreferencesHelper.firstTimeStartupPerformed();

        if (firstTimeStartupPerformed) {
            Log.d(TAG, "First time startup had already been performed before");

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Log.d(TAG, "Performing first time startup now");

            setContentView(R.layout.activity_first_time_startup);

            sharedPreferencesHelper.updateFirstTimeStartupPerformed(true);
        }
    }
}
