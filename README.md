# mis-2019-exercise-3b-opencv

Gabriel De Ioannes 120770 
Abhay Syal 120800

Adding the source build of Open CV to the project was easier than expected but with lots of unknown processes.
First was finding the right Open CV version, the last one 4.1.0 has some issues with the dependency manager Gradle
So decide to go with the 3.4.1 for the SDK, in the android studio the integration of the SDK is really straight forward
Add to edit some think in the Gradle build file so much with my project but after that importing the modules to 
the dependencies of the project were straight forward.
Try to use an example project from the web and the OpenCV manager was needed.
So building OpenCV source
Building from source open CV, there was diffuse information off how to do this with android studio,
Use a combination of all of them to work it out.
First, download CMake, and then realize that I could use the tools manager from Android studio and download
CMake, NDK, LLDB with CMake can build OpenCV in C++, NDK Native Development Kit to work with C project, LLDB android debug
specific for native code.
Download the source project and there is an option in the file to sink a C++ project into my project using Gradle and CMake
    externalNativeBuild {
        cmake {
            path file('../../opencv-3.4.1-source/CMakeLists.txt')
        }
    }
had some issue because I use the 4.1.0 version of OpenCV for the build, changed to 3.4.1 and after waiting like 40 min I
could run the project on my phone. After the first build is much faster, but still have to wait like 3 min for every test.
The final APK is 90mb heavy, pretty big for the functionalities fo my APP.

https://developer.android.com/studio/projects/add-native-code.html
https://www.youtube.com/watch?v=Vp20EdU5qjU
https://www.learnopencv.com/install-opencv-on-android-tiny-and-optimized/
https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c
