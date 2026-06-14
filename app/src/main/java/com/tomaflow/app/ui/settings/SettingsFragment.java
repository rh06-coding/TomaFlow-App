package com.tomaflow.app.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.tomaflow.app.R;
import com.tomaflow.app.timer.SettingsManager;

import java.util.Locale;
import java.util.function.IntConsumer;

public class SettingsFragment extends Fragment {

    private SettingsManager mSettingsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mSettingsManager = new SettingsManager(requireContext());

        View avatar = view.findViewById(R.id.iv_avatar);
        if (avatar != null) {
            avatar.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_profile));
        }

        bindDarkMode(view);
        bindDurationSettings(view);

        return view;
    }

    private void bindDarkMode(View view) {
        MaterialSwitch switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        int currentMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO));
    }

    /**
     * Drive the duration sliders and labels from the settings source of truth
     * (user value, else AppConstants) and persist user changes back to it. The
     * timer service reads the same SettingsManager when starting a fresh session,
     * so a change here takes effect on the next session.
     */
    private void bindDurationSettings(View view) {
        bindDuration(view.findViewById(R.id.slider_focus), view.findViewById(R.id.tv_focus_value),
                mSettingsManager.getFocusDurationMs(), mSettingsManager::setFocusDurationMinutes);
        bindDuration(view.findViewById(R.id.slider_short), view.findViewById(R.id.tv_short_value),
                mSettingsManager.getShortBreakDurationMs(), mSettingsManager::setShortBreakDurationMinutes);
        bindDuration(view.findViewById(R.id.slider_long), view.findViewById(R.id.tv_long_value),
                mSettingsManager.getLongBreakDurationMs(), mSettingsManager::setLongBreakDurationMinutes);
    }

    private void bindDuration(Slider slider, TextView label, long durationMs, IntConsumer persist) {
        int minutes = (int) (durationMs / 60000L);
        label.setText(formatMinutes(minutes));
        slider.setValue(snapToSlider(slider, minutes));
        slider.addOnChangeListener((s, value, fromUser) -> {
            label.setText(formatMinutes((int) value));
            if (fromUser) {
                persist.accept((int) value);
            }
        });
    }

    /** Clamp to the slider's range and align to its step so setValue never throws. */
    private float snapToSlider(Slider slider, float value) {
        float from = slider.getValueFrom();
        float to = slider.getValueTo();
        float step = slider.getStepSize();
        float clamped = Math.max(from, Math.min(to, value));
        if (step > 0f) {
            clamped = from + Math.round((clamped - from) / step) * step;
            clamped = Math.max(from, Math.min(to, clamped));
        }
        return clamped;
    }

    private String formatMinutes(int minutes) {
        return String.format(Locale.getDefault(), "%dm", minutes);
    }
}
