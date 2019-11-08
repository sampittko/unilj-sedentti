package sk.tuke.ms.sedentti.ui.statistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.CommonValues;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        statisticsViewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        return root;
    }

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityTransitionResult.hasResult(intent)) {

                StringBuilder sb = new StringBuilder();
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    // chronological sequence of events....

                    sb.append(event.toString());
                    sb.append(" ");
                }

                TextView status = getActivity().findViewById(R.id.status_info);
                status.setText(sb.toString());
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(activityReceiver, new IntentFilter(CommonValues.ACTIVITY_RECOGNITION_COMMAND));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(activityReceiver);
    }
}