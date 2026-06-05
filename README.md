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

## Version 1.4.1
- **List Optimization**: Implemented memory-efficient item rendering for History and Insights screens using Lazy items and memoization, dramatically reducing scroll stutter and transition judder.
- **Dynamic Insights**: Refactored Improvement Points to support an infinitely scaling list instead of a fixed cap.
- **Preview Widgets**: Added native Material 3 previews for Weekly Focus and Blocked Apps widgets in the widget selector screen.
- **Battery Optimization**: Deferred unneeded logic and optimized pie chart visualization drawing limits.

## Version 1.4.0
- **Refined Chart Aesthetics**: Updated the stacked bar charts on the Profile screen to use precise rounded paths, ensuring beautiful symmetry and clipping regardless of how many apps stack up.
- **Material 3 Widget Upgrade**: Overhauled the Weekly Focus widget to fully adopt Material 3 theming (GlanceTheme), delivering gorgeous corner radii and matching native system UI perfectly.
- **Deep Insights**: Added advanced analytics to the Insights screen, automatically detecting and Warning you about "Doomscrolling Streaks" and "Peak Temptation Hours" when you have enough data.
- **Silky Smooth Visuals**: Improved Compose drawing loops under the hood to ensure animations and scroll performance are snappier across all screens.

## Version 1.3.2
- **Optimized Performance**: Re-engineered historical stats generation to run cleanly in background threads, eliminating UI stutters across App Selection, History, and Profile screens.
- **Accurate Subreddit Handling**: Refined accessibility service tracking logic so it no longer interrupts your session repeatedly if you navigate between secondary activities (like sub-reddits or embedded webviews) within the same allowed application.
- **Multi-color Chart Details**: The Profile screen's Weekly Focus chart now perfectly stacks data using precise Material theme color matching per individual app, ensuring full visualization of what actually distracted you each day.
- **Improved Adaptive Widgets**: Added a highly requested `4x3` Weekly Focus stacked-bar widget and resolved sizing bugs on Blocked Apps widgets to utilize dynamic Glance layout space calculation instead of hardcoded item limits.
- **Synchronous Breathing Sync**: Restructured Jetpack Compose `BreathingExerciseScreen` logic for perfectly locked scaling interpolation mappings and correct phase text, preventing stutter and incorrect phase labels.

## Version 1.3.1
- **Tablet & Foldable Optimization**: Revamped History, Apps, and Profile screens to use responsive `LazyVerticalGrid` layouts, utilizing the full width of large screens.
- **Scroll Fix for Profile**: Added scroll support and grid wrapping to the Profile screen to ensure diagrams are no longer clipped or excessively large on portrait mode.
- **Dynamic "Time Saved"**: Replaced the static 5-minute estimate with dynamic, app-specific calculation. The app now queries your actual average session length per application (via UsageStats) to accurately calculate the real time saved by blocking an app contextually.

## Version 1.3.0
- **Enhanced Weekly Chart**: Added X-axis day labels (Today, 1d, 2d, etc.) and a clear Y-axis metric scale to make spikes easily readable.
- **Dynamic Color Palette**: Overhauled the Today's Breakdown pie chart to generate an unlimited spectrum of theme-aligned colors based on the number of blocked apps (fixing the 4-color limit).
- **Smarter App Tracking (Instagram bug fix)**: Improved the Accessibility Service loop so returning to an actively used blocked app (or momentarily triggering an in-app keyboard/share sheet) no longer resets the breathing window timer.
- **Personalized Insights**: Upgraded the Insights engine to generate dynamic, personality-driven tips based on specific app names, success rates, and behavior thresholds (e.g. "Zen Master" vs "Minor Leaks").
- **UI & Performance Optimizations**: Added smooth `animateContentSize` transitions to dashboard cards and ensured the Room database preserves historical data seamlessly across updates without destructive migrations.

## Version 1.2.2
- Made the Weekly Focus Chart on the Profile screen clickable, allowing direct navigation into Detailed Insights for the week.

## Version 1.2.1
- Fixed continuous tracking window triggers (now accurately tracks 30, 60, 90 minute milestones).
- Sorted blocked apps list to pin selected targets at the top with spring animations.
- Prevented double-loading and reloading of active navigation tabs.
- Added full day-by-day historic navigation inside Detailed Insights.
- Fixed home screen widgets not populating properly on initial addition.
- Linked Today's Focus stats widget directly to Detailed Insights.

## Version 1.2.0
- Added a Detailed Insights screen showing app-by-app breakdown of attempts, avoided launches, and continuations, along with personalized improvement tips.
- Improved "Today" stats calculation to reliably refresh at 12 AM.
- Refined continuous tracking to prevent unexpected breathing windows while using in-app share menus (like Instagram Reels).
- Added an interactive help dialog guiding users through Android 13+ "Restricted Settings" to simplify enabling the Accessibility Service.
- Updated Navigation bar icons and UI styling for consistency.

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
