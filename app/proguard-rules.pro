# proguard-rules.pro
# TomaFlow app — ProGuard / R8 rules for the release build.
#
# Add project-specific rules here. By default no rules are needed for debug.

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}

# ── Keep data classes (entity field names used by Room) ───────────────────────
-keepclassmembers class com.tomaflow.app.data.db.entity.** {
    <fields>;
    <init>(...);
}

# ── Suppress warnings for unused Kotlin runtime (pulled by transitive deps) ───
-dontwarn kotlin.**
