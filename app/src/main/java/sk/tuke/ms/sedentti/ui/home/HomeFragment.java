package sk.tuke.ms.sedentti.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
import sk.tuke.ms.sedentti.helper.TimeHelper;
import sk.tuke.ms.sedentti.model.Session;

public class HomeFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private HomeViewModel homeViewModel;
    private final int TIMELINE_ITEM_HEIGHT = 60;
    private LinearLayout timelineLayout;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);

//        Log.i("haha", "hahaha");

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel.getHomeTimelineSessions().observe(this, sessions -> makeTimeline(sessions));


        TextView streakValue = root.findViewById(R.id.tw_f_home_value_streaks);
        homeViewModel.getStreak().observe(this, value -> {
            if (value != null) {
                streakValue.setText(value.toString());
            }
        });

        TextView successValue = root.findViewById(R.id.tw_f_home_value_success);
        homeViewModel.getSuccess().observe(this, value -> {
            if (value != null) {
                successValue.setText(value.toString() + " %");
            }
        });

        makeGraphs(root);

        setOnClickOnViews(root);
        return root;
    }

    private void makeGraphs(View root) {
        DecoView activeSessionGraph = root.findViewById(R.id.graph_f_home_session);
        DecoView activeTime = root.findViewById(R.id.graph_f_home_active);
        DecoView sedentaryTime = root.findViewById(R.id.graph_f_home_sedentary);

        activeSessionGraph.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(40f)
                .build());

        activeTime.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(20f)
                .build());

        sedentaryTime.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(20f)
                .build());

        SeriesItem activeSessionItem = new SeriesItem.Builder(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                .setRange(0, 100, 0)
                .setLineWidth(40f)
                .build();

        SeriesItem activeItem = new SeriesItem.Builder(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null))
                .setRange(0, 100, 0)
                .setLineWidth(20f)
                .build();

        SeriesItem sedentaryItem = new SeriesItem.Builder(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setRange(0, 100, 0)
                .setLineWidth(20f)
                .build();

        int series1Index = activeSessionGraph.addSeries(activeSessionItem);
        int series2Index = activeTime.addSeries(activeItem);
        int series3Index = sedentaryTime.addSeries(sedentaryItem);

        TextView graphTimeValue = root.findViewById(R.id.tw_f_home_graph_time);
        homeViewModel.getPendingSessionDuration().observe(this, value -> {
            if (value != null) {
                graphTimeValue.setText(TimeHelper.formatTimeWithSeconds(value));
                int limit = new SharedPreferencesHelper(getContext()).getSedentarySecondsLimit() * 1000;
                int normalizedValue = getNormalizedValue(value, limit);
                activeSessionGraph.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(series1Index).setDelay(4000).build());
            }
        });

        TextView activeTimeValue = root.findViewById(R.id.tw_f_home_value_active);
        TextView sedentaryTimeValue = root.findViewById(R.id.tw_f_home_value_sedentary);

        homeViewModel.getDailySedentaryDuration().observe(this, value -> {
            sedentaryTimeValue.setText(TimeHelper.formatTimeString(value));
            // TODO: 11/10/19 set sedentary time goal
            int normalizedValue = getNormalizedValue(value, 8L * 3600L * 1000L);
            sedentaryTime.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(series2Index).setDelay(4000).build());
        });
        homeViewModel.getDailyActiveDuration().observe(this, value -> {
            activeTimeValue.setText(TimeHelper.formatTimeString(value));
            // TODO: 11/10/19 set activity time goal
            int normalizedValue = getNormalizedValue(value, 8 * 3600 * 1000);
            activeTime.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(series3Index).setDelay(4000).build());
        });
    }

    private int getNormalizedValue(Long milliseconds, long milliLimit) {
        int result = (int) (((double) milliseconds / milliLimit) * 100L);

        if (result > 100) {
            return 100;
        }
        return result;
    }

    private void setOnClickOnViews(View root) {
        this.timelineLayout = root.findViewById(R.id.f_home_layout_timeline);
        this.timelineLayout.setOnClickListener(v -> {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
            View view = bottomNavigationView.findViewById(R.id.navigation_statistics);
            view.performClick();
        });

        root.findViewById(R.id.iw_f_home_profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                View view = bottomNavigationView.findViewById(R.id.navigation_profile);
                view.performClick();
            }
        });
    }

    private void makeTimeline(ArrayList<Session> sessions) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        int index_sedentary = 1;
        int index_active = 1;

        // handles activity naming
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            View view = inflater.inflate(R.layout.item_timeline_home, this.timelineLayout, false);
            TextView dot = view.findViewById(R.id.tw_f_home_timeline_dot);
            String sessionName;

            if (session.isSedentary()) {
                sessionName = getResources().getString(R.string.home_timeline_name_sedentary) + " " + index_sedentary;
                index_sedentary++;
                dot.setBackground(getActivity().getDrawable(R.drawable.shape_timeline_circle_sedentarry));
            } else {
                sessionName = getResources().getString(R.string.home_timeline_name_activity) + " " + index_active;
                index_active++;
                dot.setBackground(getActivity().getDrawable(R.drawable.shape_timeline_circle_active));
            }
            TextView activityName = view.findViewById(R.id.tw_f_home_timeline_activity_name);
            activityName.setText(sessionName);

//            handles the time and adds date if needed
            String sessionTime;
            Date sessionDate = new Date(session.getStartTimestamp());
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(sessionDate);
            int sessionDayNumber = currentCalendar.get(Calendar.DAY_OF_MONTH);
            currentCalendar.setTime(new Date());
            int currentDayNumber = currentCalendar.get(Calendar.DAY_OF_MONTH);

            if (currentDayNumber != sessionDayNumber) {
                sessionTime = TimeHelper.formatDateTime(sessionDate.getTime());
            } else {
                sessionTime = TimeHelper.formatTime(sessionDate.getTime());
            }

//            handles duration
            if (session.getDuration() > 0L) {
                sessionTime += " " + TimeHelper.formatDuration(session.getDuration());
            }

            TextView activityTime = view.findViewById(R.id.tw_f_home_timeline_activity_time);
            activityTime.setText(sessionTime);

            this.timelineLayout.addView(view);
        }
        ViewGroup.LayoutParams layoutParams = this.timelineLayout.getLayoutParams();
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TIMELINE_ITEM_HEIGHT * sessions.size(), getResources().getDisplayMetrics());
        this.timelineLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}