# Google Play Store Assets for Focus Intent

Here are all the necessary materials you need to submit **Focus Intent** to the Google Play Store. You can simply copy and paste these into your Play Console.

## 1. Store Listing Text

**App Name (Max 30 chars):** 
`Focus Intent - Scroll Break` 
*(Alternative ideas: "Scroll Interrupter", "Doomscroll Blocker")*

**Package Name:**
`com.aistudio.focusintent.xyzqpz`

**Short Description (Max 80 chars):**
`Interrupt bed-rotting & unconscious doomscrolling with mindful interventions.`

**Full Description (Max 4000 chars):**
```text
Reclaim your focus, stop bed-rotting, and break unconscious doomscrolling habits with Focus Intent!

Focus Intent is not just a standard app blocker—it's a scroll interrupter designed to catch you before you fall down the rabbit hole. Instead of just locking you out entirely, it introduces intentional friction and breathing exercises, helping you become aware of your subconscious habits and breaking the cycle of non-stop scrolling.

🚀 Key Features:
• Smart App Blocking: Select the apps that distract you the most (social media, games, news) and block them when you need to focus.
• Intent Tracking: Understand *why* and *when* you are distracted. The app tracks the number of times you've attempted to open blocked apps.
• Beautiful Weekly Widgets: Track your progress directly from your home screen with highly customizable Material You widgets.
• Insightful Analytics: View daily and weekly statistics, discover your peak distraction hours, and track your streaks of avoiding doomscrolling.
• Privacy-First: All of your usage data is stored locally on your device. We do not track you, and your browsing habits remain completely private.

🔒 Accessibility Service API:
Focus Intent utilizes the Android AccessibilityService API to detect when you open a designated blocked app. When you launch an app on your blocklist, our Accessibility Service quickly intervenes to redirect you to an interception screen, preventing mindless scrolling.
• We DO NOT use this service to collect, store, or share any personal data, text typed, or sensitive information.
• It is strictly used for the single purpose of detecting the launch of your chosen distracting apps to help you stay focused.

Take control of your time and build healthier digital habits today with Focus Intent!
```

---

## 2. App Content & Policy Declarations

When you navigate to the **App Content** dashboard in the Google Play Console, you will have to fill out specific forms. Use these answers to pass the review:

### Accessibility Service Declaration
* Google requires you to explain exactly how you use the Accessibility Service.
* **Question:** Does your app use the AccessibilityService API?
  * **Answer:** Yes
* **Question:** What is the core functionality that requires this service?
  * **Answer:** App Blocker / Device Usage Control
* **Question:** Provide a prominent disclosure and justification for the review team:
  * *(Copy this text exactly)*: `"Focus Intent utilizes the AccessibilityService API to detect when the user launches a globally blocked app (selected by the user) in order to immediately intervene and show a focus reminder screen. We do not use the AccessibilityService to collect, store, or transmit any user data, screen content, or personal information. It is strictly used locally to prevent access to distracting apps and reduce screen time, fulfilling the direct core utility of the app."`

---

## 3. Privacy Policy

Google Play strictly requires a live URL pointing to a Privacy Policy for apps using the Accessibility Service.
> **How to host:** You can copy the markdown below and host it for free on **GitHub Pages**, **Google Sites**, or **Notion** (and share it to the web). Then, paste the public link into the Play Console.

```markdown
### Privacy Policy for Focus Intent

**Effective Date:** [Enter Today's Date]

**1. Overview**
Focus Intent ("we", "our", "us") is committed to protecting your privacy. This Privacy Policy explains how our Android application handles your data. Focus Intent is built as a privacy-first application, meaning your data remains locally on your device.

**2. Data Collection and Usage**
Focus Intent is an offline-first application. We do not collect, transmit, upload, or store any of your personal data on external servers. 
- **App Usage Data:** Information about your blocked apps, intent sessions, and interception statistics are stored locally on your device's internal App Database. We, the developers, do not have any access to this data.

**3. Accessibility Service API**
Focus Intent uses the Android Accessibility Service API as a required core feature to function properly, specifically to detect when you launch an app you have chosen to block.
- **Why we need it:** The Accessibility Service intercepts the launch of your distracting apps so that it can display the focus intervention screen.
- **Data Privacy:** We **do not** use the Accessibility Service to monitor your general device usage, read your screen content, capture keystrokes, or collect any personal information. The service operates entirely locally and only monitors the specific package names of the apps you explicitly add to your blocklist. No data collected by this service is ever transmitted off your device.

**4. Third-Party Services**
Focus Intent does not use external tracking or analytics services that collect personal tracking data. 

**5. Changes to This Privacy Policy**
We may update this policy occasionally as new features are introduced. Any changes will be reflected within the app and on the app's Play Store listing.

**6. Contact Us**
If you have any questions or concerns about this Privacy Policy, please contact us at [rishi.kankane.257@gmail.com].
```

---

## 4. Visual Assets (Icons and Graphics)

I have also generated AI image assets in your project root to help your listing stand out!
- **`play_store_icon.png`**: A minimalist, high-resolution app icon you can use as the 512x512 store listing icon.
- **`feature_graphic.png`**: A promotional landscape banner (1024x500 dimension equivalent) for the "Feature Graphic" section of the store.

> **Note:** Ensure you take a few 9:16 screenshots of the application on your own device or emulator, including the profile chart and the blocked apps screen, to upload as your phone screenshots.
