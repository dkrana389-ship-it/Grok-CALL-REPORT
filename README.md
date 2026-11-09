# Call Report – After Call Screen (Android)

A clean after-call report screen similar to Truecaller, built **without** `SYSTEM_ALERT_WINDOW` / overlay permission.

## How it works

1. App requests **ROLE_CALL_SCREENING** (Caller ID & spam app).
2. When a call ends, the system launches our `CallReportActivity` via `android.telecom.action.POST_CALL`.
3. The activity shows:
   - Caller name & number
   - Today’s total duration + call count
   - Last call type, duration & date
   - 5-second visual timer
   - Banner ad placeholder
   - Close + Callback buttons

## Requirements

- Android 10+ (API 29+) for Call Screening role
- Target: Android 16 (API 36)
- Kotlin + ViewBinding

## Setup & Test

1. Open the project in **Android Studio** (Meerkat or newer recommended).
2. Sync Gradle.
3. Run on a real device (emulator call simulation is limited).
4. On first launch:
   - Tap **Grant Permissions**
   - Tap **Set as Caller ID & spam app** → choose this app
5. Make a test call (incoming or outgoing) and hang up.
6. Call Report screen should appear automatically.

## Important Notes

- Only **one** app can hold the Call Screening role at a time.
- If another app (Truecaller, Google Phone, etc.) is selected, this app will not receive the post-call intent.
- Exact call duration comes from `READ_CALL_LOG`.
- Banner is currently a placeholder. Integrate AdMob / AppLovin later.

## Play Store readiness

- Use proper app icons (replace placeholder mipmaps).
- Add Privacy Policy.
- Declare `READ_CALL_LOG` use-case in Play Console (Caller ID / call history).
- Keep the primary purpose as “Call Report / call summary”. Ads should remain secondary.
- Sign the release AAB with your upload key.

## Project Structure

```
app/
 ├── src/main/
 │   ├── java/com/callreport/app/
 │   │   ├── MainActivity.kt
 │   │   ├── CallReportActivity.kt
 │   │   ├── CallScreeningService.kt
 │   │   └── CallStatsHelper.kt
 │   ├── res/layout/
 │   │   ├── activity_main.xml
 │   │   └── activity_call_report.xml
 │   └── AndroidManifest.xml
 └── build.gradle.kts
```

## License

For educational / personal use. Adapt as needed.
