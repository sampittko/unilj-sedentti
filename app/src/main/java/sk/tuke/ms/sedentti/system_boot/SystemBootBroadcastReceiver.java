package sk.tuke.ms.sedentti.system_boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import sk.tuke.ms.sedentti.activity.FirstTimeStartupActivity;

public class SystemBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NotNull Intent intent) {
        if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context, FirstTimeStartupActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
