
NUI Chapter 8.6. Facial Features Recognition

From the website:

  Killer Game Programming in Java
  http://fivedots.coe.psu.ac.th/~ad/jg

  Dr. Andrew Davison
  Dept. of Computer Engineering
  Prince of Songkla University
  Hat yai, Songkhla 90112, Thailand
  E-mail: ad@fivedots.coe.psu.ac.th


If you use this code, please mention my name, and include a link
to the website.

Thanks,
  Andrew

============================

This directory contains 4 Java files:
  * FacialRecog.java, FFPanel.java
    FaceInfo.java, FeatureID.java


Image used for displaying a moustache on the user
  * mustache.png


Two batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of FaceSDK, JavaCV, and OpenCV


----------------------------
Before Compilation/Execution:

You need to have a webcam connected to your PC.

You need to download and install:

  * OpenCV:  I downloaded v2.4.5 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.5-bin.zip
             and installed it in d:\javacv-bin


  * get a free FaceSDK evaluation key string from
    http://www.luxand.com/facesdk/requestkey/
    This will be used to validate the FaceSDK library at install time *and*
    activate your FaceSDK code at runtime. The key is valid for 6 weeks.

  * FaceSDK:  I downloaded v4 from http://www.luxand.com/facesdk/download/
              and installed it in C:\Program Files\Luxand\FaceSDK
              (the installation can only be completed with the evaluation key)

       The facesdk.dll is loaded using System.loadLibrary()
       at the start of the FacialRecog class; that code should not 
       require any changing.


----------------------------
Compilation:

> compile *.java
    // you must have FaceSDK, JavaCV, and OpenCV installed

----------------------------
Execution:

> run FacialRecog
    // you must have FaceSDK, JavaCV, and OpenCV installed


----------------------------
Mood Detector and Mustache Augmenter

FFPanel.paintComponent() currently only displays the facial feature lines
and points. 

If you want to add the mood detector text and/or a moustache to the display
then modify FFPanel.paintComponent() by uncommenting the calls to
reportMood() and/or attachStache()

If you want to stop displaying the facial feature lines and points then
comment out the call to faceInfo.draw() in FFPanel.paintComponent()


----------------------------
Last updated: 18th July 2013
