
NUI Chapter 11. Fingerprint Recognition

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

This directory contains 8 Java files:

  * Templater.java, ScanPanel.java, ImagePanel.java,
    FingerPrint.java, ExtFilter.java, FingerUtils.java
         // the Templater application

  * Matcher.java, MatchInfo.java
         // the Matcher application


There is one subdirectory:
  * prints/
      // this is where fingerprints information is saved;
      // each fingerprint is represented by a 3 files. If XXX
         is the name of the fingerprint, then the files will be
          - XXX.png (the fingerprint image)
          - XXXTemplate.txt (the fingerprint template data)
          - XXXLabelled.png (an image combining the thinned fingerprint 
                             and the template data)


There are 2 batch files:
  *  compile.bat
  *  run.bat
       // make sure they refer to the correct locations for your
       // downloads of JavaCV and OpenCV


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

FingerUtils.java contains several methods copied from Scott Johnston's Biometric SDK
(http://sourceforge.net/projects/biometricsdk/) for image thinning, template
creation, and template matching. See the file for more information. 

You do NOT need to download your own copy of the Biometric SDK.

----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed
    
----------------------------
Execution of Templater:

> run Templater
    // you must have JavaCV and OpenCV installed


----------------------------
Fingerprints and a Webcam

My webcam doesn't have the resolution or focussing ability to directly 
snap pictures of fingerprints. 

My solution is to use pencil graphite and sticky tape to transfer 
an impression of my fingertips onto paper (e.g. as explained at 
http://www.wikihow.com/Take-Fingerprints). I photocopied the paper, 
enlarging the image, so my webcam could adequately focus on it.


----------------------------
Execution of Matcher:

> run Matcher <fingerprint name>

The fingerprint name (e.g. XXX) must already have been used in Templater
to generate 3 files in prints/:
    - XXX.png (the fingerprint image)
    - XXXTemplate.txt (the fingerprint template data)
    - XXXLabelled.png (an image combining the thinned fingerprint 
                       and the template data)

Matcher will compare the XXX template with all other templates found
in prints/

e.g.
>  run Matcher fingertest3
      -- this compares the fingertest3 template against the others
         in prints/ (it should match finger6)

--------------------------------
Last updated: 9th July 2013
