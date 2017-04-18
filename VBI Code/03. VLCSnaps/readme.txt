
Chapter NUI-2.5. Webcam Snaps Using VLC

From:
  'Killer Game Programming in Java' website
  http://fivedots.coe.psu.ac.th/~ad/jg


Contact Address:
  Dr. Andrew Davison
  Dept. of Computer Engineering
  Prince of Songkla University
  Hat Yai, Songkhla 90112, Thailand
  E-mail: ad@fivedots.coe.psu.ac.th
  http://fivedots.coe.psu.ac.th/~ad/jg


If you use this code, please mention my name, and include a link
to the book's Web site.

Thanks,
  Andrew

---------------------------------


Three Java applications: 
  * Player.java         -- plays a video file
  * CaptureTest.java    -- plays video coming from a capture device
  * Capturer.java,      -- read video from a capture device, but display it as a sequence of snapshots
    CaptureSnapPanel.java

Example Video file:
  * FunnyCats.flv       -- for testing Player.java

Two batch files:
  * compile.bat, run.bat

One directory:
  * SnapPics/           -- uses VLCCapture (see readme.txt in that directory)

---------------------------------
Libraries Needed

* VLC media player (http://www.videolan.org/)
    -- install this first; test the media player with FunnyCats.flv

* vlcj, the Java binding for the VLC C API
  (http://code.google.com/p/vlcj/ or https://github.com/caprica/vlcj)
      -- make sure you download the version that matches the VLC version
         e.g. I downloaded vlcj 2.3.1 which supposedly requires VLC 2.3.1 for
              some features (although capture works fine with VLC 2.0.7), 
              and unzipped it to d:/vlcj-2.3.1

* Make sure the paths to VLC and vlcj are correct in
  compile.bat and run.bat

---------------------------------
Compilation and Execution: 

> compile *.java

---
> run Player <video file>
e.g.
> run Player FunnyCats.flv

---
> run CaptureTest

> run Capturer

Note: before running these applications,you will need to modify the CAMERA_NAME constants in 
CaptureTest.java and CaptureSnapPanel (called by Capturer.java) to refer
to the capture device name on your machine. As explained in the chapter, you
can use the VLC media player to find out the device's name.

---------
Last updated: 2nd July 2013