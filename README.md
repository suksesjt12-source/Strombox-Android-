# StormBox Android — MVP

StormBox is a small Android launcher shell for a separately installed Windows compatibility runtime.

## Important limitation

This project is **not a Windows emulator** and does not bundle Wine, Box64, DXVK, Vulkan drivers, or any game. It cannot run a Windows EXE by itself.

The launcher expects a companion runtime app to implement this integration contract:

- Android package: `com.stormbox.runtime`
- Activity action: `com.stormbox.runtime.RUN`
- Extras:
  - `executable_uri`: String content URI selected with Android's document picker
  - `game_title`: String
  - `profile`: String (`helio_g100_960x540_30fps`)

This is a new StormBox integration contract, **not a standard Winlator/GameNative API**. Stock Winlator will not automatically understand it. A companion runtime adapter must be developed or an existing runtime must be modified to implement it.

## Build from an Android phone with GitHub Actions

1. Create a new empty GitHub repository.
2. Upload the contents of this folder to the repository root (not the ZIP itself).
3. Open the repository's **Actions** tab.
4. Enable GitHub Actions if prompted.
5. Run **Build StormBox APK** using **Run workflow**, or push to `main`.
6. Open the completed workflow run.
7. Under **Artifacts**, download `StormBox-debug-apk`.
8. Extract the ZIP artifact and install `app-debug.apk`.

## Build requirements

- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Android SDK platform 35 (provided by the GitHub-hosted build environment only if installed/configured; if the runner reports a missing SDK platform, add an Android SDK setup step or use a runner image with API 35 installed).

## Current MVP behavior

- Landscape launcher screen
- Select and remember a game document URI
- Probe for the companion runtime package/action
- Send the selected URI and game profile to the companion runtime

## Next engineering tasks

1. Implement a runtime companion/adapter that receives the StormBox intent.
2. Convert the selected content URI into a file path accessible to Wine's prefix, or copy it into app-private storage.
3. Implement runtime installation/version checks and safe process lifecycle handling.
4. Add per-game configuration UI.
5. Test graphics translation and Vulkan support on the target Mali GPU.

Use only game files and runtime components you are authorized to use.
