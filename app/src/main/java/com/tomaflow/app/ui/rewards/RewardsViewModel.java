package com.tomaflow.app.ui.rewards;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.tomaflow.app.data.model.DailyTomato;
import com.tomaflow.app.data.repository.RewardsRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RewardsViewModel extends AndroidViewModel {

    private final RewardsRepository repository;
    private final MutableLiveData<YearMonth> currentMonthLive = new MutableLiveData<>(YearMonth.now());
    private final LiveData<List<DailyTomato>> farmTomatoes;

    public RewardsViewModel(@NonNull Application application) {
        super(application);
        repository = new RewardsRepository(application);

        farmTomatoes = Transformations.switchMap(currentMonthLive, month -> 
            Transformations.map(repository.getMonthlyTomatoes(month.toString()), rawData -> 
                buildCalendarGrid(month, rawData)
            )
        );
    }

    public LiveData<List<DailyTomato>> getDailyTomatoes() {
        return farmTomatoes;
    }

    public LiveData<YearMonth> getCurrentMonth() {
        return currentMonthLive;
    }

    public void nextMonth() {
        YearMonth current = currentMonthLive.getValue();
        if (current != null) {
            currentMonthLive.setValue(current.plusMonths(1));
        }
    }

    public void previousMonth() {
        YearMonth current = currentMonthLive.getValue();
        if (current != null) {
            currentMonthLive.setValue(current.minusMonths(1));
        }
    }

    private List<DailyTomato> buildCalendarGrid(YearMonth month, List<DailyTomato> rawData) {
        List<DailyTomato> grid = new ArrayList<>();
        Map<String, DailyTomato> dataMap = rawData.stream()
                .collect(Collectors.toMap(DailyTomato::getDateStr, t -> t));

        LocalDate firstOfMonth = month.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)

        // Pad start of month
        int paddingDays = dayOfWeek - 1;
        for (int i = 0; i < paddingDays; i++) {
            grid.add(new DailyTomato("", 0, true));
        }

        // Fill days
        int daysInMonth = month.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            String dateStr = month.atDay(i).toString();
            if (dataMap.containsKey(dateStr)) {
                grid.add(dataMap.get(dateStr));
            } else {
                grid.add(new DailyTomato(dateStr, 0, false));
            }
        }

        // Pad end of month
        int remainingCells = 42 - grid.size();
        if (remainingCells >= 7 && grid.size() <= 35) {
            remainingCells = 35 - grid.size(); // Support 5 rows if fits
        }
        for (int i = 0; i < remainingCells; i++) {
            grid.add(new DailyTomato("", 0, true));
        }

        return grid;
    }
}
