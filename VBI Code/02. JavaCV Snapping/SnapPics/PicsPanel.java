
// PicsPanel.java
// Andrew Davison, June 2013, ad@fivedots.psu.ac.th

/* Snap pictures from a camera every DELAY ms, and show in the panel.
   A good DELAY value can be obtained by looking at the 
   average ms time for obtaining a snap, which is written at
   the bottom of the panel.

   Uses JavaCV to grab webcam snaps

   If the user presses <enter>, <space> or '5' then the current image
   is saved in the SAVE_DIR directory as numbered images files with
   the names PIC_FNM.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.videoInputLib.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.avutil.*;   // for grabber/recorder constants



public class PicsPanel extends JPanel implements Runnable
{
  /* dimensions of each image; the panel is the same size as the image */
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private static final int DELAY = 100;  // ms 

  // directory and filenames used to save images
  private static final String SAVE_DIR = "pics/"; 
  private static final String PIC_FNM = "pic";

  private static final int CAMERA_ID = 0;


  private volatile boolean isRunning;
  private volatile boolean isFinished;

  // used for the average ms snap time info
  private long totalTime = 0;
  private int imageCount = 0;
  private Font msgFont;

  private IplImage snapIm = null;

  private volatile boolean takeSnap = false;



  public PicsPanel()
  {
    setBackground(Color.white);
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    prepareSnapDir();

    new Thread(this).start();   // start updating the panel's image
  } // end of PicsPanel()



  public Dimension getPreferredSize()
  // make the panel wide enough for an image
  {   return new Dimension(WIDTH, HEIGHT); }



  private void prepareSnapDir()
  /* make sure there's a SAVE_DIR directory, and backup
     any images in there by prefixing them with "OLD_"
  */
  {
    File saveDir = new File(SAVE_DIR);

    if (saveDir.exists()) {   // backup any existing files
      File[] listOfFiles = saveDir.listFiles();
      if (listOfFiles.length > 0) {
        System.out.println("Backing up files in " + SAVE_DIR);
        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile()) {
            File nFile = new File(SAVE_DIR + "OLD_" + listOfFiles[i].getName()); 
            listOfFiles[i].renameTo(nFile);
          }
        }
      }
    }
    else {   // directory does not exist, so create it
      System.out.println("Creating directory: " + SAVE_DIR);
      boolean isCreated = saveDir.mkdir();  
      if(!isCreated) {
        System.out.println("-- could not create");  
        System.exit(1);
      }
    }
  }  // end of prepareSnapDir()



  public void run()
  /* take pictures every DELAY ms */
  {
    FrameGrabber grabber = initGrabber(CAMERA_ID);
    if (grabber == null)
      return;

    long duration;
    int snapCount = 0;
    isRunning = true;
    isFinished = false;

    while (isRunning) {
      long startTime = System.currentTimeMillis();

      snapIm = picGrab(grabber, CAMERA_ID); 

      if (takeSnap) {   // save the current image
        saveImage(snapIm, PIC_FNM, snapCount);
        snapCount++;
        takeSnap = false;
      }

      imageCount++;
      repaint();

      duration = System.currentTimeMillis() - startTime;
      totalTime += duration;
      if (duration < DELAY) {
        try {
          Thread.sleep(DELAY-duration);  // wait until DELAY time has passed
        } 
        catch (Exception ex) {}
      }
    }
    closeGrabber(grabber, CAMERA_ID);
    System.out.println("Execution terminated");
    isFinished = true;
  }  // end of run()



  private FrameGrabber initGrabber(int ID)
  {
    FrameGrabber grabber = null;
    System.out.println("Initializing grabber for " + videoInput.getDeviceName(ID) + " ...");
    try {
      grabber = FrameGrabber.createDefault(ID);
      grabber.setFormat("dshow");       // using DirectShow
      grabber.setImageWidth(WIDTH);     // default is too small: 320x240
      grabber.setImageHeight(HEIGHT);
      grabber.start();
    }
    catch(Exception e) 
    {  System.out.println("Could not start grabber");  
       System.out.println(e);
       System.exit(1);
    }
    return grabber;
  }  // end of initGrabber()



  private IplImage picGrab(FrameGrabber grabber, int ID)
  {
    IplImage im = null;
    try {
      im = grabber.grab();  // take a snap
    }
    catch(Exception e) 
    {  System.out.println("Problem grabbing image for camera " + ID);  }
    return im;
  }  // end of picGrab()



  private void closeGrabber(FrameGrabber grabber, int ID)
  {
    try {
      grabber.stop();
      grabber.release();
    }
    catch(Exception e) 
    {  System.out.println("Problem stopping grabbing for camera " + ID);  }
  }  // end of closeGrabber()



  private void saveImage(IplImage snapIm, String saveFnm, int snapCount)
  /* save a grayscale version of the image as a JPG file in SAVE_DIR.
     The file is called saveFnm, followed by a 2-digit number.
  */
  {
    if (snapIm == null) {
      System.out.println("Not saving a null image");
      return;
    }

    IplImage grayImage  = IplImage.create(WIDTH, HEIGHT, IPL_DEPTH_8U, 1);
    cvCvtColor(snapIm, grayImage, CV_BGR2GRAY);

    String fnm = (snapCount < 10) ? 
                SAVE_DIR + saveFnm + "0" + snapCount +".jpg" :
                SAVE_DIR + saveFnm + snapCount +".jpg";
    System.out.println("Saving image " + fnm);
    cvSaveImage(fnm, grayImage);
  }  // end of saveImage()



  public void paintComponent(Graphics g)
  /* Draw the snaps side-by-side and add the average ms snap time at the 
     bottom of the panel. */
  { 
    super.paintComponent(g);

    g.setFont(msgFont);

    // draw the image and stats 
    if (snapIm != null) {
      g.setColor(Color.YELLOW);
      g.drawImage(snapIm.getBufferedImage(), 0, 0, this);   // draw the snap
      String statsMsg = String.format("Snap Avg. Time:  %.1f ms",
                                        ((double) totalTime / imageCount));
      g.drawString(statsMsg, 5, HEIGHT-10);  
                        // write statistics in bottom-left corner
    }
    else  {// no image yet
      g.setColor(Color.BLUE);
      g.drawString("Loading from camera " + CAMERA_ID + "...", 5, HEIGHT-10);
    }
  } // end of paintComponent()


  // --------------- called from the top-level JFrame ------------------

  public void closeDown()
  /* Terminate run() and wait for it to finish.
     This stops the application from exiting until everything
     has finished. */
  { 
    isRunning = false;
    while (!isFinished) {
      try {
        Thread.sleep(DELAY);
      } 
      catch (Exception ex) {}
    }
  } // end of closeDown()


  public void takeSnap()
  {  takeSnap = true;   } 

} // end of PicsPanel class

