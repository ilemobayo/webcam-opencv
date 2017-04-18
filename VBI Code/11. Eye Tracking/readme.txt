
NUI Chapter 8.5. Eye Tracking

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

This directory contains 6 Java files:
  * EyeTracker.java, EyePanel.java,
    AverageRect.java, ColorRectDetector.java,
    TargetMover.java, MoverPanel.java


Image used for displaying a crosshairs target:
  * crosshairs.png


One HSV configuration file:
  * blackHSV.txt
    This is for detecting the near-black of a pupil.
    If you want to change it, use the HSVSelector application, which 
    is described in NUI chapter 5 (http://fivedots.coe.psu.ac.th/~ad/jg/nui05/).


One Haar classifier (for a left eye):
  * eye.xml
    This file was originally called haarcascade_frontalface_alt2.xml
    and comes from the OpenCV download (in C:\opencv\data\haarcascades\) 
    or from http://alereimondo.no-ip.org/OpenCV/34


Two batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of JavaCV and OpenCV


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


----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> run EyeTracker
    // you must have JavaCV and OpenCV installed

----------------------------
Last updated: 17th July 2013
