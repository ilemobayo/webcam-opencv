
// TCPanel.java
// Andrew Davison, July 2013, ad@fivedots.psu.ac.th

/* This panel repeatedly snaps a picture, find
   all the topcodes, and draw them over the webcam image.

   The topcode drawing includes its ID, a picture of the topcode,
   its (x, y, z) coordinate, and a line denoting its orientation from
   the vertical.
   
   Finger tracking is implemented by looking for the topcodes 
   107 and 47 which I assume are stuck on the user's index and middle
   fingers.

   Topcode 107 is used to track the position of the index finger and 
   if topcode 47 is detected it means 'pressed' (like a mouse button :))

   This information is passed to a TargetMover object which displays a
   target image in another window at the given position, or a 'fired' image
   if 'pressed' is true.

   The topcodes library can be downloaded from
      http://users.eecs.northwestern.edu/~mhorn/topcodes/
*/

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.videoInputLib.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.avutil.*;   // for grabber/recorder constants


import topcodes.*;



public class TCPanel extends JPanel implements Runnable
{
  // dimensions of panel == dimensions of webcam image
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private static final int DELAY = 50;  // time (ms) between redraws of the panel

  private static final int CAMERA_ID = 0;

  // topcode IDs used for the fingers
  private static final int INDEX_FINGER = 107;  
  private static final int MIDDLE_FINGER = 47;  

  private static final double FINGER_SCALE = 3;   
    // for increasing finger movement relative to webcam center

  private static final double DIST_DIA = 210 * 70;   
     /* for my camera a topcode has diameter 70 pixels when it is
        full-size, and this occurs when my hand is 210 mm from the camera,
        so a diameter is mapped to a z-distance using 210*70 
     */

  private BufferedImage im = null;    // current webcam image
  private volatile boolean isRunning;
  private volatile boolean isFinished;

  // topcode variables
  private topcodes.Scanner scanner;
  private java.util.List<TopCode> topCodes = null;

  // current relative finger position inside the panel
  private double xFinger = 0.5;   // at the center
  private double yFinger = 0.5;

  private TargetMover targetFrame;   
    // the window whose target is moved (and 'pressed') by finger movement



  public TCPanel(TargetMover tm)
  {
    targetFrame = tm;
    setBackground(Color.white);

    scanner = new topcodes.Scanner();
    new Thread(this).start();   // start updating the panel's image
  } // end of TCPanel()



  public Dimension getPreferredSize()
  {   return new Dimension(WIDTH, HEIGHT); }




  public void run()
  /* Display the current webcam image every DELAY ms.
     Find the topcodes in the image, and use them to track the
     position of the user's index finger and whether the middle
     finger is visible
  */
  { 
    FrameGrabber grabber = initGrabber(CAMERA_ID);
    if (grabber == null)
      return;

    IplImage snapIm;
    long duration;
    isRunning = true;
    isFinished = false;

    while (isRunning) {
      long startTime = System.currentTimeMillis();

      snapIm = picGrab(grabber, CAMERA_ID); 
      im = snapIm.getBufferedImage();
      topCodes = scanner.scan(im);  // find topcodes in the image
      trackFingers(topCodes);
      repaint();

      duration = System.currentTimeMillis() - startTime;
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



  // ----------------- finger tracking -------------------------

  private void trackFingers(java.util.List<TopCode> topCodes) 
  /*  Look for topcodes 107 and 47: topcode 107 (INDEX_FINGER) locates 
      the position of the user's index finger, while topcode 47 (MIDDLE_FINGER)
      indicates that the middle finger is raised.

      A target image managed by targetFrame is positioned using topcode 107's
      position, and is 'pressed' is topcode 47 is visible.
  */
  {
    boolean isPressed = false;
    for (TopCode tc : topCodes) {
      int id = tc.getCode();
      if (id == INDEX_FINGER) {  // topcode 107 found
        // calculate distance of finger point from center of the webcam image
        int xDist = (int)tc.getCenterX() - WIDTH/2;    
        int yDist = (int)tc.getCenterY() - HEIGHT/2;
        
        /* scale distances, and convert to percentage positions inside the 
           image (the values may be greater than 1 due to the scaling)  */
        xFinger = ((double)(WIDTH/2 + xDist*FINGER_SCALE))/WIDTH;   
        yFinger = ((double)(HEIGHT/2 + yDist*FINGER_SCALE))/HEIGHT;
      }
      else if (id == MIDDLE_FINGER)   // topcode 47 is visible
        isPressed = true;
    }

    // move the target using the finger's relative position inside the panel
    // and the isPressed boolean 
    targetFrame.setTarget(xFinger, yFinger, isPressed);
  }  // end of trackFingers()



  // ------------------------- painting ----------------------------


  public void paintComponent(Graphics g)
  // draw the webcam image and all the topcodes
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                       RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    if (im != null) {
      g2.drawImage(im, 0, 0, this);
      drawTopCodes(g2, topCodes);
    }
    else
      g2.drawString("Loading from camera " + CAMERA_ID + "...", 20, HEIGHT/2);
  } // end of paintComponent()



  private void drawTopCodes(Graphics2D g2,
                            java.util.List<TopCode> topCodes)
  // draw all the topcodes
  {
    if ((topCodes == null) || (topCodes.size() == 0))   // no topcodes
      return;

    for (TopCode tc : topCodes) {
      tc.draw(g2);    // draw a topcode image at its location on the image
      drawID(g2, tc);
      drawPos(g2, tc);     // draw (x,y,z) and orientation line
    }
  }  // end of drawTopCodes()



  private void drawID(Graphics2D g2, TopCode tc)
  // draw the topcode ID number in a text box below the topcode image
  {
    String idStr = String.valueOf( tc.getCode() );
    int y = (int)(tc.getCenterY() + tc.getDiameter()*0.7 + 8);

    drawTextBox(g2, idStr, (int)tc.getCenterX(), y);
  }  // end of drawID()




  private void drawTextBox(Graphics2D g2, String msg, int x, int y)
  // draw the msg in black inside a white box centered at (x,y)
  {
    int strWidth = g2.getFontMetrics().stringWidth(msg);

    g2.setColor(Color.WHITE);    // white box
    g2.fillRect( x - strWidth/2 - 3,  y - 16, strWidth + 6, 16);

    g2.setColor(Color.BLACK);    // black text
    g2.drawString(msg, x - strWidth/2, y - 4);
  }  // end of drawTextBox()



  private void drawPos(Graphics2D g2, TopCode tc)
  /* draw the (x,y,z) coordinate and angle to the vertical for 
     the topcode; (x, y) are pixel positions in the webcam image, 
     but z is a millimeter distance from the camera; the angle is
     in degrees, and measured clockwise from straight up.
  */
  {
    int xc = (int) tc.getCenterX();    // in pixels
    int yc = (int) tc.getCenterY();

    int zDist = -1;
    float dia = tc.getDiameter();   // diameter in pixels
    if (dia > 0)
      zDist = (int)(DIST_DIA / dia);   // z-distance in mm from camera

    int angle = 360 + (int) Math.toDegrees(tc.getOrientation());
             // since topcodes orientation varies from -360 to 3
    angle = angle % 360;

    // calculate a rotated point using the angle and (xc, yc)
    int xEnd = xc + (int)( 0.9 * dia * Math.sin(Math.toRadians(angle)));
    int yEnd = yc - (int)( 0.9 * dia * Math.cos(Math.toRadians(angle)));

    // draw topcode coordinate in a text box
    String coord = String.valueOf("(" + xc + ", " + yc + ", " + zDist + ")");
    drawTextBox(g2, coord, xEnd, yEnd);

    // draw a angled line going to the box
    g2.setColor(Color.RED);  // rounded, thick red line
    g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));   
    g2.drawLine(xc, yc, xEnd, yEnd);
  }  // end of drawPos()


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


} // end of TCPanel class

