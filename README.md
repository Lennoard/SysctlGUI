<p align="center">
  <img width="108" height="108" src="https://i.imgur.com/TPKCbg6.png"/>
</p>

<p align="center">
  <img height="360" src="https://lh3.googleusercontent.com/vmi_rXs0nfqfAh3woDLDRxDW0tx_UM1nx8zCda7nY4FoO6ebmvSQJaHT-GENzEOp34o"/>  
  <img height="360" src="https://lh3.googleusercontent.com/DXaSIdkmBzUsQzPOkkIbY5YdKVsohcTb4IeSq9q70PPdG07yovHCD7X6XPebtesM0L0"/>  
  <img height="360" src="https://lh3.googleusercontent.com/zrIdQ1jGiaDb_kfnxAPqd8bJwejjJzvq7whU-kGXvT0G86l3RHzuseAnZZpP7r3RaV0"/>  
  <img height="360" src="https://lh3.googleusercontent.com/fpWBqB-qTRp1zfw6r7aBm6auQD7cdw-3vQbKsqwVVo5lcPHvQq96XKVdO1gRTydF8qU"/>  
</p>

# SysctlGUI

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d91bf38325aa4bb6b6cb67136f72f1f1)](https://www.codacy.com/gh/Lennoard/SysctlGUI/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Lennoard/SysctlGUI&amp;utm_campaign=Badge_Grade)
![](https://img.shields.io/github/languages/top/Lennoard/SysctlGUI)  
![](https://img.shields.io/github/contributors/Lennoard/SysctlGUI)  
![](https://img.shields.io/github/downloads/Lennoard/SysctlGUI/total)  
![](https://img.shields.io/github/v/release/Lennoard/SysctlGUI)  
![GitHub commits since latest release (by date)](https://img.shields.io/github/commits-since/Lennoard/SysctlGUI/latest/master)

A GUI application for Android <code>sysctl</code> to edit kernel variables

## Features
-  Browse filesystem for specific kernel parameters
-  Select parameters from a searchable list
-  Show documentation for known parameters
-  Load parameters from a configuration file
-  Reapply parameters at startup
-  Mark parameters as favorite for easy access

## Technologies

This project utilizes a modern Android development stack, leveraging a comprehensive suite of libraries and tools:

-   **Core & Architecture:**
    -   Architectural Patterns: MVI, reactive and maintainable.
    -   [Kotlin](https://kotlinlang.org/): For modern, concise, and safe programming.
    -   Android Jetpack:
        -   [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)
        -   [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
        -   [Navigation Component](https://developer.android.com/guide/navigation)
        -   [Room](https://developer.android.com/training/data-storage/room): For local data persistence.
        -   [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager): For deferrable, asynchronous tasks.
-   **UI Development:**
    -   [Jetpack Compose](https://developer.android.com/jetpack/compose): For building native UIs with a declarative approach.
        -   Compose Material 3 & Material Components
    -   [Jetpack Glance](https://developer.android.com/develop/ui/compose/glance): For creating App Widgets with Jetpack Compose.
-   **Asynchronous Programming:**
    -   [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flows](https://kotlinlang.org/docs/flow.html): For efficient and structured background tasks and reactive data streams.
-   **Utilities:**
    -   [Koin](https://insert-koin.io/): Dependency injection framework for Kotlin.
    -   [Ktor Client](https://ktor.io/docs/client-reference.html): For making HTTP requests (used for parameter documentation).
    -   [Libsu](https://github.com/topjohnwu/libsu): For interacting with root services.
    -   [Jsoup](https://jsoup.org/): For parsing HTML (used for parameter documentation).
    -   [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization): For JSON serialization/deserialization.

## Contributing

We welcome contributions to SysctlGUI!

### Translations
If you'd like to help translate the app into other languages, please see the [translation guide](TRANSLATING.md) for instructions on how to get started. Your contributions will help make SysctlGUI accessible to a wider audience.

## Download

<a href='https://apt.izzysoft.de/fdroid/index/apk/com.androidvip.sysctlgui'><img alt='Get it on IzzyOnDroid' height="64" src='https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png'/></a>  
<a href='https://play.google.com/store/apps/details?id=com.androidvip.sysctlgui&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' height="64" src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

## License

This project is licensed under the terms of the MIT license.

> Copyright (c) 2019-2025 Lennoard.
>
> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
