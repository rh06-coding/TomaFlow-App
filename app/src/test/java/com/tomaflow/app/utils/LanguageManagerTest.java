package com.tomaflow.app.utils;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class LanguageManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("lang_prefs", Context.MODE_PRIVATE)
                .edit().clear().commit();
    }

    @Test
    public void getSavedLanguage_default_isEnglish() {
        assertEquals(LanguageManager.LANG_EN, LanguageManager.getSavedLanguage(context));
    }

    @Test
    public void setLanguage_vietnamese_persists() {
        LanguageManager.setLanguage(context, LanguageManager.LANG_VI);
        assertEquals(LanguageManager.LANG_VI, LanguageManager.getSavedLanguage(context));
    }

    @Test
    public void setLanguage_english_persists() {
        LanguageManager.setLanguage(context, LanguageManager.LANG_VI);
        LanguageManager.setLanguage(context, LanguageManager.LANG_EN);
        assertEquals(LanguageManager.LANG_EN, LanguageManager.getSavedLanguage(context));
    }

    @Test
    public void wrap_returnsNonNullContext() {
        Context wrapped = LanguageManager.wrap(context, LanguageManager.LANG_VI);
        assertEquals(true, wrapped != null);
    }

    @Test
    public void wrap_withDefaultLanguage_returnsNonNullContext() {
        Context wrapped = LanguageManager.wrap(context);
        assertEquals(true, wrapped != null);
    }

    @Test
    public void langConstants_areDistinct() {
        assertEquals(false, LanguageManager.LANG_EN.equals(LanguageManager.LANG_VI));
    }
}
