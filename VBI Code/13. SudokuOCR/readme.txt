
NUI Chapter 12. OCR and Sudoku

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
  *  SudokuOCR.java, ScanPanel.java, GridPanel.java,
     GridVisualizer.java,    // where the OpenCV code is located
     Solver.java,            // where the Sudoku search function is located
     SaferExec.java, NumberPos.java,
     SpinnerCircularListModel.java


There are 3 image files:
  *  horiz.png, vertical.png
        // these are used to label then grid display on the right side of the GUI
  * example.pnm
        // an example image file that can be used to test xmlGocr.bat


There is a JAR file containing the TableLayout layout manager:
  * TableLayout-bin-jdk1.5-2009-08-26.jar
        // this is used to layout the 9x9 grid of JSpinner objects on the rhs of the GUI
        // I downloaded it from http://java.net/projects/tablelayout/


The gocr command tool:
  * gocr049.exe
       // this performs the OCR
       // I downloaded it from http://jocr.sourceforge.net/


There are 3 batch files:
  *  xmlGocr.bat
       // this batch file calls gocr049.exe with several options set
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


----------------------------
Compilation:

> compile *.java
    // you must have JavaCV and OpenCV installed
    
----------------------------
Execution:

> run SudokuOCR
    // you must have JavaCV and OpenCV installed


---------------------------------
Using gocr standalone

Use the xmlGocr.bat batch file to call gocr

> xmlGocr <image file in pnm format> <XML output file>

e.g.
> xmlGocr example.pnm out.xml


--------------------------------
Last updated: 18th July 2013
