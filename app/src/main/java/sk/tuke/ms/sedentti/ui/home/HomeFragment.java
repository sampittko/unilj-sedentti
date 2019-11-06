package sk.tuke.ms.sedentti.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import sk.tuke.ms.sedentti.R;
import sk.tuke.ms.sedentti.model.Session;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private final int TIMELINE_ITEM_HEIGHT = 60;

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
        return root;
    }

    private void makeTimeline(ArrayList<Session> sessions) {
        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.f_home_layout_timeline);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < sessions.size(); i++) {
            View view = inflater.inflate(R.layout.item_timeline_home, linearLayout, false);
            linearLayout.addView(view);
        }
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TIMELINE_ITEM_HEIGHT * sessions.size(), getResources().getDisplayMetrics());
        linearLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void onResume() {
        super.onResume();


        Session session1 = new Session();
        Session session2 = new Session();
        Session session3 = new Session();

        ArrayList<Session> sessions = new ArrayList<>();
        sessions.add(session1);
        sessions.add(session2);
        sessions.add(session3);

        makeTimeline(sessions);

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