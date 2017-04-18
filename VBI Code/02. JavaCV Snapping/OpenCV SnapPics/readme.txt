
NUI Chapter 2b Webcam Snaps Using JavaCV

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

This directory contains 2 Java files:
  * SnapPics.java, PicsPanel.java


A subdirectory:
  * pics/
      - the snapped pictures from the camera will be stored here;


Two batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of OpenCV


----------------------------
Before Compilation/Execution:

You need to have a webcam connected to your PC.

You need to download and install:

  * OpenCV:  I downloaded v2.4.5 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * NOTE: JavaCV is not required

----------------------------
Compilation:

> compile *.java
    // you must OpenCV installed
    // NOTE: JavaCV is not required

----------------------------
Execution:

> run SnapPics
   - the webcam image will be updated every 100 ms, but a snap will only be saved
     if the user presses the <enter>, <space>, or <numpad>-5 key
   - images are saved to the pics/ subdirectory 


----------------------------
Last updated: 27th June 2013
