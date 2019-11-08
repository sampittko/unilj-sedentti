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
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.TimeHelper;
import sk.tuke.ms.sedentti.model.Session;

public class HomeFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private HomeViewModel homeViewModel;
    private final int TIMELINE_ITEM_HEIGHT = 60;
    private LinearLayout timelineLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel.getHomeTimelineSessions().observe(this, new Observer<ArrayList<Session>>() {
            @Override
            public void onChanged(ArrayList<Session> sessions) {
                makeTimeline(sessions);
            }
        });

        setOnClickOnViews(root);
        return root;
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

            String sessionTime;
            sessionTime = TimeHelper.formatTime(session.getStartTimestamp());

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
        graphs.add((DecoView) getView().findViewById(R.id.graph_f_home_session));

        for (DecoView graph : graphs) {
            // Create background track
            graph.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                    .setRange(0, 100, 100)
                    .setInitialVisibility(false)
                    .setLineWidth(32f)
                    .build());

//Create data series track
            SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
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