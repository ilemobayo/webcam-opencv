
NUI Chapter 8.7. Finger Tracking with TopCodes

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
  * TopCodesFingers.java, TCPanel.java,
    TargetMover.java, MoverPanel.java


2 images used for displaying a crosshairs target:
  * crosshairs.png
  * shotHairs.png


Two batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of TopCodes, JavaCV, and OpenCV


----------------------------
Before Compilation/Execution:

You need to have a webcam connected to your PC.

You need to download and install:

  * topCodes: http://users.eecs.northwestern.edu/~mhorn/topcodes/
          I downloaded http://users.eecs.northwestern.edu/~mhorn/topcodes/topcodes.zip
          --> I unzipped it to D:\topcodes\
          --> for testing see the "Quick Start Guide" at the topcodes website

  * OpenCV:  I downloaded v2.4.5 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.5-bin.zip
             and installed it in d:\javacv-bin

----------------------------
Other requirements:

  * a sheet of topcodes graphics from 
    http://users.eecs.northwestern.edu/~mhorn/topcodes/topcodes.pdf
      --> print out the topcodes for IDs 107 and 47 which I use in my code
          (the INDEX_FINGER and MIDDLE_FINGER constants in TCPanel)


   * a glove (optional, but it's more convenient to stick the topcodes to
              a glove than your hand)


   * calculate the DIST_DIA constant in TCPanel according to
     your webcam.
     For my camera, DIST_DIA == 210 * 70, which means that my hand was 
     210 mm from the camera when the topcode reported a diameter of 70 pixels.
     You can uncomment the println statment in drawPos() in TCPanel
     to print the diameter (and other info) to stdout. Use a ruler to measure
     your hand's distance from the camera.

----------------------------
Compilation:

> compile *.java
    // you must have TopCodes, JavaCV, and OpenCV installed

----------------------------
Execution:

> run TopCodesFingers
    // you must have TopCodes, JavaCV, and OpenCV installed


----------------------------
Last updated: 7th July 2013
