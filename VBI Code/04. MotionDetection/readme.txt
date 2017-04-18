
NUI Chapter 3. Motion Detection

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
  * MotionDetector.java, MotionPanel.java,
    JCVMotionDetector.java      


There is 1 image file, used by the application:
  * crosshairs.png


There are 3 subdirectories:
  * test-rig\    -- this holds the JavaCV test-rig for 
                    this image differencing motion detector;
                    see the readme.txt in that directory for details

  * MogCog Detection\    -- this holds the JavaCV test-rig for 
                            a background subtraction motion detector;
                            see the readme.txt in that directory for details

  * OpFlow\      -- this holds the JavaCV test-rig for 
                    an optical flow detector;
                    see the readme.txt in that directory for details


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
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> run MotionDetector
    // you must have JavaCV and OpenCV installed


----------------------------
Last updated: 13th September 2013
