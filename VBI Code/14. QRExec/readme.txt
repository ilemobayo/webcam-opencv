
NUI Chapter 17. QR Codes

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

This directory contains 5 Java files:
  * QRExec.java, QRPanel.java, 
    QRCodex.java, ResultSounder.java, DesktopRun.java


There are 3 subdirectories:
  * core\    -- holds the ZXing core.jar library
  * javase\  -- holds the ZXing javase.jar library
  * sounds\  -- hold the success and fail sound clips used by ResultSounder


There are 2 batch files:
  * compile.bat
  * run.bat
     - they use Zxing JARs (core\core.jar, javase\javase.jar)
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

> run QRExec
    // you must have JavaCV and OpenCV installed

----
> run QRCodex  <some string>
e.g.
> run QRCodex "http://fivedots.coe.psu.ac.th/~ad/"

    // there is a main() function inside the QRCodex class which gives
       a brief example of how to use its encode and decode() functions 
       separately from the rest of the QRExec example. 

   // the barcode for the input string is saved in test.png

----------------------------

ZXing API

You do *not* have to download the ZXing API from 
http://code.google.com/p/zxing/downloads/list

I downloaded version 1.5, and compiled the core/ and javase/ subdirectories 
using their Ant build files. The resulting JARs are included with my code.

----------------------------

Last updated: 18th July 2013
