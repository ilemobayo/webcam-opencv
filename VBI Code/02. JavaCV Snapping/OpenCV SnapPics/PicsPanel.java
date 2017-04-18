
// PicsPanel.java
// Andrew Davison, June 2013, ad@fivedots.psu.ac.th

/* Snap pictures from a camera every DELAY ms, and show in the panel.
   A good DELAY value can be obtained by looking at the 
   average ms time for obtaining a snap, which is written at
   the bottom of the panel.

   Uses OpenCV VideoCapture to grab webcam snaps

   If the user presses <enter>, <space> or '5' then the current image
   is saved in the SAVE_DIR directory as numbered images files with
   the name "pic"
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;


import org.opencv.core.*; 
import org.opencv.highgui.*; 
import org.opencv.imgproc.Imgproc;



public class PicsPanel extends JPanel implements Runnable
{
  // dimensions of each image, and of the panel
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private static final int DELAY = 100;  // ms 

  private static final int CAMERA_ID = 0;

  // directory and filenames used to save images
  private static final String SAVE_DIR = "pics/"; 
  private static final String PIC_FNM = "pic";


  private volatile boolean isRunning;
  private volatile boolean isFinished;

  // used for the average ms snap time info
  private long totalTime = 0;
  private int imageCount = 0;
  private Font msgFont;

  private Mat snapIm = null;

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
  // take pictures every DELAY ms
  {
    VideoCapture grabber = initGrabber(CAMERA_ID);
    if (grabber == null)
      return;

    long duration;
    int snapCount = 0;
    isRunning = true;
    isFinished = false;

    while (isRunning) {
      long startTime = System.currentTimeMillis();

      snapIm = new Mat();        
      grabber.read(snapIm);

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
    grabber.release();
    System.out.println("Execution terminated");
    isFinished = true;
  }  // end of run()



  private VideoCapture initGrabber(int ID)
  {
    VideoCapture grabber = new VideoCapture(ID);
    if ((grabber == null) || (!grabber.isOpened())) {
      System.out.println("Cannot connect to webcam: " + ID);
      System.exit(1);
    }
    else {
      System.out.println("Connected to webcam: " + ID);
      grabber.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, WIDTH);     // make sure of frame size
      grabber.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, HEIGHT);
    }

    return grabber;
  }  // end of initGrabber()




  private void saveImage(Mat snapIm, String saveFnm, int snapCount)
  /* save a grayscale version of the image as a JPG file in SAVE_DIR.
     The file is called saveFnm, followed by a 2-digit number.
  */
  {
    if (snapIm == null) {
      System.out.println("Not saving a null image");
      return;
    }

    Mat grayImage  = new Mat(WIDTH, HEIGHT, CvType.CV_8UC1);
    Imgproc.cvtColor(snapIm, grayImage, Imgproc.COLOR_BGR2GRAY);

    String fnm = (snapCount < 10) ? 
                SAVE_DIR + saveFnm + "0" + snapCount +".jpg" :
                SAVE_DIR + saveFnm + snapCount +".jpg";
    System.out.println("Saving image " + fnm);
    Highgui.imwrite(fnm, grayImage);
  }  // end of saveImage()



  public void paintComponent(Graphics g)
  /* Draw the snap and add the average ms snap time at the 
     bottom of the panel. */
  { 
    super.paintComponent(g);

    g.setFont(msgFont);

    // draw the image on the panel
    if (snapIm != null) {
      g.setColor(Color.YELLOW);
      g.drawImage( matToImage(snapIm), 0, 0, this);   // draw the snap
      String statsMsg = String.format("Snap Avg. Time:  %.1f ms",
                                        ((double) totalTime / imageCount));
      g.drawString(statsMsg, 5, HEIGHT-10);  
                        // write statistics in bottom-left corner
    }
    else {  // no image yet
      g.setColor(Color.BLUE);
      g.drawString("Loading from camera " + CAMERA_ID + "...", 5, HEIGHT-10);
    }
  } // end of paintComponent()



  private BufferedImage matToImage(Mat snapIm)
  // convert OpenCV's Mat into a BufferedImage
  {
    MatOfByte matOfByte = new MatOfByte();
    Highgui.imencode(".jpg", snapIm, matOfByte); 
    byte[] byteArray = matOfByte.toArray();

    BufferedImage bufImage = null;
    try {
      InputStream in = new ByteArrayInputStream(byteArray);
      bufImage = ImageIO.read(in);
    } 
    catch (Exception e) {
      System.out.println("Could not convert matrix to a BufferedImage");
    }
    return bufImage;
  }  // end of matToImage()


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

