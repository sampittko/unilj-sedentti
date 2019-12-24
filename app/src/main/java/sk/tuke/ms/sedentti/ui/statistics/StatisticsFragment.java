package sk.tuke.ms.sedentti.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import sk.tuke.ms.sedentti.R;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;
//    private BarChart chart;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                this.statisticsViewModel.updateModel();
                return true;
            case R.id.action_settings:
                return false;
        }
        return false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        statisticsViewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        RecyclerView recyclerViewTimeline = root.findViewById(R.id.f_statistics_layout_timeline);
        recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewTimeline.setHasFixedSize(true);

        TimelineAdapter timelineAdapter = new TimelineAdapter(getActivity());
        recyclerViewTimeline.setAdapter(timelineAdapter);

//        this.chart = root.findViewById(R.id.wc_f_statistics_graph);

        statisticsViewModel.getDayModels().observe(this, dayModels -> {
            timelineAdapter.setDayModelsList(dayModels);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
//        makeGraph();
    }

//    private void makeGraph() {
//        List<BarEntry> entries = new ArrayList<BarEntry>();
//        entries.add(new BarEntry(0f, 40f));
//        entries.add(new BarEntry(1f, 70f));
//        entries.add(new BarEntry(2f, 80f));
//        entries.add(new BarEntry(3f, 90f));
//        entries.add(new BarEntry(4f, 40f));
//        entries.add(new BarEntry(5f, 30f));
//        entries.add(new BarEntry(6f, 50f));
//
//        BarDataSet set = new BarDataSet(entries, "BarDataSet");
//
//        BarData data = new BarData(set);
//        data.setBarWidth(0.9f); // set custom bar width
//        chart.setData(data);
//        chart.setFitBars(true); // make the x-axis fit exactly all bars
//        chart.invalidate(); // refresh
//    }
}