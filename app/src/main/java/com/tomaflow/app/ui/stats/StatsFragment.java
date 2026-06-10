package com.tomaflow.app.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.tomaflow.app.R;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.repository.SessionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private SessionRepository mSessionRepository;

    private BarChart mBarChart;
    private PieChart mPieChart;
    private TextView mTvTotalFocus;
    private TextView mTvPomos;
    private TextView mTvBestDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSessionRepository = new SessionRepository(requireActivity().getApplication());

        mBarChart = view.findViewById(R.id.bar_chart);
        mPieChart = view.findViewById(R.id.pie_chart);
        mTvTotalFocus = view.findViewById(R.id.tv_total_focus_value);
        mTvPomos = view.findViewById(R.id.tv_pomos_value);
        mTvBestDay = view.findViewById(R.id.tv_best_day_value);

        configureBarChart();
        configurePieChart();

        mSessionRepository.getWeeklyDailyStats().observe(getViewLifecycleOwner(), this::renderWeeklyStats);
        mSessionRepository.getAllSessions().observe(getViewLifecycleOwner(), this::renderSessionBreakdown);
    }

    private void configureBarChart() {
        mBarChart.setNoDataText(getString(R.string.stats_no_data));
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setScaleEnabled(false);
        mBarChart.setFitBars(true);
        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getLegend().setEnabled(false);

        int textColor = ContextCompat.getColor(requireContext(), R.color.toma_text_muted);

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(StatsAggregator.DAY_LABELS));

        mBarChart.getAxisLeft().setAxisMinimum(0f);
        mBarChart.getAxisLeft().setTextColor(textColor);
        mBarChart.getAxisLeft().setDrawGridLines(false);
    }

    private void configurePieChart() {
        mPieChart.setNoDataText(getString(R.string.stats_no_data));
        Description description = new Description();
        description.setText("");
        mPieChart.setDescription(description);
        mPieChart.setUsePercentValues(false);
        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        mPieChart.setEntryLabelColor(ContextCompat.getColor(requireContext(), R.color.toma_on_primary));

        Legend legend = mPieChart.getLegend();
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.toma_text));
    }

    private void renderWeeklyStats(@Nullable List<SessionDao.DailyStatRow> rows) {
        float[] minutesByDay = StatsAggregator.minutesByDay(rows);
        int totalMinutes = StatsAggregator.totalMinutes(rows);
        int totalCycles = StatsAggregator.totalCycles(rows);
        int bestDay = StatsAggregator.bestDayIndex(minutesByDay);

        mTvTotalFocus.setText(formatDuration(totalMinutes));
        mTvPomos.setText(String.valueOf(totalCycles));
        if (bestDay >= 0) {
            mTvBestDay.setText(String.format(Locale.getDefault(), "%s %.1fh",
                    StatsAggregator.DAY_LABELS[bestDay], minutesByDay[bestDay] / 60f));
        } else {
            mTvBestDay.setText("—");
        }

        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < StatsAggregator.DAYS_IN_WEEK; i++) {
            entries.add(new BarEntry(i, minutesByDay[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.stats_total));
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.toma_primary));
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        mBarChart.setData(data);
        mBarChart.invalidate();
    }

    private void renderSessionBreakdown(@Nullable List<SessionEntity> sessions) {
        int completed = StatsAggregator.countByStatus(sessions, "Completed");
        int failed = StatsAggregator.countByStatus(sessions, "Failed");

        if (completed == 0 && failed == 0) {
            mPieChart.clear();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if (completed > 0) {
            entries.add(new PieEntry(completed, getString(R.string.stats_completed)));
            colors.add(ContextCompat.getColor(requireContext(), R.color.toma_success));
        }
        if (failed > 0) {
            entries.add(new PieEntry(failed, getString(R.string.stats_failed)));
            colors.add(ContextCompat.getColor(requireContext(), R.color.toma_primary));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.toma_on_primary));
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        mPieChart.setData(data);
        mPieChart.invalidate();
    }

    private String formatDuration(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
    }
}
