
// DepthViewer.java
// Andrew Davison, June 2013, ad@fivedots.coe.psu.ac.th

/* Calibrate cameras using a series of stereo image pairs 
   and/or calculate grayscale disparity image and point cloud information for test image pairs.

   Slide values allow the grayscale disparity image attributes to be changed,
   and then the map is recalculated and redrawn.

   Clicking on the map causes the depth at that position to be shown.

   Usage:
      > run DepthViewer -n <number>   [ draw ]
          - runs DepthViewer in calibration mode
          - there must be <number> image pairs in the stereoData/ subdirectory,
            which are PNG files with the names "left" and "right" and a two-digit
            ID number. The numbers must start at 0 and run up to <number-1> with
            no gaps

          * the optional "draw" flag switches on the drawing of the chessboard pattern
            as it is detected inside a calibration image

      > run DepthViewer -p <number>
         - runs DepthViewer in depth processing mode
         - there must be an image pair in stereoData/ with the specified number ID
         - there must be saved calibration matricies in stereoData/:
              - 4 lookup matricies: mx1.txt, my1.txt, mx2.txt, my2.txt
              - the reprojection matrix: q.txt
*/

import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

import com.googlecode.javacv.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;



public class DepthViewer extends JFrame implements SliderBoxWatcher
{

  private ImagePanel imPanel;   // where the grayscale disparity image is drawn
  private DepthCalc depthCalc;


  public DepthViewer(int val, boolean isCalibrating, boolean drawChessboards)
  {
    super("Depth Viewer");

    Container c = getContentPane();
    c.setLayout( new BorderLayout());

    /* Hardwired initial attributes for the fast block-matching (BM) 
       stereo algorithm implemented by cvFindStereoCorrespondenceBM(); 
       see the OpenCV book p.444.
       These values are based on those used in the p.451 example code.
    */
    int preFilterSize = 31;  // 41;
    int prefilterCap = 31;
    int sadSize = 15; // 41;

    int minDisp = -100;  // -64;
    int numDisp = 128;

    int uniqRatio = 15;
    int texThresh = 10;
    int specSize = 100;
    int specRange = 4;

    depthCalc = new DepthCalc(val, isCalibrating, drawChessboards,
                            preFilterSize, prefilterCap, sadSize,
                            minDisp, numDisp, uniqRatio,
                            texThresh, specSize, specRange);

    JPanel ctrlPanel = new JPanel();
    ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.Y_AXIS));

    // sliders to adjust BM attribute values
    ctrlPanel.add(new SliderBox("Prefilter Size", 5, 255, preFilterSize, this));
    ctrlPanel.add(new SliderBox("Prefilter Cap", 1, 63, prefilterCap, this));

    ctrlPanel.add(new SliderBox("SAD Window Size", 5, 255, sadSize, this));
    ctrlPanel.add(new SliderBox("Min Disparity", -128, 128, minDisp, this));
    ctrlPanel.add(new SliderBox("No. of Disparities", 32, 192, numDisp, this));

    ctrlPanel.add(new SliderBox("Uniq Ratio", 0, 100, uniqRatio, this));
    ctrlPanel.add(new SliderBox("Texture Thresh", 0, 100, texThresh, this));

    ctrlPanel.add(new SliderBox("Speckle Size", 0, 1000, specSize, this));
    ctrlPanel.add(new SliderBox("Speckle Range", 0, 31, specRange, this));
    c.add(ctrlPanel, BorderLayout.WEST);

    // display panel for grayscale disparity image
    imPanel = new ImagePanel(depthCalc);
    imPanel.setImage( depthCalc.getGDispMap() );
    c.add(imPanel, BorderLayout.CENTER);


    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { try {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          depthCalc.storeDepthInfo();
             /* stores the slider settings, the grayscale disparity image, a
                PLY point cloud file representing the disparity map
             */
        }
        catch(Exception ie) {}
        finally {
          setCursor(Cursor.getDefaultCursor());
        }
        System.exit(0);
      }
    });

    pack();
    setResizable(false);
    setVisible(true);
  }  // end of DepthViewer()



  public void valChange(String title, int val)
  // callback from SliderBox class
  /* respond to slider changes by recalculating the grayscale disparity image
     for the test image pair, and displaying it again  */
  {
    // System.out.println(title + ": " + val);
    try {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      imPanel.setImage( depthCalc.changeBM(title, val) );
    }
    finally {
      setCursor(Cursor.getDefaultCursor());
    }
  }  // end of valChange()


  // -----------------------------------------------------

  public static void main(String[] args)
  {
    if ((args.length < 2) || (args.length > 3)) {
      System.out.println("Usage: run DepthViewer -n <no of image pairs; e.g. 14>  [draw] ");
      System.out.println("   or  run DepthViewer -p <image pair ID; e.g. 0> [draw]");
      return;
    }

    boolean drawChessboards = false;
    if (args.length == 3) {
      if (args[2].equals("draw"))
        drawChessboards = true;
    }

    int val = 0;
    try {
      val = Integer.parseInt(args[1]);
    }
    catch(NumberFormatException e)
    { System.out.println("Cannot parse numeric 2nd argument");
      return;
    }

    if (args[0].equals("-n")) {
      System.out.println("Calibrating using " + val + " image pairs");
      new DepthViewer(val, true, drawChessboards);
    }
    else if (args[0].equals("-p")) {
      System.out.println("Calculating disparities for the image pair with ID " + val);
      new DepthViewer(val, false, drawChessboards);
    }
    else
      System.out.println("Did not recognise option string");
  }  // end of main()


}  // end of DepthViewer class
