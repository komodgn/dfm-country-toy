# DFM Country Toy

A tiny Android app that detects the user's country with **zero code** — purely from
which Dynamic Feature Module (DFM) is installed, with no GPS and no location
permission.

**What you'll see:** the app shows a detected country (Korea / Japan / Unknown).
Each country is just an **empty marker module** (no code, no resources). The app
reads *which* module is present and infers the country from that single fact.

---

## How it works

- One **empty marker module** per country (`hasCode="false"`, zero code).
- Each module is delivered by Google Play **only to users of that country**
  (`install-time` + `<dist:user-countries>`), so it is auto-installed at install
  time based on the account's Play Store country.
- The app reads `splitInstallManager.installedModules`:
  - `countrycodekr` present → 🇰🇷 Korea
  - `countrycodejp` present → 🇯🇵 Japan
  - neither → Unknown
- In other words, **"the fact that it is installed" is itself the data.**

Core logic: `app/src/main/java/com/example/dfmtoy/MainActivity.kt`.
UI styling follows `DESIGN.md`.

> **Two ways to experience it**
> - **On Google Play** — the real thing: your account's country decides which
>   module is auto-installed, so the app shows your country with no interaction.
> - **Locally (bundletool)** — Play isn't involved, so no country is auto-detected.
>   The in-app buttons let you install a module manually to see the mechanism.

---

## Try it via Google Play (real country detection)

This is where the trick actually shines — install from an internal-test link and
the app shows *your* country automatically.

1. Ask the maintainer to add your Play Store email as a tester
   (closed test — the link won't open until your email is added).
2. Open the [internal-test opt-in link](https://play.google.com/apps/internaltest/4700595280769825488),
   tap **Become a tester**, and install from Play.
3. Launch the app — it shows your country (or "Unknown" if it isn't KR/JP yet).

> **Currently supported:** 🇰🇷 Korea and 🇯🇵 Japan. Any other Play Store country
> resolves to **Unknown** — that's the expected fallback, not a bug. Adding your
> country is a great first PR (see below).

> Play decides the country from your **Google account (Play Store) country**, which
> is rooted in your **billing profile** — not GPS/IP. It doesn't change with travel
> or VPN.

---

## Run it locally (see the mechanism, no Play needed)

Local installs don't evaluate country conditions, so nothing is auto-detected.
Use the in-app buttons to install a marker module by hand and watch the detection
flip.

### Requirements
- Android Studio (recent) with **JDK 21–23**, *or* JDK 25 (the project uses
  **Gradle 9.1 + AGP 8.13.2**, which run on JDK 25).
- Android SDK 36, an emulator or a connected device.
- [`bundletool`](https://github.com/google/bundletool) (`brew install bundletool`).

### Steps
```bash
./gradlew :app:bundleDebug
bundletool build-apks --local-testing \
  --bundle=app/build/outputs/bundle/debug/app-debug.aab \
  --output=/tmp/dfmtoy.apks --overwrite
bundletool install-apks --apks=/tmp/dfmtoy.apks
```

Then on the device:
1. First launch shows **Unknown** — no module installed.
2. Tap **Install countrycodekr** → the headline flips to **🇰🇷 Korea**.
3. Note that `Installed modules` changed — that single fact drove the detection.

To reset: `adb uninstall io.github.komodgn.dfmtoy`, then re-run `install-apks`.

> Don't use Android Studio's **Run ▶** for this — it pre-installs every feature
> module, so everything shows as installed from the start. Use bundletool
> `--local-testing` instead.

---

## Add a country (contributions welcome)

Adding a country is a small, self-contained change. To add, say, the UK (`GB`):

1. **Create the module** — copy `countrycodekr/` to `countrycodegb/` and update
   its `namespace` to `com.example.dfmtoy.countrycodegb` in `build.gradle.kts`.
2. **Register it** — add `include(":countrycodegb")` in `settings.gradle.kts`,
   and add `:countrycodegb` to `dynamicFeatures` in `app/build.gradle.kts`.
3. **Set the country** — in `countrycodegb/src/main/AndroidManifest.xml`, set
   `<dist:country dist:code="GB" />`, and add a `title_countrycodegb` string in
   `app/src/main/res/values/strings.xml`.
4. **Handle it in the UI** — add a `"countrycodegb" in installed → "🇬🇧 United Kingdom"`
   branch in `MainActivity.kt`.

Open a PR — new countries are very welcome!

---

## Build / release notes

- `applicationId` is `io.github.komodgn.dfmtoy`.
- Release signing reads from a gitignored `keystore.properties` (never committed).
- Bump `versionCode` for every Play upload.

---

## Further reading

- [당근 숏폼팀의 on-demand Dynamic Feature Module 도입기](https://medium.com/daangn/200mb-%EB%AA%A8%EB%93%88%EC%9D%84-%ED%8C%80-%EB%8B%A8%EC%9C%84%EB%A1%9C-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0-%EB%8B%B9%EA%B7%BC-%EC%88%8F%ED%8F%BC%ED%8C%80%EC%9D%98-on-demand-dynamic-feature-module-%EB%8F%84%EC%9E%85-adb6794f2a9b)
  — a real-world write-up on adopting DFM to solve a 200MB module problem (Korean).
