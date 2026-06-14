package com.tomaflow.app.data.model;

import java.util.Objects;

/**
 * POJO class representing an achievement badge.
 * Not stored as a Room Entity, managed dynamically via SharedPreferences and logic.
 */
public class BadgeItem {
    private String id;
    private String title;
    private String description;
    private int iconResId;
    private boolean isUnlocked;

    public BadgeItem(String id, String title, String description, int iconResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.isUnlocked = false; // Default to locked
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BadgeItem badgeItem = (BadgeItem) o;
        return iconResId == badgeItem.iconResId && isUnlocked == badgeItem.isUnlocked && Objects.equals(id, badgeItem.id) && Objects.equals(title, badgeItem.title) && Objects.equals(description, badgeItem.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, iconResId, isUnlocked);
    }
}
