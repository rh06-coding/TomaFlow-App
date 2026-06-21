package com.tomaflow.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * Manages app-level language switching (EN / VI).
 *
 * Usage:
 *  1. In every Activity, override attachBaseContext:
 *       super.attachBaseContext(LanguageManager.wrap(base));
 *  2. When the user picks a language, call:
 *       LanguageManager.setLanguage(context, langCode);
 *       requireActivity().recreate();
 */
public class LanguageManager {

    private static final String PREF_NAME = "lang_prefs";
    private static final String KEY_LANG  = "selected_language";
    public  static final String LANG_EN   = "en";
    public  static final String LANG_VI   = "vi";

    // ── Read / Write preference ───────────────────────────────────────────────

    public static String getSavedLanguage(Context context) {
        return prefs(context).getString(KEY_LANG, LANG_EN);
    }

    public static void setLanguage(Context context, String langCode) {
        prefs(context).edit().putString(KEY_LANG, langCode).apply();
    }

    // ── Apply to a Context (call from attachBaseContext) ──────────────────────

    /**
     * Wraps the given base context with the persisted locale.
     * Call this from every Activity#attachBaseContext.
     */
    public static Context wrap(Context base) {
        return wrap(base, getSavedLanguage(base));
    }

    public static Context wrap(Context base, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(base.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(new LocaleList(locale));
        } else {
            config.setLocale(locale);
        }
        return base.createConfigurationContext(config);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
