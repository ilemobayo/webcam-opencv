
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

Contents:
  * Chessboard.png
      - print this out, and use for the calibration phase in DepthViewer
      - stick it to a piece of cardboard to make it rigid, and easy to hold

  * DoubleGrabber/
      - use this to collect image pairs from two webcams
      - for more details see readme.txt in that directory

  * Depth Viewer/
      - use this to calibrate and depth process image pairs
      - for more details see readme.txt in that directory
      -  **** IMPORTANT ****
         the stereoData/ sub-directory is empty, saving 31 MB. 
         To get the images, download the original zipped code folder at
         http://fivedots.coe.psu.ac.th/~ad/jg/nuiN13/


============================

Before Starting:

You need to have **two** webcams connected to your PC.

You need to download and install:

  * OpenCV:  I downloaded v2.4.3 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.3-bin.zip
             and installed it in C:\javacv-bin

----------------------------
Last updated: 19th June 2013
