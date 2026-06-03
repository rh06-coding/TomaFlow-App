# Phase 4 — Firebase Auth Fixes and Features Implementation Plan

This plan outlines the steps to fix the launcher activity, remove the debug package suffix, implement Google Sign-In, and add sign-out functionality.

## User Review Required

- **Google Sign-In**: I'll be adding a Google Sign-In button to the login screen. I'll use a standard Material button for now unless you prefer a specific Google-branded one.

## Proposed Changes

### Build and Manifest Fixes

#### [AndroidManifest.xml](file:///D:/SE114/TomaFlow-App/app/src/main/AndroidManifest.xml)
- Move the `intent-filter` for `MAIN`/`LAUNCHER` from `RegisterActivity` to `LoginActivity`.
- Set `exported="true"` for `LoginActivity` and `exported="false"` for `RegisterActivity`.

#### [build.gradle](file:///D:/SE114/TomaFlow-App/app/build.gradle)
- Verify `applicationIdSuffix ".debug"` is not present (already checked, it's missing, so no change needed unless it was supposed to be there but removed in a way that causes issues).
- Ensure `play-services-auth` dependency is present (it is already there).

---

### Authentication Features

#### [LoginActivity.java](file:///D:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/auth/LoginActivity.java)
- Implement Google Sign-In flow using `GoogleSignInClient` and `ActivityResultLauncher`.
- Handle `firebaseAuthWithGoogle` to authenticate with Firebase after successful Google Sign-In.

#### [activity_login.xml](file:///D:/SE114/TomaFlow-App/app/src/main/res/layout/activity_login.xml)
- Add a "Sign in with Google" button.

#### [strings.xml](file:///D:/SE114/TomaFlow-App/app/src/main/res/values/strings.xml)
- Add `auth_google` string.

---

### Profile Features

#### [ProfileFragment.java](file:///D:/SE114/TomaFlow-App/app/src/main/java/com/tomaflow/app/ui/profile/ProfileFragment.java)
- Wire up the `btn_logout` button to sign out from Firebase and redirect to `LoginActivity`.
- Update user profile info (name, initials) from `FirebaseUser`.

## Verification Plan

### Automated Tests
- I'll run the existing `PomodoroTimerTest` to ensure no regressions in timer logic.
- `gradlew test`

### Manual Verification
- Deploy the app to a device/emulator.
- Verify `LoginActivity` is the first screen shown.
- Test Email/Password login.
- Test Google Sign-In.
- Test Sign Out from Profile screen.
- Verify that after sign out, opening the app again leads to `LoginActivity`.
