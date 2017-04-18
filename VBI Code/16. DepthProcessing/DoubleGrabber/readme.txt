
NUI Chapter 13. Depth Processing

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
  * SnapPics.java, DoublePicsPanel.java


A subdirectory:
  * pics/
      - the snapped pictures from the left and right cameras will be stored here;
      - image pairs will heve the same ID number as part of their name


Two batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of JavaCV and OpenCV


----------------------------
Before Compilation/Execution:

You need to have **two** webcams connected to your PC.

You need to download and install:

  * OpenCV:  I downloaded v2.4.3 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.3-bin.zip
             and installed it in C:\javacv-bin

----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> run ListDevices
    - this will list the cameras that OpenCV can 'see', including their camera IDs
      which you will need to add to the call to DoublePicsPanel in the SnapPics class

> run SnapPics
   - the two webcam images will be updated every 150 ms, but a snap will only be saved
     if the user presses the <enter>, <space>, or <numpad>-5 key
   - the images are saved to the pics/ subdirectory 


----------------------------
Last updated: 19th June 2013
