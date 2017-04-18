
NUI Chapter 5. HSV Selector

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

This directory contains two applications spread over 4 Java files:
  * HSVSelector.java, HSVPanel.java, 
    ColorRectDetector.java
and
  * RangeSliderDemo.java


This is one Java package, used by both applications
  * rslider/
     -- this contains RangeSlider, a GUI component originally
        developed by Ernie Yu
        (http://ernienotes.wordpress.com/2010/12/27/creating-a-java-swing-range-slider/)


There is one HSV configuration files, which is read/written to by
the HSVSelector application:
  * HSV.txt


There are 2 batch files used by the HSVSelector application:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of JavaCV and OpenCV


----------------------------
Before Compilation/Execution:

You need to download and install:
  * OpenCV:  I downloaded v2.4.5 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.5-bin.zip
             and installed it in d:\javacv-bin


To help with HSV selection, download Shervin Emami’s HSV Color Wheel program:
   http://www.shervinemami.co.cc/colorWheelHSV.7z

----------------------------
Physical Materials:

You need a coloured card, whose HSV values can be obtained by adjusting
the sliders in HSVSelector. The values can be saved in HSV.txt, and then
used later in BlobsDrumming.

----------------------------
Compilation:

> javac RangeSliderDemo.java
    -- this will compile the contents of rslider/ as well

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> java RangeSliderDemo

> run HSVSelector
    // you must have JavaCV and OpenCV installed


----------------------------
Last updated: 6th July 2013
