
NUI Chapter 5. Blobs Drumming

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
  * BlobsDrumming.java, BDPanel.java,
    DrumsManager.java, Drum.java, PercussionPlayer.java,
    ColorRectDetector.java


There are two HSV configuration files, used by the application:
  * redHSV.txt, blueHSV.txt


There are 2 batch files:
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
Physical Materials:

You need two coloured cards, whose HSV values match those in 
redHSV.txt and blueHSV.txt. The contents of these configuration files
can be created with the help of the HSVSelector application.

----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> run BlobsDrumming
    // you must have JavaCV and OpenCV installed

----------------------------
Last updated: 6th July 2013
