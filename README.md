## mDNS Service Browser for Oculus Quest

This is an example to scan Aroma Shooter via Wifi by browsing its mDNS service. This demo is for running on Oculus Quest.  Basically, this can be used to browse any mDNS service in local networks.

### Prerequisites:

- Android API version >= 16

### Structure of the repository:

- An Android library project ( to build .AAR) which implements browsing service.

- A C# script to utilize the function provided in the library for browsing services.

  ※Both of them are just samples, please understand to make it work with your own requirements.

### References:

- [Unity's document about AAR Plugins and Android Libraries](https://docs.unity3d.com/Manual/AndroidAARPlugins.html)
- [How to call Android native class constructors from Unity script](https://answers.unity.com/questions/815804/call-native-android-contructor.html)
- [How to get application context of Unity apps for Android](https://stackoverflow.com/questions/33496438/get-applicationcontext-from-unity-3d)
- [How to call a function with return values from Android library in Unity](https://stackoverflow.com/questions/25450733/calling-a-unity-c-sharp-method-that-returns-a-value-from-android-java)
- [How to create an Android AAR library](https://dominoc925.blogspot.com/2015/09/how-to-create-and-use-android-archive.html)