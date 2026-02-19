# Orienteering Game - Android Client

A location-based scavenger hunt game for Android. Navigate through real-world locations, solve clues, and compete on leaderboards.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with ViewModel + StateFlow
- **Navigation**: Compose Navigation
- **Location**: Google Play Services (FusedLocationProviderClient)

## Requirements

- Android API 26+ (Android 8.0 Oreo)
- Android Studio Hedgehog or later
- Device with Google Play Services (for location features)

## Getting Started

1. Open Android Studio
2. Select "Open" and navigate to the `android/` folder
3. Wait for Gradle sync to complete
4. Run on an emulator or physical device

```bash
./gradlew assembleDebug
```

## Architecture Overview

The app follows **MVVM (Model-View-ViewModel)** architecture:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   UI Screens    │ ◄── │  GameViewModel  │ ◄── │  HuntRepository │
│  (Composables)  │     │   (StateFlow)   │     │   (Data Layer)  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌─────────────────┐
                        │ LocationService │
                        │     (GPS)       │
                        └─────────────────┘
```

- **GameViewModel**: Manages UI state via StateFlow, coordinates hunts and location updates
- **HuntRepository**: Provides hunt data (currently mock, API integration coming)
- **LocationService**: GPS tracking via FusedLocationProviderClient

## Project Structure

```
app/src/main/java/com/orienteering/hunt/
├── data/
│   ├── models/           # Data classes (Hunt, Clue, Player, Progress)
│   └── repository/       # HuntRepository (data layer)
├── services/
│   └── LocationService.kt  # GPS and location utilities
├── viewmodel/
│   └── GameViewModel.kt    # UI state management
├── navigation/
│   └── Navigation.kt       # Compose Navigation graph
├── ui/
│   ├── screens/          # Screen composables
│   │   ├── OnboardingScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── HuntSelectionScreen.kt
│   │   ├── HuntStartScreen.kt
│   │   ├── ActiveHuntScreen.kt
│   │   ├── HuntMapScreen.kt
│   │   ├── CheckInScreen.kt
│   │   └── LeaderboardScreen.kt
│   ├── components/       # Reusable UI components
│   └── theme/            # Material 3 theme
├── MainActivity.kt       # Entry point
└── HuntApplication.kt    # Application bootstrap
```

## Features

- **Hunt Selection**: Browse available scavenger hunts
- **GPS Tracking**: Real-time location updates and distance to target
- **Check-ins**: Verify arrival at hunt locations within radius
- **Progress Tracking**: Track completion status across hunts
- **Leaderboards**: View rankings for each hunt
- **Onboarding**: Player profile creation

## Permissions

The app requires the following permissions:

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | GPS tracking for hunt navigation |
| `ACCESS_COARSE_LOCATION` | Approximate location fallback |
| `ACCESS_BACKGROUND_LOCATION` | Location updates when app is backgrounded |
| `INTERNET` | API communication (future) |
| `ACCESS_NETWORK_STATE` | Network availability checks |

## Screenshots

*Screenshots coming soon*

## Future Improvements

- [ ] Real backend API integration
- [ ] User authentication (login/register)
- [ ] Real-time leaderboard updates
- [ ] Offline map support
- [ ] Push notifications for hunt updates
- [ ] Social features (teams, challenges)
