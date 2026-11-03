# L CARROM — Android starter project

## Included
- Android app in Kotlin + Jetpack Compose
- Carrom-style board, coins, striker, power control, basic motion/collision and corner pockets
- Local two-player score display and reset

## Open and run
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Extract this ZIP.
3. Android Studio → Open → choose the `L_CARROM_Android` folder.
4. Wait for Gradle sync. If prompted, install the requested Android SDK/build tools.
5. Connect an Android phone with USB debugging enabled, or create an emulator.
6. Press Run ▶.
7. To make an APK: Build → Build Bundle(s) / APK(s) → Build APK(s).
   The debug APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.

## Important scope notes
This is a playable starter, not a polished commercial carrom simulation. It uses simplified physics and is intended as a base to improve. The current `VS COMPUTER` button is a mode selector only; computer AI is not implemented yet. The score logic is a basic placeholder, not official tournament rules.

Online multiplayer is NOT active in this ZIP. Real online play needs a backend (for example Firebase Realtime Database/Firestore), player sign-in, room matchmaking, authoritative turn/state handling, and anti-cheat validation. Do not put Firebase service secrets in source control. Add Firebase configuration from your own Firebase project before enabling online play.

## Suggested next development steps
- Add proper strike/aim gesture and collision physics
- Implement official queen/cover/foul rules and turn retention
- Implement computer opponent
- Add Firebase room creation/joining and synchronized shots
- Add sound, settings, profile, and app icon
