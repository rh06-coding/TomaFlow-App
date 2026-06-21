package com.tomaflow.app.data.model;

public class BadgeItem {
    private final String id;
    private final int titleResId;
    private final int descResId;
    private final int iconResId;
    private boolean isUnlocked;

    public BadgeItem(String id, int titleResId, int descResId, int iconResId) {
        this.id = id;
        this.titleResId = titleResId;
        this.descResId = descResId;
        this.iconResId = iconResId;
        this.isUnlocked = false;
    }

    public String getId() { return id; }
    public int getTitleResId() { return titleResId; }
    public int getDescResId() { return descResId; }
    public int getIconResId() { return iconResId; }
    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
}
