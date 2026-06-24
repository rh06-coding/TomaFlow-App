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
        bindLanguageToggle(view);
        bindToggles(view);

        return view;
    }

    private void bindToggles(View view) {
        MaterialSwitch strictSwitch = view.findViewById(R.id.switch_strict);
        if (strictSwitch != null) {
            strictSwitch.setChecked(mSettingsManager.isStrictMode());
            strictSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                com.tomaflow.app.data.repository.SubscriptionManager subManager =
                        new com.tomaflow.app.data.repository.SubscriptionManager(requireContext());
                if (isChecked && !subManager.isVip()) {
                    btn.setChecked(false);
                    com.tomaflow.app.ui.premium.PremiumGateDialog.newInstance().show(getChildFragmentManager(), "PremiumGateDialog");
                } else {
                    mSettingsManager.setStrictMode(isChecked);
                }
            });
        }

        MaterialSwitch dndSwitch = view.findViewById(R.id.switch_dnd);
        if (dndSwitch != null) {
            dndSwitch.setChecked(mSettingsManager.isDndMode());
            dndSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                mSettingsManager.setDndMode(isChecked);
            });
        }

        MaterialSwitch autoStartBreakSwitch = view.findViewById(R.id.switch_auto_start_break);
        if (autoStartBreakSwitch != null) {
            autoStartBreakSwitch.setChecked(mSettingsManager.isAutoStartBreak());
            autoStartBreakSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                mSettingsManager.setAutoStartBreak(isChecked);
                android.content.Intent intent = new android.content.Intent(com.tomaflow.app.timer.TimerEngineService.ACTION_COMMAND);
                intent.setClass(requireContext(), com.tomaflow.app.timer.TimerEngineService.class);
                intent.putExtra(com.tomaflow.app.constants.AppConstants.INTENT_EXTRA_COMMAND, com.tomaflow.app.constants.AppConstants.COMMAND_RELOAD_SETTINGS);
                requireContext().startService(intent);
            });
        }
    }

    private void bindLanguageToggle(View view) {
        com.google.android.material.button.MaterialButtonToggleGroup toggleLang =
                view.findViewById(R.id.toggle_lang);
        if (toggleLang == null) return;

        String currentLang = com.tomaflow.app.utils.LanguageManager.getSavedLanguage(requireContext());
        toggleLang.check(com.tomaflow.app.utils.LanguageManager.LANG_VI.equals(currentLang)
                ? R.id.btn_lang_vi : R.id.btn_lang_en);

        toggleLang.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String lang = (checkedId == R.id.btn_lang_vi)
                    ? com.tomaflow.app.utils.LanguageManager.LANG_VI
                    : com.tomaflow.app.utils.LanguageManager.LANG_EN;
            com.tomaflow.app.utils.LanguageManager.setLanguage(requireContext(), lang);
            // Restart host activity to apply
            requireActivity().recreate();
        });
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
        
        slider.setOnTouchListener((v, event) -> {
            com.tomaflow.app.data.repository.SubscriptionManager sm = new com.tomaflow.app.data.repository.SubscriptionManager(requireContext());
            if (!sm.isVip()) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    com.tomaflow.app.ui.premium.PremiumGateDialog.newInstance().show(getChildFragmentManager(), "PremiumGateDialog");
                }
                return true;
            }
            return false;
        });

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
