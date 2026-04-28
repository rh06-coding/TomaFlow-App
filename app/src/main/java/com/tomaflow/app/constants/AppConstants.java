package com.tomaflow.app.constants;

/**
 * AppConstants — Application-wide configuration constants
 *
 * Contains all configurable values for the Pomodoro timer, shared preferences keys,
 * notification IDs, and other app-level constants.
 *
 * These values can be overridden via SharedPreferences for user customization.
 */
public final class AppConstants {

    private AppConstants() {
        // Prevent instantiation
    }


    /** Default work session duration: 25 minutes */
    public static final long TIMER_WORK_DURATION_MS = 25 * 60 * 1000L;

    /** Default short break duration: 5 minutes */
    public static final long TIMER_SHORT_BREAK_MS = 5 * 60 * 1000L;

    /** Default long break duration: 15 minutes */
    public static final long TIMER_LONG_BREAK_MS = 15 * 60 * 1000L;

    /** Number of work cycles before a long break (after 4 work sessions → long break) */
    public static final int TIMER_CYCLES_BEFORE_LONG_BREAK = 4;

    /** Timer countdown interval (in milliseconds) — ticks every 1 second */
    public static final long TIMER_COUNTDOWN_INTERVAL_MS = 1000L;


    /** SharedPreferences file name */
    public static final String PREFERENCES_FILE_NAME = "tomaflow_prefs";

    /** User-configured work duration in minutes */
    public static final String PREF_WORK_DURATION_MIN = "pref_work_duration_min";

    /** User-configured short break duration in minutes */
    public static final String PREF_SHORT_BREAK_MIN = "pref_short_break_min";

    /** User-configured long break duration in minutes */
    public static final String PREF_LONG_BREAK_MIN = "pref_long_break_min";

    /** Enable/disable sound notifications on phase completion */
    public static final String PREF_SOUND_ENABLED = "pref_sound_enabled";

    /** Enable/disable vibration notifications on phase completion */
    public static final String PREF_VIBRATION_ENABLED = "pref_vibration_enabled";

    /** Number of cycles before long break (customizable) */
    public static final String PREF_CYCLES_BEFORE_LONG_BREAK = "pref_cycles_before_long_break";


    /** Notification channel ID for timer updates */
    public static final String NOTIFICATION_CHANNEL_TIMER = "timer_channel";

    /** Notification channel ID for sound/vibration alerts */
    public static final String NOTIFICATION_CHANNEL_SOUND = "sound_channel";

    /** Notification ID for the persistent foreground service notification */
    public static final int NOTIFICATION_ID_TIMER = 1001;

    /** Notification ID for completion alerts */
    public static final int NOTIFICATION_ID_PHASE_COMPLETE = 1002;


    /** Delay before auto-stopping the TimerService after timer goes idle (in milliseconds) */
    public static final long SERVICE_AUTO_STOP_DELAY_MS = 5 * 60 * 1000L;  // 5 minutes


    /** Vibration pattern for phase completion: short buzz (100 ms) */
    public static final long[] VIBRATION_PATTERN_PHASE_COMPLETE = {0, 100};

    /** Vibration pattern for session completion: triple buzz (200-100-200 ms) */
    public static final long[] VIBRATION_PATTERN_SESSION_COMPLETE = {0, 200, 100, 200};


    /** Intent extra key for timer command (START, PAUSE, SKIP, RESET) */
    public static final String INTENT_EXTRA_COMMAND = "timer_command";

    /** Intent extra key for current task ID */
    public static final String INTENT_EXTRA_TASK_ID = "task_id";


    /** Database file name */
    public static final String DATABASE_NAME = "tomaflow.db";

    /** Database version */
    public static final int DATABASE_VERSION = 1;


    /** Animation duration for UI transitions (in milliseconds) */
    public static final long ANIMATION_DURATION_MS = 400L;
}


