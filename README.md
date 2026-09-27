# DFM Country Toy

A tiny app that reproduces a "detect country with **zero code**" trick, running
live on a device/emulator.

**What you'll see:** the app shows a detected country (Korea / Japan / Unknown).
It figures this out *not* from GPS or IP, but purely from **which Dynamic Feature
Module is installed** — even though those modules contain no code at all.

---

## Core idea

- Create one **empty marker module** per country (zero code, `hasCode="false"`).
- The app only reads `splitInstallManager.installedModules`.
- If `countrycodekr` is installed → Korea; if `countrycodejp` → Japan.
- In other words, **"the fact that it is installed" is itself the data.**

The whole logic lives in `app/.../MainActivity.kt`.

---

## Requirements

- Android Studio (recent) with **JDK 21–23** for Gradle, *or* JDK 25 as configured
  here (the project uses **Gradle 9.1 + AGP 8.13.2**, which run on JDK 25).
- Android SDK 35, an emulator or a connected device.
- [`bundletool`](https://github.com/google/bundletool) for the recommended run
  path below (`brew install bundletool`).

---

## Run it (recommended: bundletool local testing)

This is the path that actually shows the on-demand behavior — modules start
**uninstalled**, and you install them from the in-app buttons.

```bash
brew install bundletool                       # once

./gradlew :app:bundleDebug                    # build the AAB
bundletool build-apks --local-testing \
  --bundle=app/build/outputs/bundle/debug/app-debug.aab \
  --output=/tmp/dfmtoy.apks --overwrite
bundletool install-apks --apks=/tmp/dfmtoy.apks
```

Then, on the device:

1. First launch shows **Unknown** — no country module installed yet.
2. Tap **Install countrycodekr** → the headline flips to **🇰🇷 Korea**.
3. Notice that `Installed modules` actually changed — that single fact is what
   drove the detection.

To reset: `adb uninstall com.example.dfmtoy`, then re-run `install-apks`.

> **Why not just Run ▶ from Android Studio?** A normal Studio run pre-installs
> ALL feature modules, so everything shows as installed from the start and the
> on-demand effect disappears. Use the bundletool path above to see it properly.

---

## Level 2 — real automatic country detection (needs Play)

The steps above prove the *mechanism*. To see Google Play auto-install a module
based on the user's real country:

1. In each module's `AndroidManifest.xml`, remove `<dist:on-demand/>` and switch
   to the commented `<dist:install-time>` + `<dist:user-countries>` block.
2. Build a release AAB: `./gradlew :app:bundleRelease`.
3. Upload to the Play Console **Internal testing** track.
4. Install with accounts from different countries and confirm each country only
   receives its own module.

### How Play decides the country

Play uses the **Google account (Play Store) country**, not GPS/IP. Its root is
usually the **billing profile country**, so it does not change with travel or VPN.

> Country-based conditional delivery is evaluated on Play's servers, so a
> bundletool device-spec alone cannot fully reproduce it. Verify the real
> behavior via Play internal testing.
