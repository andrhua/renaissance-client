# Ren*ai*ssance

Replica of Google's [Quick, Draw!](https://quickdraw.withgoogle.com) as an autonomous Android app.

// insert demo

## Trying out
**Important**: project is licensed with Google Cloud Platform Free Tier, meaning it won't be working past mid-December 2019, but you can relatively easy deploy your own prediction server as described [here](https://github.com/andrhua/renaissance-keras).

### Android
Make sure your device is running Android 4.4+ and has OpenGL 3.0+ support.

Download [.apk](../master/android-debug.apk).

### Desktop
Install [JRE 1.8](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html), download [.jar](../master/desktop-1.0.jar).
```
java -jar desktop-1.0.jar
```

## Built with
- A custom drawing recognition [model](https://github.com/andrhua/renaissance-keras) written in Tensorflow and deployed to Google Cloud AI Platform for requesting online predictions.

- Client app is developed with the help of LibGDX framework, and technically is able to run anywhere with JRE 1.8 installed.

## Further improvements
1. Rewrite client in Flutter to support iOS
2. Train a much more [sophisticated](https://www.tensorflow.org/tutorials/sequences/recurrent_quickdraw) recognition model, using stroke direction

## Authors 
hey, that's me

## Acknowledgment
Sanyochek feat. forex broker
