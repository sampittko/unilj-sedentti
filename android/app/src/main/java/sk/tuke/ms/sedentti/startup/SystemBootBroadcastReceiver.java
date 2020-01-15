package sk.tuke.ms.sedentti.startup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import sk.tuke.ms.sedentti.activity.FirstTimeStartupActivity;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;

public class SystemBootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SystemBootBroadcastReceiver";

    // TODO test
    @Override
    public void onReceive(Context context, @NotNull Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction != null) {
            if (intentAction.equals(Intent.ACTION_BOOT_COMPLETED) ||
                intentAction.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
                Crashlytics.log(Log.DEBUG, TAG, "Boot completed, trying to start Sedentti");
                ProfileHelper profileHelper = new ProfileHelper(context);
                Profile activeProfile = getActiveProfile(profileHelper);

                if (activeProfile != null) {
                    Crashlytics.log(Log.DEBUG, TAG, "Active profile is present");
                    try {
                        new StartupTasksExecutor(context).execute(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Crashlytics.log(Log.DEBUG, TAG, "Unable to execute required startup tasks");
                    }
                }
                else {
                    Crashlytics.log(Log.DEBUG, TAG, "There is no active profile, waiting for the first time startup");
                }
            }
        }
    }

    @Nullable
    private Profile getActiveProfile(@NotNull ProfileHelper profileHelper) {
        try {
            return profileHelper.getActive();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
