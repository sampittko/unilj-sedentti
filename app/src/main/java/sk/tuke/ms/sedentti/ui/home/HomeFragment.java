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
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        DecoView activeSessionGraph = root.findViewById(R.id.graph_f_home_session);
        DecoView activeTime = root.findViewById(R.id.graph_f_home_active);
        DecoView sedentaryTime = root.findViewById(R.id.graph_f_home_sedentary);

        // Create background track
        activeSessionGraph.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0, 100, 100)
                .setInitialVisibility(false)
                .setLineWidth(32f)
                .build());

        //Create data series track
        // TODO oprav farbu grafu na novu sviezu
        SeriesItem seriesItem1 = new SeriesItem.Builder(R.color.colorAccent)
                .setRange(0, 100, 0)
                .setLineWidth(32f)
                .build();

        int series1Index = activeSessionGraph.addSeries(seriesItem1);

        activeSessionGraph.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(1000)
                .build());

        TextView graphTimeValue = root.findViewById(R.id.tw_f_home_graph_time);
        homeViewModel.getPendingSessionDuration().observe(this, value -> {
            if (value != null) {
                graphTimeValue.setText(TimeHelper.formatTimeWithSeconds(value));
                int limit = new SharedPreferencesHelper(getContext()).getSedentarySecondsLimit();
                int normalizedValue = getNormalizedValue(value, limit);
                activeSessionGraph.addEvent(new DecoEvent.Builder(normalizedValue).setIndex(series1Index).setDelay(4000).build());
            }
        });

        TextView activeTimeValue = root.findViewById(R.id.tw_f_home_value_active);
        TextView sedentaryTimeValue = root.findViewById(R.id.tw_f_home_value_sedentary);

        homeViewModel.getDailySedentaryDuration().observe(this, value -> {
            sedentaryTimeValue.setText(TimeHelper.formatTimeString(value));
        });
        homeViewModel.getDailyActiveDuration().observe(this, value -> activeTimeValue.setText(TimeHelper.formatTimeString(value)));

        setOnClickOnViews(root);
        return root;
    }

    private int getNormalizedValue(Long value, int limit) {
        int minutesValue = (int) (value / 60);
        return minutesValue / limit * 100;
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
            if (session.getDuration() > 0) {
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

    @Override
    public void onResume() {
        super.onResume();

        List<DecoView> graphs = new ArrayList<>();
        graphs.add((DecoView) getView().findViewById(R.id.graph_f_home_active));
        graphs.add((DecoView) getView().findViewById(R.id.graph_f_home_sedentary));

        for (DecoView graph : graphs) {
            // Create background track
            graph.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                    .setRange(0, 100, 100)
                    .setInitialVisibility(false)
                    .setLineWidth(32f)
                    .build());

//Create data series track
            SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 64, 196, 200))
                    .setRange(0, 100, 0)
                    .setLineWidth(32f)
                    .build();

            int series1Index = graph.addSeries(seriesItem1);

            graph.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                    .setDelay(1000)
                    .setDuration(2000)
                    .build());

            graph.addEvent(new DecoEvent.Builder(25).setIndex(series1Index).setDelay(4000).build());
            graph.addEvent(new DecoEvent.Builder(100).setIndex(series1Index).setDelay(8000).build());
            graph.addEvent(new DecoEvent.Builder(10).setIndex(series1Index).setDelay(12000).build());
        }
    }
}