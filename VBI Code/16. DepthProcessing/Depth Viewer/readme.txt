
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

This directory contains 5 Java files:
  * DepthViewer.java, ImagePanel.java,
    SliderBox.java, SliderBoxWatcher.java,
    DepthCalc.java


A subdirectory:
  * stereoData/
      -  **** IMPORTANT ****
         this sub-directory is empty, saving 31 MB. 
         To get the images, download the original zipped code folder at
         http://fivedots.coe.psu.ac.th/~ad/jg/nuiN13/



Four files saved automatically at the end of depth processing:
  * anaglyph.jpg     - the anaglyph image

  * bsState.txt      - the current block-matching parameters for the sliders
                       (these are *not* loaded by DepthViewer at start-up time, only
                        saved at the end)

  * gDispMap.jpg     - the grayscale disparity map

  * pointCloud.ply   - a point cloud saved in PLY ASCII format
                     - I use MeshLab (http://meshlab.sourceforge.net/) to view it in 3D


Two batch files:
  * compile.bat
  * run.bat
     - make sure they refer to the correct locations for your
       downloads of JavaCV and OpenCV


----------------------------
Before Compilation/Execution:

You need to download and install:

  * OpenCV:  I downloaded v2.4.3 for Windows with pre-compiled binaries:
             from http://opencv.org/downloads.html
             and installed it in C:\opencv

  * JavaCV:  http://code.google.com/p/javacv/
             I downloaded javacv-0.3-bin.zip
             and installed it in C:\javacv-bin


NOTE: you don't need to have webcams connected to your PC
at this stage, but you do need image pairs stored inside 
the stereoData/ subdirectory.

----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed

----------------------------
Execution:

> run DepthViewer -n <number>   [ draw ]
    - runs DepthViewer in calibration mode

    - there must be <number> image pairs in the stereoData/ subdirectory,
      which are PNG files with the names "left" and "right" and a two-digit
      ID number. The numbers must start at 0 and run up to <number-1>, with
      no gaps

    - the optional "draw" flag switches on the drawing of the chessboard pattern
      as it is detected inside a calibration image

    - calibration doesn't use a GUI interface, so watch stdout to monitor its 
      progress

    - at the end of the calibration, the first image pair (i.e. those with ID 0)
      are automatically depth processed, and the GUI appears at that time
e.g.
> run DepthViewer -n 40



> run DepthViewer -p <number>
    - runs DepthViewer in depth processing mode
    
    - there must be an image pair in stereoData/ with the specified number ID
    
    - there must be saved calibration matricies in stereoData/:
         - 4 lookup matricies: mx1.txt, my1.txt, mx2.txt, my2.txt
         - the reprojection matrix: q.txt
    
    - when you press the close box, several files are saved, which delays the 
      application's termination by 1-2 seconds; please be patient :)
e.g.
> run DepthViewer -p 0

----------------------------
Last updated: 19th June 2013
