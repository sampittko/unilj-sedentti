package sk.tuke.ms.sedentti.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.helper.TimeHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ProfileHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class HomeFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private HomeViewModel homeViewModel;
    private final int TIMELINE_ITEM_HEIGHT = 60;
    private LinearLayout timelineLayout;
    private SessionHelper sessionHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
//        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

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
            String sessionName;
            if (session.isSedentary()) {
                sessionName = getResources().getString(R.string.home_timeline_name_sedentary) + " " + index_sedentary;
                index_sedentary++;
            } else {
                sessionName = getResources().getString(R.string.home_timeline_name_activity) + " " + index_active;
                index_active++;
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

        ProfileHelper profileHelper = new ProfileHelper(getActivity().getApplicationContext());

        Profile activeProfile = null;
        try {
            activeProfile = profileHelper.getActiveProfile();
        } catch (SQLException e) {
            Toast.makeText(getContext(), "Error, no profile", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        this.sessionHelper = new SessionHelper(getActivity().getApplicationContext(), activeProfile);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            makeTimeline((ArrayList<Session>) sessionHelper.getSessionsInInterval(SessionHelper.SessionsInterval.LAST_DAY));
        } catch (SQLException e) {
            Log.w(TAG, "No sessions for today");
            e.printStackTrace();
        }

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