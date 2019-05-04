# mis-2019-exercise-3b-opencv

Gabriel De Ioannes 120770 
Abhay Syal 120800

BUILDING FROM SOURCE
Adding the source build of OpenCv to the project was easier than expected but with lots of unknown part of the processes.
First was finding the right OpenCv version, the last one 4.1.0 has some issues with the dependency manager Gradle
So decide to go with the 3.4.1 for the SDK, in the android studio the integration of the SDK is really straight forward
Had to edit some think in the Gradle build file so match with my project but after importing the modules to 
the dependencies of the project everything was straight forward.
Try to use an example project from the web and the OpenCV manager was needed in the phone.
Building OpenCV source
Building from source OpenCv, there was diffuse information off how to do this with android studio,
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
could run the project on my phone. After the first build is much faster, but still have to wait like 1 min for every test.
The final APK was 81mb heavy, pretty big for the functionalities fo my APP.

This were the link that I use to do this
https://developer.android.com/studio/projects/add-native-code.html
https://www.youtube.com/watch?v=Vp20EdU5qjU
https://www.learnopencv.com/install-opencv-on-android-tiny-and-optimized/
https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c

RED NOSE IN THE FACES
There was a lot of information about the different sources to achieve this in the code,
The main project was a color tracker that I use to have something running in OpenCv, it was a blob color tracker, then use the project in the PDF whit phyton as a guide
Try different XML with Cascade Classifiers, finally, use the front face one
It was really good and putting the nose, thanks to human face proportions was really straight forward
just use the rectangle generated in the Cascade Classifier function from open CV, draw a circle using OpenCv functionality
placed in the centerX of the rectangle and multiplied by 0.6 the height of the rectangle. for the size, I use the width 
of the rectangle as a reference and multiplied by 0.15, that's a 15% of the width of the face.
Doing that did the trick.