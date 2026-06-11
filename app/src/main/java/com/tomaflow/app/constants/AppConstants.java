package com.tomaflow.app.constants;

public final class AppConstants {

    private AppConstants() {}

    public static final long TIMER_WORK_DURATION_MS = 25 * 60 * 1000L;
    public static final long TIMER_SHORT_BREAK_MS = 5 * 60 * 1000L;
    public static final long TIMER_LONG_BREAK_MS = 15 * 60 * 1000L;
    public static final int TIMER_CYCLES_BEFORE_LONG_BREAK = 4;
    public static final long TIMER_COUNTDOWN_INTERVAL_MS = 1000L;
    public static final int TIMER_DEFAULT_TARGET_SESSIONS = 8;

    public static final String PREFERENCES_FILE_NAME = "tomaflow_prefs";
    public static final String PREF_WORK_DURATION_MIN = "pref_work_duration_min";
    public static final String PREF_SHORT_BREAK_MIN = "pref_short_break_min";
    public static final String PREF_LONG_BREAK_MIN = "pref_long_break_min";
    public static final String PREF_SOUND_ENABLED = "pref_sound_enabled";
    public static final String PREF_VIBRATION_ENABLED = "pref_vibration_enabled";
    public static final String PREF_CYCLES_BEFORE_LONG_BREAK = "pref_cycles_before_long_break";

    public static final String NOTIFICATION_CHANNEL_TIMER = "timer_channel";
    public static final String NOTIFICATION_CHANNEL_SOUND = "sound_channel";
    public static final int NOTIFICATION_ID_TIMER = 1001;
    public static final int NOTIFICATION_ID_PHASE_COMPLETE = 1002;

    // Auto-stop service after 5 min of no user interaction
    public static final long SERVICE_AUTO_STOP_DELAY_MS = 5 * 60 * 1000L;

    // Vibration patterns: [delay, vibrate, delay, vibrate, ...] in ms
    public static final long[] VIBRATION_PATTERN_PHASE_COMPLETE = {0, 100};           // single pulse
    public static final long[] VIBRATION_PATTERN_SESSION_COMPLETE = {0, 200, 100, 200}; // double pulse

    public static final String INTENT_EXTRA_COMMAND = "timer_command";
    public static final String INTENT_EXTRA_TASK_ID = "task_id";

    public static final String COMMAND_START_FOCUS = "com.tomaflow.app.COMMAND_START_FOCUS";
    public static final String COMMAND_PAUSE = "com.tomaflow.app.COMMAND_PAUSE";
    public static final String COMMAND_RESUME = "com.tomaflow.app.COMMAND_RESUME";
    public static final String COMMAND_SKIP = "com.tomaflow.app.COMMAND_SKIP";
    public static final String COMMAND_RESET = "com.tomaflow.app.COMMAND_RESET";
    /** Debug only: set timer speed multiplier (1/2/4/8/16). */
    public static final String COMMAND_SET_SPEED    = "com.tomaflow.app.COMMAND_SET_SPEED";
    public static final String INTENT_EXTRA_SPEED   = "timer_speed";

    public static final String DATABASE_NAME = "tomaflow.db";
    public static final int DATABASE_VERSION = 1;

    public static final long ANIMATION_DURATION_MS = 400L;
}
