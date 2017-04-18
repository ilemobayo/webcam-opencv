
// CamReaderPanel.java
// Andrew Davison, August 2013, ad@fivedots.psu.ac.th

/* Take a picture every DELAY ms, and show in a
   this object's panel.

   The snaps are taken by a thread, which is terminated when the
   isRunning boolean is set to true by closeDown(). 

   Each snap is split into NUM_SEPS*NUM_SEPS subimages, and sent over
   TCP/IP to a receiver (a CamReceiver object spawned by a CamsViewer server) 
   which glues them back together. The parts have IDs
   going from 0 up to (NUM_SEPS*NUM_SEPS)-1, with the parts created row-by-row.

   To reduce network load, this object compares the brightness of each part
   with the brightness of the corresponding last part that was sent. If
   the brightness difference is less than BRIGHT_THRESHOLD then the part's 
   data is not sent. In that case, the receiver uses 
   the previously received part to reconstruct the large image at its end.
   At the end of a parts update the special message END_UPDATE is sent.

   Another optimization is the scaling of the input image to be the
   same size as PANEL_WIDTH * PANEL_HEIGHT.

   If this object sends the ID value END_TRANS to the receiver, it means that this 
   client is about to finish.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;


import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.videoInputLib.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.avutil.*;   // for grabber/recorder constants


public class CamReaderPanel extends JPanel implements Runnable
{
  private static final int PANEL_WIDTH = 320;
  private static final int PANEL_HEIGHT = 240;

  private static final int DELAY = 100;  // ms, time between snaps

  private static final int CAMERA_ID = 0;

  private static final int PORT = 4444;   // port where CamsViewer is listening

  private static final int NUM_SEPS = 4;    // separations per row and column

  // special message IDs
  private static final int END_UPDATE = -98;
  private static final int END_TRANS = -99;

  private static final int MAX_WRITE_FAILS = 50;

  private static final int BRIGHT_THRESHOLD = 150;   // to judge if two brightnesses are different

  
  private String ipAddr;    // IP address of CamsViewer

  private BufferedImage image = null;    // scaled snap
  private Font msgFont;

  private volatile boolean isRunning;
  private volatile boolean isFinished;

  private int numWriteFails = 0;
  private int[] brightness;   // brightness data for each subimage part


  public CamReaderPanel(String addr)
  {
    ipAddr = addr;

    setBackground(Color.white);
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    // initialize brightness data
    brightness = new int[NUM_SEPS*NUM_SEPS];
    for (int i=0; i < brightness.length; i++)
       brightness[i] = 0;

    new Thread(this).start();   // start updating the panel's image
  } // end of CamReaderPanel()


  public Dimension getPreferredSize()
  // how big this panel should be
  {   return new Dimension(PANEL_WIDTH, PANEL_HEIGHT); }



  public void run()
  /* Connect to CamsViewer using a socket, then enter a loop that reads a snap 
     and sends the parts which have sufficiently changed brightness
     levels over to CamsViewer
  */
  {
    FrameGrabber grabber = initGrabber(CAMERA_ID);
    if (grabber == null)
      return;

    IplImage snapIm = picGrab(grabber, CAMERA_ID); 
    if (snapIm == null)
      return;
    double scaleFactor = calcScale(snapIm.width(), snapIm.height());
    // System.out.printf("Scaling factor for input image: %.3f\n", scaleFactor);

    long duration;
    isRunning = true;
    isFinished = false;
    BufferedImage[] imParts;    // holds parts of the total image

    try {
      // connect to CamsViewer via a socket
      System.out.println("Connecting to " + ipAddr + ", port: " + PORT + "...");
      Socket sock = new Socket(ipAddr, PORT);
      DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

      while (isRunning) {
	    long startTime = System.currentTimeMillis();

        snapIm = picGrab(grabber, CAMERA_ID); 
        image = ImageUtils.scale(snapIm.getBufferedImage(), scaleFactor);     // scale the input image
        imParts = ImageUtils.split(image, NUM_SEPS);   // split into parts
        updateParts(dos, imParts);                       // send the parts that have changed

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
      sendMessage(dos, END_TRANS, null);   // tell CamsViewer that this client is finishing
      sock.close();
    }
    catch(IOException e)
    {  System.out.println("Could not connect to server"); 
       System.exit(1);
    }

    System.out.println("Reader shut down");
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




  private double calcScale(int imWidth, int imHeight)
  /* calculate the smallest scale factor (which will reduce the image by
     the most) so the resulting image is at most PANEL_WIDTH x PANEL_HEIGHT large */
  {
    double widthScale = ((double)PANEL_WIDTH)/imWidth;
    double heightScale = ((double)PANEL_HEIGHT)/imHeight;
    return (widthScale < heightScale) ? widthScale : heightScale;  // return smaller
  }  // end of calcScale()



  public void paintComponent(Graphics g)
  // draw the image
  { 
    super.paintComponent(g);

    // center the image
    if (image != null) {
      int x = (int)(PANEL_WIDTH - image.getWidth())/2;
      int y = (int)(PANEL_HEIGHT - image.getHeight())/2;
      g.drawImage(image, x, y, null);   // draw the snap
    }
    else  {      // no image yet
	  g.setColor(Color.blue);
      g.setFont(msgFont);
	  g.drawString("Loading...", 5, PANEL_HEIGHT-10);
    }
  } // end of paintComponent()



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




  private void updateParts(DataOutputStream dos, BufferedImage[] imParts)
  /* send all subimage parts whose brightness levels are different enough from the 
     corresponding parts sent previously. Finish this parts update with an
     END_UPDATE message.
 */
  {
    byte[] imPartBytes;
    for(int i=0; i < imParts.length; i++) {
      int currBright = ImageUtils.getAvgBrightness(imParts[i]);
      if (Math.abs(currBright - brightness[i]) >= BRIGHT_THRESHOLD) {   // levels are different
        imPartBytes = ImageUtils.imToBytes(i, imParts[i]);
        sendMessage(dos, i, imPartBytes);    // send message containing the image bytes
        brightness[i] = currBright;        // store this data's brightness level
      }
    }
    sendMessage(dos, END_UPDATE, null);   // tell server that this parts update has finished
  }  // end of updateParts()



  private void sendMessage(DataOutputStream dos, int idx, byte[] bytes)
  /*  The message format is:
             <part index> <byte array length>  <image data bytes ...>
        or    END_UPDATE
        or    END_TRANS
      If the ID is END_UPDATE then it means that this round of updates is finished.
      If the ID is END_TRANS then it means that this client is closing down.
  */
  {
    try {
      if (idx == END_UPDATE) {
        dos.writeInt(idx);
      }
      else if (idx == END_TRANS) {
        System.out.println("Sending client END message");
        dos.writeInt(idx);
      }
      else {   // send part as a message
        dos.writeInt(idx);
        dos.writeInt(bytes.length);
        dos.write(bytes, 0, bytes.length);
      }
    }
    catch(IOException e)
    {  
      numWriteFails++;
      if (numWriteFails >  MAX_WRITE_FAILS) {
        System.out.println("Terminating due to multiple server write fails");
        System.exit(1);
      }
    }
  }  // end of sendMessage()


} // end of CamReaderPanel class

