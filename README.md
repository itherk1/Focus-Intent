# Focus Intent

Focus Intent is a mindful app launching interceptor that helps you break digital habits.
Instead of directly opening distractive apps (like social media), Focus Intent intercepts the launch and asks "Are you sure you want to open this app?" in a calming, breathing window. 

## Features
- **App Intercepting**: Blocks selected apps from opening immediately.
- **Breathing Window**: Take a moment to breathe before deciding to continue.
- **Widgets**:
  - Focus Stats widget to see intercepted apps.
  - Blocked Apps widget for a quick look at your restricted apps.
- **Detailed Stats**: Keep track of intercepts, prevented launches, and more.
- **Screen Time**: Track and analyze your digital wellbeing insights.
- **Responsive Layout**: Designed for phones, foldables, and tablets.

## Version 1.1.0
- Added continuous app usage tracking to send a gentle notification after 15 minutes of uninterrupted use.
- Reintroduced the breathing intercept window automatically after 30 and 60 minutes of continuous usage to prompt a break.
- Added developer watermark "Created with ❤️ by Rishabh Kankane" for personal app identity at the bottom of the navigation screens.
- Updated app to prompt for post notification permissions on Android 13+.

## Version 1.0.0
- Refined app interception logic seamlessly across device configurations.
- Fixed widget updates. 
- Fixed the logic for recent apps behavior to not continuously intercept for canceled launches.

## Permissions Needed
- **Accessibility Service**: Required to detect which apps are launched to intercept them.
- **Usage Access**: Required to calculate screen time statistics.

## Building the project
Tested with Android Gradle Plugin and Jetpack Compose.
Open in Android Studio or build via the command line:

```
./gradlew assembleDebug
```
