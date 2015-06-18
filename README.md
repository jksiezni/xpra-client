# xpra-client
Xpra Client for Java/Android

# Getting source

The project depends on external libraries (bee-encode and rencode4j) which are linked here as submodules. So it is recommended to clone repository with '--recursive' option.

```shell
	git clone --recursive https://github.com/jksiezni/xpra-client.git
```

# Building project

Requirements:

* Java 1.7
* Android SDK
* Gradle (optional)

## Compiling
1. Setup path to Android SDK with environment variable

	>	ANDROID_HOME=/path/to/android/sdk

	or create a *local.properties* file and write:

	>	sdk.dir=/path/to/android/sdk

2. Start building

	>	$ ./gradlew assembleDebug

3. Install app on the Android device

	>	$ ./gradlew installDebug

