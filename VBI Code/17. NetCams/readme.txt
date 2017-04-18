
Chapter 36. Networked Cameras

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
Example Files:

The client-side classes:
* CamReader.java
* CamReaderPanel.java
* ImageUtils.java    (used on both the client and server sides)

The server-side classes:
* CamsViewer.java
* CamViewerPanel.java
* ImageUtils.java    (used on both the client and server sides)

Two batch files:
* compile.bat
* run.bat
        -- these are only need to compile and run the client-side
           of the code which uses JavaCV and OpenCV



---------------------------------
Compilation on the server-side: 

$ javac CamsViewer.java


----------------------------
Before Compilation/Execution of the client-side:

You need to have a webcam connected to your PC.

You need to download and install:

  * OpenCV:  I downloaded v2.4.5 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.5-bin.zip
             and installed it in d:\javacv-bin

---------------------------------
Compilation on the client-side: 

$ compile CamReader.java
    // you must have JavaCV and OpenCV installed


---------------------------------
Server-side Execution: 

$ java CamsViewer

Part of the output will be the IP address of the host machine.
Make a note of it for when you start the CamReader clients.
e.g.
Viewer's IP address: 1.1.1.1
Port: 4444
Waiting for a cam connection...


---------------------------------
Client-side Execution: 

$ run CamReader <IP address of server>
e.g.
$ run CamReader 1.1.1.1
    // you must have JavaCV and OpenCV installed

---------
Last updated: 20th August 2013