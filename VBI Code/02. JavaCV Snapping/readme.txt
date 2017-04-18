
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

This directory contains 3 subdirectories:

  * OpenCV SnapPics/
        - webcam capture using Java OpenCV
        - NOTE: JavaCV is not required

  * ShowImage/
        - contains a simple example using JavaCV

  * SnapPics/
        - webcam capture using JavaCV

For more details, see the readme.txt files in the relevant directories.


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
     - NOTE: JavaCV is not required for the OpenCV SnapPics/ example


----------------------------
Standalone Tools for Checking a Webcam

As explained in the chapter, it's useful to have some simple tools for
checking the webcam which don't depend on JavaCV/OpenCV. I use:

  * CommandCam (http://batchloaf.wordpress.com/commandcam/)

  * DevCon (http://support.microsoft.com/kb/311272)

  * FFmpeg (http://www.ffmpeg.org/)


----------------------------
Webcam Snapping Without Using JavaCV/OpenCV


The old version of chapter 2 describes Webcam snapping using JMF
(see http://fivedots.coe.psu.ac.th/~ad/jg/nui01/)

Another approach is to use vlcj, the Java API for the VLC media player 
(see http://fivedots.coe.psu.ac.th/~ad/jg/nui025/).


----------------------------
Last updated: 27th June 2013
