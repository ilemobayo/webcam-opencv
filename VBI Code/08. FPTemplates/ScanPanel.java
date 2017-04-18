
// ScanPanel.java
// Andrew Davison, July 2013, ad@fivedots.coe.psu.ac.th

/* This panel repeatedly snaps a picture and draw it onto
   the panel.  

   The class has two public methods that are triggered by GUI button presses
   in the enclosing JFrame:
     * findPrint()  -- finds a fingerprint in the webcam image. Its box outline
                       will be drawn onto the webcam image;
                    -- the extracted image is drawn separately in the fpPanel ImagePanel

     * analyzePrint() -- get the user to select a name for the fingerprint image
                      -- create a FingerPrint object which contains template and labelled image info. 
                      -- display the labelled fingerprint image in the skelPanel ImagePanel
*/

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;


import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.videoInputLib.*;
import com.googlecode.javacpp.Loader;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.avutil.*;   // for grabber/recorder constants



public class ScanPanel extends JPanel implements Runnable
{
  /* dimensions of each image; the panel is the same size as the image */
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private static final int DELAY = 500;  // time (ms) between redraws of the panel

  private static final int CAMERA_ID = 0;

  private static final float SMALLEST_BOX =  1000.0f;
            // ignore detected contour boxes smaller than SMALLEST_BOX pixels

  private static final double BOX_FRAC =  0.8; 
            // for specifying the size of the largest possible contour box

  private static final double CROP_FRAC =  0.75; 
            // for cropping the top/bottom of the fingerprint image

  private static final double X_LEN =  323.0; 
           // x- length of final fingerprint (same as in the Biometric SDK)


  private IplImage snapIm = null;
  private volatile boolean isRunning;
  private volatile boolean isFinished;

  private Polygon gridPoly;     // holds the current fingerprint box outline polygon
  private boolean foundOutline = false;   // has an outline been found?

  private BufferedImage fpImage;  // the extracted fingerprint image
  private int fileCount = 0;

  private ImagePanel fpPanel;     // where the extracted fingerprint image is displayed
  private ImagePanel skelPanel;   // where the labelled fingerprint is displayed



  public ScanPanel(ImagePanel fpPanel, ImagePanel skelPanel)
  {
    this.fpPanel = fpPanel;
    this.skelPanel = skelPanel;

    setBackground(Color.white);

    gridPoly = new Polygon(); 

    new Thread(this).start();   // start updating the panel's image
  } // end of ScanPanel()



  public Dimension getPreferredSize()
  // make the panel wide enough for an image
  {   return new Dimension(WIDTH, HEIGHT); }



  public void run()
  /* display the current webcam image every DELAY ms.
     Fingerprint processing is triggered by GUI buttons, and so processed
     in the separate Java GUI thread.
  */
  {
    FrameGrabber grabber = initGrabber(CAMERA_ID);
    if (grabber == null)
      return;

    long duration;
    isRunning = true;
    isFinished = false;

    while (isRunning) {
	  long startTime = System.currentTimeMillis();

      snapIm = picGrab(grabber, CAMERA_ID); 
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



  public void paintComponent(Graphics g)
  // Draw the current image, and the detected fingerprint box outline 
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    if (snapIm == null) {
      g.setColor(Color.BLUE);
      g.drawString("Loading from camera " + CAMERA_ID + "...", 5, HEIGHT/2);
    }
    else
      g2.drawImage(snapIm.getBufferedImage(), 0, 0, this);

    if (foundOutline) {
      g2.setColor(Color.YELLOW);
      g2.setStroke(new BasicStroke(6));   // thick yellow lines
      synchronized(gridPoly) {
        g2.drawPolygon(gridPoly);
      }
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



  // ------------------------- fingerprint processing ----------------------------


  public void findPrint()
  /* Finds a fingerprint in the current webcam image, and its box outline
     will be drawn on to the webcam image. Lots of JavaCV processing of
     the webcam image is carried out here.
     The extracted image is drawn separately in the fpPanel ImagePanel.
  */
  {  
    fpImage = null;
    fpPanel.reset();
    foundOutline = false;     // not found an outline yet
    skelPanel.reset();

    if (snapIm == null)
      return;

    // convert to grayscale and equalize
    IplImage grayImg = IplImage.create(cvGetSize(snapIm), IPL_DEPTH_8U, 1);
    cvCvtColor(snapIm, grayImg, CV_BGR2GRAY);  
    cvEqualizeHist(grayImg, grayImg);

    // blur fingerprint into a black blob
    IplImage blobImg = IplImage.create(cvGetSize(snapIm), IPL_DEPTH_8U, 1);
    cvErode(grayImg, blobImg, null, 5);    // convert print into grayish blob

    // change blob to black using thresholding
    cvThreshold(blobImg, blobImg, 150, 255, CV_THRESH_BINARY);   // 80 -- 150; was 120
         // reducing the threshold value makes more things white in the resulting blob
                             // threshold value, max_value

    // sharpen the fingerprint (which also adds a lot of general noise to the image)
    IplImage threshImg = IplImage.create(cvGetSize(snapIm), IPL_DEPTH_8U, 1);
    cvAdaptiveThreshold(grayImg, threshImg, 255,
            CV_ADAPTIVE_THRESH_MEAN_C,     // CV_ADAPTIVE_THRESH_GAUSSIAN_C
            CV_THRESH_BINARY,
            5, 2);   // block size and offset

    /* remove the noise surrounding the fingerprint while using the black blob as a mask
       to protect the fingerprint itself */
    cvMax(threshImg, blobImg, threshImg);    // remove threshImg areas that are white in blobImg

    // carry out more noise reduction to improve the white insides of the fingerprint
    cvSmooth(threshImg, threshImg, CV_MEDIAN, 3);
    cvEqualizeHist(threshImg, threshImg);

    IplImage largeFPImg = null;

    /* find a contour box near the center of the image, which should be the outline
       of the fingerprint  */
    IplImage boxImg = IplImage.create(cvGetSize(snapIm), IPL_DEPTH_8U, 1);
    cvNot(blobImg, boxImg);   // so fingerprint is white on black background
    CvRect centerBox = boxNearCenter(boxImg);

    if (centerBox == null) {
      System.out.println("No center box found in blob image");
    }
    else {   
      // System.out.println("Box: " + centerBox);
      // calculate the bounded box around the selected contour
      int x = centerBox.x();
      int y = centerBox.y();
      int w = centerBox.width();
      int h = centerBox.height();
      // cvRectangle(origImg, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 3, CV_AA, 0);

      // store the box's outline in the polygon object for later drawing
      synchronized(gridPoly) {
        gridPoly.reset();  // add points in clockwise order
        gridPoly.addPoint(x, y);
        gridPoly.addPoint(x+w, y);
        gridPoly.addPoint(x+w, y+h);
        gridPoly.addPoint(x, y+h);
      }
      foundOutline = true;

      // crop top and bottom part of fingerprint from threshImg into fpImg
      int hFrac = (int)(h * CROP_FRAC);
      int yFrac = y + (h-hFrac)/2;
      IplImage fpImg = cvCreateImage(cvSize(w, hFrac), IPL_DEPTH_8U, 1);
      cvSetImageROI(threshImg, cvRect(x, yFrac, w, hFrac) );
      cvCopy(threshImg, fpImg);
      cvResetImageROI(threshImg);

      // scale the image so it's x- dimension == X_LEN
      double scale = X_LEN / fpImg.width();
      largeFPImg = cvCreateImage( cvSize((int)(fpImg.width()*scale), 
                                         (int)(fpImg.height()*scale)), IPL_DEPTH_8U, 1);
      if (scale > 1)   // enlarge
        cvResize(fpImg, largeFPImg, CV_INTER_CUBIC);   // or CV_INTER_LINEAR
      else  // shrink
        cvResize(fpImg, largeFPImg, CV_INTER_AREA);

      fpImage = largeFPImg.getBufferedImage();    // IplImage --> BufferedImage

      fpPanel.setImage(fpImage);   // display the fingerprint in another ImagePanel
    }

    // show the images (used for debugging of the OpenCV processing)
/*
    imageShow("original", origImg, 0);
    imageShow("gray", grayImg, 40);
    imageShow("thresh", threshImg, 80);
    imageShow("blob", blobImg, 120);
    imageShow("fingerprint", largeFPImg, 160);
*/
  }  // end of findPrint()


  private void imageShow(String title, IplImage img, int pos)
  // debug method for showing OpenCV image in its own window
  {
    CanvasFrame canvas = new CanvasFrame(title);
    canvas.setLocation(pos, pos);
    canvas.showImage(img);   
  }  // end of imageShow()



  private CvRect boxNearCenter(IplImage boxImg)
  /* return the bounded box of the contour nearest the image's center,
     whose box area is the biggest within the range SMALLEST_BOX -- maxBox
  */
  {
    int maxBox = (int)Math.round((boxImg.width() * boxImg.height())*BOX_FRAC);
          // this upper limit stops the entire image being selected as an outline
    // System.out.println("Max box size: " + maxBox);

    // generate all the contours in the image
    CvSeq contours = new CvSeq(null);
    CvMemStorage storage = CvMemStorage.create();
    cvFindContours(boxImg, storage, contours, Loader.sizeof(CvContour.class),
                                                CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

    // center of the image
    int xCenter = boxImg.width()/2;
    int yCenter = boxImg.height()/2;
    int minDist2 = xCenter*xCenter + yCenter*yCenter;    // squared distance from center
    CvRect centerBox = null;

    /* find the convex box contour nearest the center, whose box area is the biggest
       within the range SMALLEST_BOX -- maxBox */
    while (contours != null && !contours.isNull()) {
      if (contours.elem_size() > 0) {
        CvSeq quad = cvApproxPoly(contours, Loader.sizeof(CvContour.class), storage, 
                      CV_POLY_APPROX_DP, cvContourPerimeter(contours)*0.02, 0);
        CvSeq convexHull = cvConvexHull2(quad, storage, CV_CLOCKWISE, 1);
        //System.out.println("Found hull");
        if (convexHull != null) {
          CvRect boundBox = cvBoundingRect(convexHull, 0);
          int area = boundBox.width()*boundBox.height();
          if ((area > SMALLEST_BOX) && (area < maxBox)) {
            int dist2 = distApart2(xCenter, yCenter, boundBox);
            if (minDist2 > dist2) {   // nearer center than the previous best match?
              minDist2 = dist2;
              centerBox = boundBox;
              // System.out.println("New nearest box: " + centerBox);
            }
          }
        }
      }
      contours = contours.h_next();
    }
    return centerBox;
  }  // end of boxNearCenter()



  private int distApart2(int xc, int yc, CvRect box)
  // squared distance between (xc,yc) and and the center of the box
  {  
    int xBox = box.x() + box.width()/2;
    int yBox = box.y() + box.height()/2;
    return ((xc - xBox)*(xc -xBox) + (yc - yBox)*(yc - yBox));  
  }  // end of distApart2()





  // ----------------------------- build fingerprint -------------------------

  public void analyzePrint()
  /* Save the fingerprint image, and use it to create a FingerPrint object
     which contains template and labelled image info. 
     Display the labelled fingerprint image in the skelPanel ImagePanel
  */
  {
    if (fpImage == null)
      return;

    // get the user to select a PNG filename for the fingerprint image
    String printName = "finger" + fileCount + "???";
    fileCount++;
    JFileChooser jfc = new JFileChooser(FingerUtils.PRINT_DIR);
    jfc.setAcceptAllFileFilterUsed(false);
    jfc.addChoosableFileFilter(new ExtFilter("png"));
    jfc.setSelectedFile(new File(printName + ".png"));

    int userSelection = jfc.showSaveDialog(this);
    if (userSelection == JFileChooser.CANCEL_OPTION)
      return;
     
    // extract print name from the selected filename
    if (userSelection == JFileChooser.APPROVE_OPTION) {
      String fnm = jfc.getSelectedFile().getName();   
      printName  = FingerUtils.extractPrintName(fnm);
    }

    if (printName == null)
      return;

    /* create a FingerPrint object which contains template and
       labelled image info*/
    FingerPrint fp = new FingerPrint(printName, fpImage);

    // display the labelled fingerprint image in the skelPanel ImagePanel
    BufferedImage labelledImage = fp.getLabelledImage();
    if (labelledImage != null)
      skelPanel.setImage(labelledImage);
  } // end of analyzePrint()


} // end of ScanPanel class

