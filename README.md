<p align="center">
  <img src="art/logo.png" alt="Elendheim Sample Grabber logo" width="200" />
</p>

<h1 align="center">Elendheim Sample Grabber</h1>

<p align="center">Catch found sounds before they get away.</p>

## What it is

Elendheim Sample Grabber is a one-button field recorder for Android. It is built for grabbing audio as material, not memos: rain on a window, your pets, a weird door creak. Tap the mic, it records five to ten seconds, trims the silence off both ends, and saves a WAV named by date, ready to drop straight into FL Studio.

## How it works

1. Tap the big mic button when you hear something good.
2. It records for up to ten seconds. Tap again to stop early (every grab is at least five seconds so you never come up short).
3. Dead air is trimmed off the start and end automatically.
4. The sample lands in `Music/Elendheim Samples` as a 44.1 kHz 16-bit mono WAV, named like `Grab 2026-07-05 14-32-08.wav`.
5. Play back or delete your grabs right in the app.

## Details

- Dark mode first. The app always runs in its night palette.
- No accounts, no network, no nonsense. Audio stays on your device.
- Minimum Android 10 (API 29).
- Samples are plain WAV files in your Music folder, visible to any DAW, file manager, or sync tool.

## Building

Open the project in Android Studio, or from the command line:

```
./gradlew assembleDebug
```

The APK ends up in `app/build/outputs/apk/debug/`.

## License

MIT. See [LICENSE](LICENSE).
