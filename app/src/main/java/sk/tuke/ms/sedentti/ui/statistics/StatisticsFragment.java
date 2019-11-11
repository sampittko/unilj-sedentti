package sk.tuke.ms.sedentti.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import sk.tuke.ms.sedentti.R;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;
    private BarChart chart;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        statisticsViewModel = ViewModelProviders.of(this).get(StatisticsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        RecyclerView recyclerViewTimeline = root.findViewById(R.id.f_statistics_layout_timeline);
        recyclerViewTimeline.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewTimeline.setHasFixedSize(true);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL);
        recyclerViewTimeline.addItemDecoration(itemDecor);

        TimelineAdapter timelineAdapter = new TimelineAdapter(getActivity());

        TimelineAdapter27 timelineAdapter27 = new TimelineAdapter27(getActivity());
        recyclerViewTimeline.setAdapter(timelineAdapter27);

        this.chart = root.findViewById(R.id.wc_f_statistics_graph);

        statisticsViewModel.getSessions().observe(this, sessions -> {
            timelineAdapter.setSessions(sessions);
        });

        statisticsViewModel.getDayModels().observe(this, dayModels -> {
            timelineAdapter27.setDayModelsList(dayModels);
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        makeGraph();
    }

    private void makeGraph() {
        List<BarEntry> entries = new ArrayList<BarEntry>();
        entries.add(new BarEntry(0f, 40f));
        entries.add(new BarEntry(1f, 70f));
        entries.add(new BarEntry(2f, 80f));
        entries.add(new BarEntry(3f, 90f));
        entries.add(new BarEntry(4f, 40f));
        entries.add(new BarEntry(5f, 30f));
        entries.add(new BarEntry(6f, 50f));

        BarDataSet set = new BarDataSet(entries, "BarDataSet");

        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        chart.setData(data);
        chart.setFitBars(true); // make the x-axis fit exactly all bars
        chart.invalidate(); // refresh
    }
}