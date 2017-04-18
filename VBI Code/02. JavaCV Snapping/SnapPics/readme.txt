
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

This directory contains 3 Java files:
  * ListDevices.java
  * SnapPics.java, PicsPanel.java


A subdirectory:
  * pics/
      - the snapped pictures from the camera will be stored here;


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
             and installed it in C:\javacv-bin

----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> run ListDevices
    - this will list the cameras that JavaCV can 'see', including their camera IDs
      which you will need to add to PicsPanel

> run SnapPics
   - the webcam image will be updated every 100 ms, but a snap will only be saved
     if the user presses the <enter>, <space>, or <numpad>-5 key
   - images are saved to the pics/ subdirectory 


----------------------------
Last updated: 27th June 2013
