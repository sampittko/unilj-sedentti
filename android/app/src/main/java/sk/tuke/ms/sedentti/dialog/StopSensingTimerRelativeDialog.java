package sk.tuke.ms.sedentti.dialog;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.recognition.activity.ActivityRecognitionStopReceiver;

import static sk.tuke.ms.sedentti.config.Configuration.APP_SHARED_PREFERENCES_STOP_SENSING_RELATIVE_TIME_DEFAULT;
import static sk.tuke.ms.sedentti.config.PredefinedValues.ALARM_STOP_SERVICE;

public class StopSensingTimerRelativeDialog extends DialogFragment {

    private final String TAG = this.getClass().getSimpleName();
    private StopSensingTimerRelativeDialogListener listener;

    public void setListener(StopSensingTimerRelativeDialogListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        AppSPHelper appSettings = new AppSPHelper(context);

        int value = appSettings.getStopSensingRelativeValue();
        AlarmManager alarmManager;
        PendingIntent alarmIntent;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ActivityRecognitionStopReceiver.class);
        intent.setAction(ALARM_STOP_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settings_sensing_relative, null);

        final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.numberpicker_time);
        numberPicker.setMaxValue(24);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(value);

        builder.setView(view);
        builder.setCancelable(false);

        builder.setTitle("Stop in...");
        builder.setMessage("Set when sensing stops in hours.");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (alarmManager != null) {
                    int pickedValue = numberPicker.getValue();
                    appSettings.setStopSensingRelativeValue(pickedValue);
                    long millisFromHours = pickedValue * 3600000;
                    long time = new Date().getTime() + millisFromHours;
                    appSettings.setStopSensingRelativeTime(time);

                    // is reset always when alredy set up as aong as you use same alarmIntent
                    alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + millisFromHours, alarmIntent);
                    Toast.makeText(context, "Sensing will stop in " + pickedValue + " hours.", Toast.LENGTH_LONG).show();
                    if (listener != null) {
                        listener.onStopSensingTimerRelativeUpdated(time);
                    }
                    Crashlytics.log(Log.DEBUG, TAG, "Stop Sensing relative timer registered in " + pickedValue + " hours, that is " + millisFromHours + " millis");
                } else {
                    Toast.makeText(context, "Error, please try  to reset timer once again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel timer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (alarmManager != null) {
                    appSettings.setStopSensingRelativeTime(APP_SHARED_PREFERENCES_STOP_SENSING_RELATIVE_TIME_DEFAULT);

                    alarmManager.cancel(alarmIntent);
                    Toast.makeText(context, "Stop sensing timer has been cancelled.", Toast.LENGTH_LONG).show();
                    if (listener != null) {
                        listener.onStopSensingTimerRelativeUpdated(APP_SHARED_PREFERENCES_STOP_SENSING_RELATIVE_TIME_DEFAULT);
                    }
                    Crashlytics.log(Log.DEBUG, TAG, "Stop Sensing relative timer cancelled successfully");
                } else {
                    Toast.makeText(context, "Error, please try  to reset timer once again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return builder.create();
    }

    public interface StopSensingTimerRelativeDialogListener {
        void onStopSensingTimerRelativeUpdated(long time);
    }
}
