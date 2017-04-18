     
// FFPanel.java
// Andrew Davison, July 2013, ad@fivedots.psu.ac.th

/* This panel repeatedly snaps a picture and draw it onto
   the panel. Extract the face features using the FaceSDK API
   (http://luxand.com/facesdk/) and draw them over the image.

   Add mood and mustache information to the image 

*/


import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.videoInputLib.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.avutil.*;   // for grabber/recorder constants




public class FFPanel extends JPanel implements Runnable
{
  // dimensions of this panel == dimensions of webcam image
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private static final int DELAY = 900; // time (ms) between redraws of the panel

  private static final int CAMERA_ID = 0;

  // used for mood detection using mouth/nose 'analysis'
  private static final double SMALL_MOUTH = 1.1;
  private static final double WIDE_MOUTH = 1.4;


  private BufferedImage snapIm = null;     // changed from IplImage
  private volatile boolean isRunning;
  private volatile boolean isFinished;

  private FaceInfo faceInfo;

  // mood booleans
  private boolean isSad = false;
  private boolean isHappy = false;
  private Font msgFont;

  private BufferedImage mustacheIm;



  public FFPanel()
  {
    setBackground(Color.white);

    msgFont = new Font("SansSerif", Font.BOLD, 36);
    mustacheIm = loadImage("mustache.png");

    new Thread(this).start(); // start updating the panel's image
  } // end of FFPanel()



  private BufferedImage loadImage(String fnm)
  // load the image from fnm
  {
    BufferedImage im = null;
    try {
      im = ImageIO.read( new File(fnm));
      System.out.println("Loaded image from " + fnm);
    }
    catch (Exception e)
    { System.out.println("Unable to load image from " + fnm);  }

    return im;
  }  // end of loadImage()



  public Dimension getPreferredSize()
  // dimensions of this panel == dimensions of webcam image
  {   return new Dimension(WIDTH, HEIGHT); }



  public void run() 
  /* display the current webcam image every DELAY ms
     Each image is processed to find face features
  */
  {
    FrameGrabber grabber = initGrabber(CAMERA_ID);
    if (grabber == null)
      return;

    faceInfo = new FaceInfo();

    long duration;
    isRunning = true;
    isFinished = false;

    while (isRunning) {
      long startTime = System.currentTimeMillis();

      snapIm = (picGrab(grabber, CAMERA_ID)).getBufferedImage(); 

      faceInfo.update(snapIm);   // update face features
      moodDetector();
      repaint();

      duration = System.currentTimeMillis() - startTime;
      // System.out.println("Feature detection time: " + duration);   
                       // about 800 ms on my 2-core test PC

      if (duration < DELAY) {
        try {
          Thread.sleep(DELAY - duration); // wait until DELAY time has passed
        }
        catch (Exception ex) {}
      }
    }
    faceInfo.close();
    closeGrabber(grabber, CAMERA_ID);
    System.out.println("Execution End");
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




  private void moodDetector()
  // extremely sophisticated analyzer for user mood detection
  {
    if (!faceInfo.hasPoints())
      return;

    // calculate nose width
    Point noseLeft = faceInfo.getPt(FeatureID.NOSE_LEFT_WING_OUTER);
    Point noseRight = faceInfo.getPt(FeatureID.NOSE_RIGHT_WING_OUTER);
    double noseWidth = noseLeft.distance(noseRight);

    // calculate mouth width
    Point mouthLeft = faceInfo.getPt(FeatureID.MOUTH_LEFT_CORNER);
    Point mouthRight = faceInfo.getPt(FeatureID.MOUTH_RIGHT_CORNER);
    double mouthWidth = mouthLeft.distance(mouthRight);

    double mouthRatio = mouthWidth/noseWidth;
    // System.out.printf("mouth/nose ratio: %.3f\n", mouthRatio);

    // convert ratio into a mood boolean setting
    isSad = false;
    isHappy = false;
    if (mouthRatio <= SMALL_MOUTH)
      isSad = true;
    else if (mouthRatio >= WIDE_MOUTH)
      isHappy = true;
  }  // end of moodDetector()



  // -------------------------------- painting -------------------------------


  public void paintComponent(Graphics g) 
  /* Draw the webcam image, and face features */
  {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setFont(msgFont);

    if (snapIm == null)
      g2.drawString("Initializing webcam, please wait...", 20, HEIGHT/2);
    else {
      g2.drawImage(snapIm, 0, 0, this);
      faceInfo.draw(g2);
      // reportMood(g2);
      // attachStache(g2);
    }
  } // end of paintComponent()




  private void reportMood(Graphics2D g2)
  // print user's mood on his forehead
  {
    if (!faceInfo.hasPoints())
      return;

    Point noseBridge = faceInfo.getPt(FeatureID.NOSE_BRIDGE);

    if (isSad) {
      g2.setColor(Color.RED);
      g2.drawString("SAD", noseBridge.x-45, noseBridge.y-60);
    }
    else if (isHappy) {
      g2.setColor(Color.GREEN);
      g2.drawString("HAPPY", noseBridge.x-50, noseBridge.y-60);
    }
  }  // end of reportMood()




  private void attachStache(Graphics2D g2)
  // draw a moustache centered mid-way between the nose bottom and mouth top
  {
    if (!faceInfo.hasPoints())
      return;

    Point mouthTop = faceInfo.getPt(FeatureID.MOUTH_TOP);
    Point noseBottom = faceInfo.getPt(FeatureID.NOSE_BOTTOM);
    int xC = (mouthTop.x + noseBottom.x)/2;
    int yC = (mouthTop.y + noseBottom.y)/2;

    if (mustacheIm != null)
      g2.drawImage(mustacheIm, (xC - mustacheIm.getWidth()/2),
                               (yC - mustacheIm.getHeight()/2), this);
  }  // end of attacheStache()



} // end of FFPanel class

