
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

This directory contains 1 Java file:
  * FeaturesDetector.java


A subdirectory:
  * faces/
      - this contains 17 example face pictures, and is where
        FeaturesDetector looks for a specified image file


Three batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of JMF, JavaCV, and OpenCV
  * runAll.bat
     - this calls run.bat 17 times, to test all the files currently
       stored in faces/


----------------------------
Before Compilation/Execution:

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

> run FeaturesDetector <fnm in faces/ subdirectory>

e.g.
> run FeaturesDetector at.jpg
    // you must have JavaCV and OpenCV installed
    /* the code assumes that the Haar cascades are located in 
       HAAR_DIR (C:/opencv/data/haarcascades/) and the filename
       supplied at the command line is in FACE_DIR (faces/);
       change these constants as required 
    */

> runAll
    // this calls run.bat 17 times, to test all the files 
    // currently stored in faces/

----------------------------
Last updated: 24th May 2013
