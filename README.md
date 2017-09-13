# xpra-client
Xpra Client for Java/Android

# Building project

Requirements:

* Java 1.8
* Android SDK

## Compiling
1. Setup path to Android SDK with environment variable

	>	ANDROID_HOME=/path/to/android/sdk

	or create a *local.properties* file and write:

	>	sdk.dir=/path/to/android/sdk

2. Start building

	>	$ ./gradlew build

3. Install app on the Android device

	>	$ ./gradlew installDebug

