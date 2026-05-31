# FF Loadout — Android App
## Your AdMob IDs are already inserted ✅
- App ID:     ca-app-pub-7729356342196661~6173740267
- Ad Unit ID: ca-app-pub-7729356342196661/8406667301

---

## HOW TO BUILD THE APK (Step by Step)

### STEP 1 — Install Android Studio
Download from: https://developer.android.com/studio
Install it (takes ~10 minutes)

### STEP 2 — Open This Project
1. Open Android Studio
2. Click "Open"
3. Select this "FFLoadout" folder
4. Wait for Gradle to sync (bottom bar shows progress)

### STEP 3 — Build the APK
1. Top menu → Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait ~2 minutes
3. Click "locate" in the popup at the bottom right
4. Your APK is at:
   app/build/outputs/apk/debug/app-debug.apk

### STEP 4 — Install on Phone
1. Copy the APK to your Android phone
2. Settings → Security → Enable "Unknown Sources" (or "Install unknown apps")
3. Open the APK file on your phone
4. Tap Install

---

## HOW ADS WORK IN YOUR APP

- User presses "ENTER FREE FIRE" → rewarded ad plays (30 sec) → Free Fire opens
- User presses "APPLY LOADOUT" → rewarded ad plays (30 sec) → loadout applies
- If ad hasn't loaded yet → loading spinner shows → ad plays when ready
- After each ad → next ad preloads automatically

---

## TO PUBLISH ON PLAY STORE (to earn more)

1. Build → Generate Signed Bundle/APK → APK
2. Create a keystore (Android Studio guides you)
3. Go to play.google.com/console
4. Create app → upload APK → fill details → publish

Play Store apps earn MORE ad revenue than sideloaded APKs.

---

## FILES EXPLAINED

| File | What it does |
|------|-------------|
| MainActivity.java | Loads the app, handles ads, bridges HTML↔Java |
| AndroidManifest.xml | App config + your AdMob App ID |
| assets/freefire_loadout.html | Your full app UI |
| res/layout/activity_main.xml | WebView container |
| app/build.gradle | AdMob SDK dependency |
