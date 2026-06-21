package com.tomaflow.app.data.model;

/**
 * Represents a built-in background music track bundled in res/raw/.
 */
public class BuiltInTrack {
    public final String id;
    public final String name;
    public final String category;
    public final String emoji;
    public final int rawResId; // R.raw.xxx — 0 if not applicable (e.g. device track wrapper)

    public BuiltInTrack(String id, String name, String category, String emoji, int rawResId) {
        this.id       = id;
        this.name     = name;
        this.category = category;
        this.emoji    = emoji;
        this.rawResId = rawResId;
    }
}
