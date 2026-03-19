# CONTRIBUTING (Android) – Musly

Diese Anleitung beschreibt **ausschließlich** den Beitrag zur **Android-Version** von Musly (Flutter + native Android-Integration).

## 1) Zweck dieser Anleitung

Wenn du an Android in diesem Projekt mitarbeiten möchtest, findest du hier die relevanten Ordner, Voraussetzungen, Build-/Run-Befehle, typische Änderungspfade und den empfohlenen Contribution-Workflow.

## 2) Projektüberblick (Android-Fokus)

Musly ist ein Flutter-Musikplayer mit zusätzlicher nativer Android-Integration (u. a. Android Auto, System-Integration, Bluetooth AVRCP, Samsung-spezifische Features).

Für Android-Contributions sind vor allem diese Bereiche relevant:

- `lib/` – Flutter-UI, Logik, Provider, Services
- `android/` – Android-App-Modul, Manifest, Ressourcen, Gradle-Konfiguration, Kotlin-Code

Wichtige Android-Dateien/Orte:

- `android/app/build.gradle.kts`
- `android/build.gradle.kts`
- `android/settings.gradle.kts`
- `android/gradle/wrapper/gradle-wrapper.properties`
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/kotlin/com/musly/musly/*.kt` (Kotlin-Quellen)
- `android/app/src/main/res/xml/network_security_config.xml`
- `android/app/src/main/res/xml/automotive_app_desc.xml`

## 3) Voraussetzungen für Android-Entwicklung

Aus dem Repository ableitbare Mindestanforderungen:

- **Flutter SDK**: laut `README.md` mindestens **3.10.0**
- **Dart SDK**: laut `pubspec.yaml` **`^3.10.0`**
- **Java**: **17** (siehe `android/app/build.gradle.kts` `sourceCompatibility/targetCompatibility` und Workflow `release.yml` mit `java-version: '17'`)
- **Android SDK / Android Studio**: erforderlich für Emulator, Platform-Tools und Android-Builds
- **Gerät zum Testen**: Android-Emulator oder physisches Android-Gerät mit aktivem USB-Debugging

## 4) Setup der Entwicklungsumgebung (Android)

Im Projekt-Root:

```bash
flutter pub get
flutter doctor
```

Hinweise:

- Das Android-Verzeichnis enthält in diesem Repository **keinen eingecheckten `gradlew`-Wrapper** (`android/.gitignore` ignoriert `gradlew`/`gradlew.bat`). Nutze daher primär die `flutter`-CLI für Build/Run.
- Stelle sicher, dass `flutter doctor` für Android keine kritischen Fehler meldet (SDK, Lizenzen, Device).

## 5) Android-App lokal starten

Im Projekt-Root:

```bash
flutter run
```

Optional:

- Gerät explizit wählen:

```bash
flutter devices
flutter run -d <deviceId>
```

- Android Studio kann für Emulator-Management, Logcat und Debugging zusätzlich genutzt werden.

## 6) Android Build (APK / AAB)

Im Projekt-Root:

```bash
flutter build apk
flutter build appbundle
```

Repository-spezifische Hinweise:

- In CI wird Android als Release-APK mit ABI-Splitting gebaut:

```bash
flutter build apk --release --split-per-abi
```

(siehe `.github/workflows/release.yml`)

- `android/app/build.gradle.kts` nutzt für `release` aktuell `signingConfig = signingConfigs.getByName("debug")`. Für produktive Signierung musst du lokal/CI eine eigene Release-Signing-Konfiguration setzen.

## 7) Wo Android-spezifische Änderungen stattfinden

### Flutter-Seite

- `lib/` (z. B. Player- und Library-Provider, UI, Services)
- Android-relevante Flutter-Services:
  - `lib/services/android_auto_service.dart`
  - `lib/services/android_system_service.dart`
  - `lib/services/bluetooth_avrcp_service.dart`
  - `lib/services/samsung_integration_service.dart`

### Native Android-Seite

- **Manifest & Berechtigungen**: `android/app/src/main/AndroidManifest.xml`
  - Netzwerk, Foreground Service, Bluetooth, Medien/Storage, Notifications etc.
- **Gradle/Build**:
  - `android/app/build.gradle.kts`
  - `android/settings.gradle.kts`
  - `android/build.gradle.kts`
- **Kotlin (Platform Channels + Services)**:
  - `MainActivity.kt` (Plugin-Registrierung)
  - `AndroidAutoPlugin.kt`
  - `AndroidSystemPlugin.kt`
  - `BluetoothAvrcpPlugin.kt`
  - `SamsungIntegrationPlugin.kt`
  - `MusicService.kt` (MediaBrowserService für Android Auto)
  - `BluetoothMediaHelper.kt`
  - `SamsungHelper.kt`
- **Android Auto**:
  - Manifest-Metadaten + `res/xml/automotive_app_desc.xml`

## 8) Typische Contribution-Szenarien (Android)

- **UI/Feature in der App** → primär `lib/` (Screens, Widgets, Provider, Services)
- **Neue Android-Berechtigung / Permission-Verhalten** → `AndroidManifest.xml` (+ ggf. Flutter-Seite anpassen)
- **Native Android-Funktion** (MethodChannel/EventChannel) → Kotlin-Dateien in `android/app/src/main/kotlin/...` und passender Service in `lib/services/...`
- **Build-/Performance-Anpassungen** → Gradle-Dateien unter `android/`
- **Android Auto-Verhalten** → `MusicService.kt`, `AndroidAutoPlugin.kt`, zugehörige Dart-Services/Provider

## 9) Contribution-Workflow

```bash
git checkout -b feature/<kurzbeschreibung>
```

Empfohlene Schritte:

1. Android-bezogene Änderungen umsetzen (Flutter + ggf. native Android-Schicht)
2. Auf Android-Gerät/Emulator lokal testen
3. Statische Analyse/Tests ausführen (siehe Abschnitt 10)
4. Committen (kleine, nachvollziehbare Commits)
5. Branch pushen und Pull Request erstellen

## 10) Tests, Linting, Analyse

Im Repository sind Flutter-Standardchecks vorhanden:

```bash
flutter analyze
flutter test
```

Für Android-Änderungen zusätzlich sinnvoll:

- Laufzeittest mit `flutter run` auf mindestens einem Android-Gerät/Emulator
- Bei Android-Auto-/Bluetooth-/Samsung-Features gezielt auf realen Geräten testen, falls verfügbar

## 11) Coding-Konventionen

Aus dem Repository erkennbar:

- Lint-Basis: `flutter_lints` (siehe `analysis_options.yaml`)
- Formatierung/Style über Flutter/Dart-Standards (`dart format`)
- Android-Buildskripte sind in Kotlin DSL (`*.gradle.kts`)
- Bestehende Channel-Namen und Package-Präfixe (`com.devid.musly/...`) konsistent weiterverwenden

## 12) Lizenzhinweise

Das Projekt steht unter **CC BY-NC-SA 4.0** (siehe `LICENSE`).

Zusätzlich steht in `README.md` explizit:

- **Keine Redistribution in den Google Play Store oder andere kommerzielle Stores**.

Beachte diese Einschränkung bei Distribution und PR-Diskussionen zu Release-Themen.

## 13) Projektspezifische Android-Hinweise

- Android verwendet `compileSdk`, `minSdk`, `targetSdk` aus Flutter-Defaults im Gradle-Skript.
- Java/Kotlin-Ziel ist JDK 17.
- Netzwerk-Konfiguration erlaubt in `network_security_config.xml` Cleartext-Traffic (für bestimmte Entwicklungs-/Server-Szenarien).
- AndroidManifest enthält umfangreiche Android-spezifische Integrationen (Foreground-Media-Service, Android Auto, Cast, Bluetooth, Samsung).
- CI-Workflow `release.yml` enthält den Referenz-Buildpfad für Android-Artefakte (`apk --split-per-abi`).

---

Wenn du an Android beiträgst, halte Änderungen möglichst klein, teste sie auf einem realen Android-Setup und dokumentiere Android-spezifische Auswirkungen klar im Pull Request.
