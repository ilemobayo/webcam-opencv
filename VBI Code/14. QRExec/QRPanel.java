
// QRPanel.java
// Andrew Davison, July 2013, ad@fivedots.psu.ac.th

/* This panel repeatedly snaps a picture and draw it onto
   itself. 

   When the tryDecoding boolean is set to true (by the user pressing
   a "Decode" button in the top-level JFrame), the current image is examined 
   for a QRCode. A success or fail sound is played depending on if a QRCode
   is found. 

   If the decoding is successful then the message is written into a 
   textfield in the top-level JFrame and a polygon is drawn around the coordinates 
   of the QRCode in the image.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.awt.geom.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.videoInputLib.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.avutil.*;   // for grabber/recorder constants

import com.google.zxing.*;   // for the QRCode Result object


class QRPanel extends JPanel implements Runnable
{
  /* dimensions of each image; the panel is the same size as the image */
  private static final int WIDTH = 640;  
  private static final int HEIGHT = 480;

  private static final int DELAY = 100;  // ms

  private static final int CAMERA_ID = 0;

  private static final int MAX_SHOW = 10;   // number of renders to show QRCode polygon

  private QRExec top;
  private BufferedImage snapIm = null;
              // type changed from IplImage
  private volatile boolean isRunning;
  private volatile boolean isFinished;
  
  // used for the average ms snap time information
  private int imageCount = 0;
  private long totalTime = 0;
  private Font msgFont;

  private volatile boolean tryDecoding = false;
  private ResultSounder sounder;   // for playing success/fail clips

  // for drawing a polygon around the QRCode in the image
  private GeneralPath qrPolygon;
  private boolean showPolygon = false;
  private int polyCounter = 0;   
      // used to keep the polygon rendering over multiple frame iterations



  public QRPanel(QRExec top)
  {
    this.top = top;
    setBackground(Color.white);

    msgFont = new Font("SansSerif", Font.BOLD, 18);
    sounder = new ResultSounder();

    qrPolygon =  new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);   
               // for holding the polygon coords (assuming it's a quadrilateral)

    new Thread(this).start();   // start updating the panel's image
  } // end of QRPanel()


  public Dimension getPreferredSize()
  // make the panel wide enough for an image
  {   return new Dimension(WIDTH, HEIGHT); }


  public void startDecoder()
  // called from GUI in QRExec
  {  tryDecoding = true;  }


  public void run()
  /* take a picture every DELAY ms, and perhaps decode the QRCode
     in the image, if tryDecoding has been set to true
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

      snapIm = (picGrab(grabber, CAMERA_ID)).getBufferedImage(); 

      if (tryDecoding) {   // try decoding the QRCode in the image
        decodeImage(snapIm);
        tryDecoding = false;
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




  private void decodeImage(BufferedImage im)
  /* attempt to decode a QR Code inside the image. The decoded
     QR Code information is stored in a Result object (res).
  */
  {
    Result res = QRCodex.decode(im);
    if (res != null) {   // found a QRC ode in the image
      // QRCodex.examineResult(res);
      String msg = res.getText();
      System.out.println("Message: " + msg);
      sounder.playSuccess();
      storePolygonCoords(res);
      top.showMessage(msg);
    }
    else {  // no QR Code found
      sounder.playFailure();
      top.showMessage("??");
    }
  }  // end of decodeImage()


  private void storePolygonCoords(Result res)
  /* convert the result points into a drawable polygon, which will
     be drawn over the image at rendering time */
  {
    ResultPoint[] resultPts = res.getResultPoints(); 

    qrPolygon.reset();
    qrPolygon.moveTo( resultPts[0].getX(), resultPts[0].getY() );
    for(int i=1; i < resultPts.length; i++)
      qrPolygon.lineTo(resultPts[i].getX(), resultPts[i].getY());
    qrPolygon.closePath();

    showPolygon = true;
  }  // end of storePolygonCoords()



  public void paintComponent(Graphics g)
  /* Draw the image, a QRCode result polygon, and the 
     average ms snap time at the bottom left of the panel. 
  */
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    if (snapIm != null)
      g2.drawImage(snapIm, 0, 0, this);

    drawQRPolygon(g2);
    writeStats(g2);
  } // end of paintComponent()


  private void drawQRPolygon(Graphics2D g2)
  /* Draw a yellow polygon which corresponds to the corners
     of the detected QRCode.
     Draw the polygon for MAX_SHOW rendering, so the user has
     time to see it.
  */
  {
    // perhaps stop showing the results polygon
    if (polyCounter == MAX_SHOW) {
      showPolygon = false;
      polyCounter = 0;
    }
    // perhaps draw the results polygon
    if (showPolygon) {
      g2.setColor(Color.YELLOW);
      g2.setStroke(new BasicStroke(6));    // thick yellow lines used
      g2.draw(qrPolygon);
      polyCounter++;
    }
  }  // end of drawQRPolygon()

  

  private void writeStats(Graphics2D g2)
  /* write statistics in bottom-left corner, or
     "Loading" at start time */
  {
    g2.setFont(msgFont);

    if (snapIm != null) {
      g2.setColor(Color.YELLOW);
      String statsMsg = String.format("Snap Avg. Time:  %.1f ms",
                                        ((double) totalTime / imageCount));
      g2.drawString(statsMsg, 5, HEIGHT-10);  
                        // write statistics in bottom-left corner
    }
    else  {  // no image yet
      g2.setColor(Color.BLUE);
      g2.drawString("Loading from camera " + CAMERA_ID + "...", 5, HEIGHT-10);
    }
  }  // end of writeStats()



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


} // end of QRPanel class

