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
import androidx.lifecycle.LiveData;
import androidx.navigation.Navigation;
import android.content.Intent;

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
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.tomaflow.app.R;
import com.tomaflow.app.data.db.dao.SessionDao;
import com.tomaflow.app.data.db.entity.SessionEntity;
import com.tomaflow.app.data.repository.SessionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private static final long DAY_MS = 24L * 60L * 60L * 1000L;
    private static final int RANGE_WEEK_DAYS = 7;
    private static final int RANGE_MONTH_DAYS = 30;

    private SessionRepository mSessionRepository;

    private BarChart mBarChart;
    private PieChart mPieChart;
    private TextView mTvTotalFocus;
    private TextView mTvPomos;
    private TextView mTvBestDay;

    // Current range's LiveData sources, observed against the view lifecycle.
    // Re-created when the user switches range; old observers are detached first.
    private LiveData<List<SessionDao.DailyStatRow>> mDailyStats;
    private LiveData<List<SessionEntity>> mSessions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        
        View avatar = view.findViewById(R.id.iv_avatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_profile));
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.tomaflow.app.utils.HeaderUIHelper.setupHeader(view, getString(R.string.nav_stats), getViewLifecycleOwner());

        mSessionRepository = new SessionRepository(requireActivity().getApplication());

        mBarChart = view.findViewById(R.id.bar_chart);
        mPieChart = view.findViewById(R.id.pie_chart);
        mTvTotalFocus = view.findViewById(R.id.tv_total_focus_value);
        mTvPomos = view.findViewById(R.id.tv_pomos_value);
        mTvBestDay = view.findViewById(R.id.tv_best_day_value);

        configureBarChart();
        configurePieChart();

        MaterialButtonToggleGroup rangeToggle = view.findViewById(R.id.range_toggle);
        rangeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            highlightActiveRange(view, checkedId);
            observeRange(checkedId == R.id.btn_month ? RANGE_MONTH_DAYS : RANGE_WEEK_DAYS);
        });

        rangeToggle.check(R.id.btn_week);
        highlightActiveRange(view, R.id.btn_week);
        observeRange(RANGE_WEEK_DAYS);

        // Journal logic
        NoteViewModel noteViewModel = new androidx.lifecycle.ViewModelProvider(this).get(NoteViewModel.class);
        NoteAdapter noteAdapter = new NoteAdapter(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onEdit(com.tomaflow.app.data.db.entity.NoteEntity note) {
                AddNoteBottomSheet bottomSheet = AddNoteBottomSheet.newInstance(note.noteId, note.title, note.content, note.mood);
                bottomSheet.show(getChildFragmentManager(), "AddNoteBottomSheet");
            }

            @Override
            public void onDelete(com.tomaflow.app.data.db.entity.NoteEntity note) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.confirm_delete_note_title)
                    .setMessage(R.string.confirm_delete_note_msg)
                    .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                        noteViewModel.delete(note);
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
            }
        });
        androidx.recyclerview.widget.RecyclerView recyclerNotes = view.findViewById(R.id.recycler_notes);
        recyclerNotes.setAdapter(noteAdapter);

        TextView tvJournalEmpty = view.findViewById(R.id.tv_journal_empty);

        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            if (notes == null || notes.isEmpty()) {
                tvJournalEmpty.setVisibility(View.VISIBLE);
                recyclerNotes.setVisibility(View.GONE);
                noteAdapter.submitList(null);
            } else {
                tvJournalEmpty.setVisibility(View.GONE);
                recyclerNotes.setVisibility(View.VISIBLE);
                // Only show the latest note
                noteAdapter.submitList(notes.subList(0, 1));
            }
        });

        view.findViewById(R.id.btn_add_note).setOnClickListener(v -> {
            AddNoteBottomSheet.newInstance(null, null, null, null).show(getChildFragmentManager(), "AddNoteBottomSheet");
        });

        View btnViewAll = view.findViewById(R.id.btn_view_all_notes);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), JournalActivity.class));
            });
        }
    }

    private void highlightActiveRange(View view, int checkedId) {
        int active = ContextCompat.getColor(requireContext(), R.color.toma_primary);
        int inactive = ContextCompat.getColor(requireContext(), R.color.toma_text_muted);
        ((TextView) view.findViewById(R.id.btn_week))
                .setTextColor(checkedId == R.id.btn_week ? active : inactive);
        ((TextView) view.findViewById(R.id.btn_month))
                .setTextColor(checkedId == R.id.btn_month ? active : inactive);
    }

    /** Point both charts at the last {@code days} of data, replacing any prior observers. */
    private void observeRange(int days) {
        long since = System.currentTimeMillis() - (long) days * DAY_MS;

        if (mDailyStats != null) {
            mDailyStats.removeObservers(getViewLifecycleOwner());
        }
        if (mSessions != null) {
            mSessions.removeObservers(getViewLifecycleOwner());
        }

        mDailyStats = mSessionRepository.getDailyStatsSince(since);
        mSessions = mSessionRepository.getSessionsSince(since);

        mDailyStats.observe(getViewLifecycleOwner(), this::renderWeeklyStats);
        mSessions.observe(getViewLifecycleOwner(), this::renderSessionBreakdown);
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
        xAxis.setCenterAxisLabels(true);  // căn giữa label giữa 2 cột
        xAxis.setValueFormatter(new IndexAxisValueFormatter(StatsAggregator.DAY_LABELS));
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(StatsAggregator.DAYS_IN_WEEK);

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
        com.tomaflow.app.timer.SettingsManager settings = new com.tomaflow.app.timer.SettingsManager(requireContext());
        int shortBreakMinutes = (int) (settings.getShortBreakDurationMs() / 60000L);

        float[] focusByDay = StatsAggregator.minutesByDay(rows);
        float[] breakByDay = StatsAggregator.breakMinutesByDay(rows, shortBreakMinutes);
        int totalMinutes = StatsAggregator.totalMinutes(rows);
        int totalCycles  = StatsAggregator.totalCycles(rows);
        int bestDay      = StatsAggregator.bestDayIndex(focusByDay);

        mTvTotalFocus.setText(formatDuration(totalMinutes));
        mTvPomos.setText(String.valueOf(totalCycles));
        if (bestDay >= 0) {
            mTvBestDay.setText(String.format(Locale.getDefault(), "%s %.1fh",
                    StatsAggregator.DAY_LABELS[bestDay], focusByDay[bestDay] / 60f));
        } else {
            mTvBestDay.setText("—");
        }

        // ── Grouped Bar Chart: Focus (red) + Break (green) ──
        List<BarEntry> focusEntries = new ArrayList<>();
        List<BarEntry> breakEntries = new ArrayList<>();
        for (int i = 0; i < StatsAggregator.DAYS_IN_WEEK; i++) {
            focusEntries.add(new BarEntry(i, focusByDay[i]));
            breakEntries.add(new BarEntry(i, breakByDay[i]));
        }

        BarDataSet focusSet = new BarDataSet(focusEntries, getString(R.string.stats_focus_label));
        focusSet.setColor(ContextCompat.getColor(requireContext(), R.color.toma_primary));
        focusSet.setDrawValues(false);

        BarDataSet breakSet = new BarDataSet(breakEntries, getString(R.string.stats_breaks_label));
        breakSet.setColor(ContextCompat.getColor(requireContext(), R.color.toma_success));
        breakSet.setDrawValues(false);

        float groupSpace  = 0.2f;   // khoảng cách giữa các ngày
        float barSpace    = 0.04f;  // khoảng cách giữa 2 cột trong một ngày
        float barWidth    = 0.36f;  // (2 * 0.36) + (2 * 0.04) + 0.2 = 1.0

        BarData data = new BarData(focusSet, breakSet);
        data.setBarWidth(barWidth);
        mBarChart.setData(data);

        // Group cần gọi sau setData
        mBarChart.groupBars(0f, groupSpace, barSpace);
        mBarChart.getXAxis().setAxisMinimum(0f);
        mBarChart.getXAxis().setAxisMaximum(StatsAggregator.DAYS_IN_WEEK);
        mBarChart.getXAxis().setCenterAxisLabels(true);
        mBarChart.setVisibleXRangeMaximum(StatsAggregator.DAYS_IN_WEEK);
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
