package com.tomaflow.app.ui.rewards;

public class BadgeItem {
    private final String id;
    private final String title;
    private final String description;
    private final int iconResId;
    private boolean isUnlocked;

    public BadgeItem(String id, String title, String description, int iconResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.isUnlocked = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
    public boolean isUnlocked() { return isUnlocked; }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}
