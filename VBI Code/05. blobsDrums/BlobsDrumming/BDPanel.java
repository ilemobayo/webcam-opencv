
// BDPanel.java
// Andrew Davison, July 2013, ad@fivedots.psu.ac.th

/* This panel repeatedly snaps a picture and draw it onto
   the panel. OpenCV is used, via the ColorRectDetector class, to detect
   two colored rectangles.

   Superimposed over the image is a grid of drums. When the rectangles
   are over a drum, it starts beating. The angle each rectangle makes
   to the vertical determines the rate of beating (a larger angle means
   faster beating).

   The two colored rectangles are taking the roles of 'drumsticks'.
*/

import java.awt.*;
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



public class BDPanel extends JPanel implements Runnable
{
  /* dimensions of each image; the panel is the same size as the image */
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private static final int DELAY = 100;  // time (ms) between redraws of the panel

  private static final int IMG_SCALE = 2;  // scaling applied to webcam image

  private static final int CAMERA_ID = 0;

  private static final int NUM_DETECTORS = 2;  
             /* each detector will find a coloured rectangle 
                in the image */


  // default HSV initial ranges
  private static final int HUE_LOWER = 0;
  private static final int HUE_UPPER = 179;
             // the Hue component ranges from 0 to 179 (not 255)

  private static final int SAT_LOWER = 0;
  private static final int SAT_UPPER = 255;

  private static final int BRI_LOWER = 0;
  private static final int BRI_UPPER = 255;


  private IplImage snapIm = null;
  private volatile boolean isRunning;
  private volatile boolean isFinished;

  // used for the average ms snap time information
  private int imageCount = 0;
  private long totalTime = 0;
  private Font msgFont;

  private ColorRectDetector[] detectors;   // for detecting the coloured rects
  private boolean haveDetectors = false;

  private DrumsManager drummer;   // manages all the drums



  public BDPanel()
  {
    setBackground(Color.white);
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    new Thread(this).start();   // start updating the panel's image
  } // end of BDPanel()



  public Dimension getPreferredSize()
  // make the panel wide enough for an image
  {   return new Dimension(WIDTH, HEIGHT); }



  public void run()
  /* display the current webcam image every DELAY ms.
     Find the coloured rectangles in the image using ColorRectDetector
     objects.
     The time statistics gathered here include the time taken to
     detect the colours.
  */
  { 
    FrameGrabber grabber = initGrabber(CAMERA_ID);
    if (grabber == null)
      return;

    // initialize the drums display, and the coloured rectangles  detectors
    drummer = new DrumsManager(WIDTH, HEIGHT, NUM_DETECTORS);  
         // create the drums, and use NUM_DETECTORS sticks to hit them
    initDetectors(WIDTH/IMG_SCALE, HEIGHT/IMG_SCALE);

    IplImage scaleImg = IplImage.create(WIDTH/IMG_SCALE, HEIGHT/IMG_SCALE, 8, 3);

    long duration;
    isRunning = true;
    isFinished = false;

    while (isRunning) {
	  long startTime = System.currentTimeMillis();

      snapIm = picGrab(grabber, CAMERA_ID); 
      imageCount++;
      cvResize(snapIm, scaleImg);
      updateDetectors(scaleImg);  
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
    drummer.stopPlaying();   // stop the drums playing
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



  private void initDetectors(int recWidth, int recHeight)
  /* create NUM_DETECTORS coloured rectangles detectors 
     (one detector per drum stick)  */
  {
    detectors = new ColorRectDetector[NUM_DETECTORS];

    detectors[0] = new ColorRectDetector(recWidth, recHeight);
    readHSVRanges("redHSV.txt", detectors[0]);

    detectors[1] = new ColorRectDetector(recWidth, recHeight);
    readHSVRanges("blueHSV.txt", detectors[1]);

    haveDetectors = true;
  }  // end of initDetectors()



  public void readHSVRanges(String fnm, ColorRectDetector detector)
  // read three lines for the lower/upper HSV ranges
  {
    try {
      BufferedReader in = new BufferedReader(new FileReader(fnm));
      int[] vals = extractInts( in.readLine(), HUE_LOWER, HUE_UPPER);     // get hues
      detector.setHueRange(vals[0], vals[1]);   // lower & upper 

      vals = extractInts( in.readLine(), SAT_LOWER, SAT_UPPER);     // get saturations
      detector.setSatRange(vals[0], vals[1]); 

      vals = extractInts( in.readLine(), BRI_LOWER, BRI_UPPER);     // get brightnesses
      detector.setBriRange(vals[0], vals[1]); 

      in.close();
      System.out.println("Read HSV ranges from " + fnm);
      // name = fnm;
    }
    catch (IOException e)
    {  System.out.println("Could not read HSV ranges from " + fnm);  }
  }  // end of readHSVRanges()



  private int[] extractInts(String line, int lower, int upper)
  /*  Format of line <word>:  lower upper
  */
  {
    int[] vals = new int[2];
    vals[0] = lower; vals[1] = upper;

    String[] toks = line.split("\\s+");
    try {
      vals[0] = Integer.parseInt(toks[1]);
      vals[1] = Integer.parseInt(toks[2]);
    }
    catch (NumberFormatException e)
    { System.out.println("Error reading line \"" + line + "\"");  }
    return vals;
  }  // end of extractInts()



  private void updateDetectors(IplImage scaleImg)
  // update detectors and drums with new image
  {
    Point center;
    for (int i=0; i < NUM_DETECTORS; i++) {
      if (detectors[i].findRect(scaleImg)) {
        center = detectors[i].getCenter();
        drummer.startBeating(i, center.x*IMG_SCALE, 
                                center.y*IMG_SCALE,   // undo scaling
                    (detectors[0].getAngle() - 90));  // change to vertical angle
      }
      else   // box not found; stop any drum beating for that detector
        drummer.stopBeating(i);
    }
  }  // end of updateDetectors()




  public void paintComponent(Graphics g)
  /* Draw the image, the drums and hit locations, the coloured
     rectangle outline boxes, and the 
     average ms snap time at the bottom left of the panel. 
  */
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                       RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    if (snapIm != null)
      g2.drawImage(snapIm.getBufferedImage(), 0, 0, this);

    if (drummer != null)
      drummer.draw(g2);     // draw all the drums

    if (haveDetectors)
      drawBoxes(g2);   // draw the rectangle outline boxes

    writeStats(g2);
  } // end of paintComponent()



  private void drawBoxes(Graphics2D g2)
  /* draw yellow outline boxes around the locations of the
     detected coloured boxes */
  {
    g2.setPaint(Color.YELLOW);
    g2.setStroke(new BasicStroke(4));  // thick yellow pen

    Polygon bbox;
    for(ColorRectDetector detector : detectors) {
      bbox = detector.getBoundedBox();
      if (bbox != null) {
        for (int i = 0; i < bbox.npoints; i++) {  // scale pts back to full size
          bbox.xpoints[i] *= IMG_SCALE;  
          bbox.ypoints[i] *= IMG_SCALE;
        }
        g2.drawPolygon(bbox);     // draw bounding box onto panel
      }
    }
  }  // end of drawBoxes()



  private void writeStats(Graphics2D g2)
  /* write statistics in bottom-left corner, or
     "Loading" at start time */
  {
    g2.setFont(msgFont);
	g2.setColor(Color.BLUE);

    if (imageCount > 0) {
      String statsMsg = String.format("Snap Avg. Time:  %.1f ms",
                                        ((double) totalTime / imageCount));
      g2.drawString(statsMsg, 5, HEIGHT-10);  
    }
    else // no image yet
	  g2.drawString("Loading...", 5, HEIGHT-10);
  }  // end of writeStats()



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


} // end of BDPanel class

