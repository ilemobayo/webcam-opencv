
// DepthCalc.java
// Andrew Davison, June 2013, ad@fivedots.coe.psu.ac.th

/* DepthCalc can be used in two ways:
      * either to read in a series of image pairs, and calculate the
        calibration details for the two cameras. Lookup maps and
        the reprojection matrix Q are saved, and a test image pair (taken
        from the series) is undistorted, rectified and displayed. Also
        a normalized disparity map for the image pair is calculated, and 
        a grayscale disparity image is shown in a panel in the
        top-level application. An anaglyph of the rectified images
        is also shown and saved. At termination time the grayscale image is saved
        to a JPG file along with a point cloud representation in a PLY file
        http://en.wikipedia.org/wiki/PLY_(file_format).

      * a single image pair can be loaded, together with pre-existing 
        lookup maps and a reprojection matrix Q (read from the stereoData/
        directory), and the pair is processed as above.

   The panel displaying the gray disparity image can be clicked upon, causing
   the depth to be displayed that corresponds to that (x, y) coordinate according
   to the normalized disparity map.

   The disparity map for the image pair is calculated using the 
   fast block-matching (BM) stereo algorithm implemented by 
   cvFindStereoCorrespondenceBM(); see OpenCV book p.444.

   The BM attributes can be adjusted via sliders in the top-level
   DepthViewer class, and any changes trigger a recalculation of the
   disparity map, and the displayed gray disparity image.

   The current BM settings are saved to a text file just before the 
   application terminates.
                           ----------------
   For more information on the maths involved, please read 
   "Learning OpenCV: Computer Vision
   with the OpenCV Library" by Gary Bradski and Adrian Kaehler, O'Reilly,
   chapter 13, 2008 (1st ed.)
   In the comments, I refer to relevant pages in the Bradski and Kaehler book
   using the word "OpenCV" and the page numbers.


   Documentation of the relevant OpenCV 3D functions can be found at:
     http://opencv.willowgarage.com/documentation/camera_calibration_and_3d_reconstruction.html
*/


import java.io.*;
import java.util.*;
import java.nio.*;
import java.awt.image.*;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import static com.googlecode.javacv.cpp.opencv_legacy.*;


public class DepthCalc
{
  private static final int LEFT = 0;
  private static final int RIGHT = 1;

  // camera image dimensions
  private static final int IM_WIDTH = 640;
  private static final int IM_HEIGHT = 480;

  // the number of INTERIOR corners in the board along its rows and columns
  private static final int CORNERS_COLS = 9;   // see Fig 11-10, OpenCV p.384
  private static final int CORNERS_ROWS = 6;
      /* check that the chessboard shown in the image pairs does have
         this number of interior corners; **CHANGE** if necessary */

  private static final int NUM_CORNS = CORNERS_COLS * CORNERS_ROWS;
  private static final CvSize BOARD_SZ = cvSize(CORNERS_COLS, CORNERS_ROWS);

  private static final int CHESS_SQ_LENGTH = 27;  // mm
       // **CHANGE** this to match the size of a square in your chessboard print-out

  private static final String STEREO_DIR = "stereoData/";

  // names of files used to store info at application termination time
  private static final String BS_FNM = "bsState.txt";   // the block-matching (BM) stereo state
  private static final String VDISP_MAP_FNM = "gDispMap.jpg";    // grayscale gray disparity image image
  private static final String PCL_FNM = "pointCloud.ply";        // points cloud
  private static final String ANA_FNM = "anaglyph.jpg";          // anaglyph image


  private int totPoints;                 // the number of image pairs * NUM_CORNS

  private CvMat mx1, my1, mx2, my2;      // lookup maps for the left and right cameras
  private CvMat Q;
  private double focalLength, baselineDist;      // parts of reprojection matrix, Q

  private IplImage[] imagesRectified = null;     // rectified image pair being depth processed
  private CvStereoBMState bmState;       // the block-matching (BM) stereo state

  private IplImage gDispMap;             // grayscale disparity image
  private CvMat normalizedDisp;          // disparity data used for depth calculations
  // private CvMat xyzMat;

  private CanvasFrame displayFrame;   
            // used to display chessboard points and rectified images

  private boolean drawChessboards;



  public DepthCalc(int val, boolean isCalibrating, boolean isDrawing,
                              int preFilterSize, int prefilterCap, int sadSize,
                              int minDisp, int numDisp, int uniqRatio,
                              int texThresh, int specSize, int specRange)
  {
    drawChessboards = isDrawing;
       // if true, then the identified chessboards are drawn at calibration time

    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);

    // initialize the block-matching (BM) stereo state; see OpenCV book p.443-444
    bmState = cvCreateStereoBMState(CV_STEREO_BM_BASIC, 0);
    bmState.preFilterSize(preFilterSize);   // prefilters
    bmState.preFilterCap(prefilterCap);

    bmState.SADWindowSize(sadSize);         // SAD-related

    bmState.minDisparity(minDisp);
    bmState.numberOfDisparities(numDisp);   // postfilters

    bmState.textureThreshold(texThresh);
    bmState.uniquenessRatio(uniqRatio);
    bmState.speckleWindowSize(specSize);
    bmState.speckleRange(specRange);

    // decide whether calibrating or processing a given image pair
    if (isCalibrating) {
      calibrateCams(val);
      depthProcessing(0);
    }
    else {   // processing the image pair with ID == val
      loadCalibrationInfo();
      depthProcessing(val);
    }
  }  // end of DepthCalc()



  // ---------------------- calibration phase -----------------


  private void calibrateCams(int maxPairs)
  // use pairs of images from two cameras to calibrate the cameras
  {
    // allocate matrix storage, and access to it via buffers
    // these 4 matricies are used by cvStereoCalibrate()
 
    totPoints = maxPairs * NUM_CORNS;

    CvMat objPts = CvMat.create(1, totPoints, CV_32F, 3);
    FloatBuffer objPtsBuf = objPts.getFloatBuffer();
        // for corner coords starting from (0,0) for each image in 1 big list

    CvMat nPts = CvMat.create(1, maxPairs, CV_32S, 1);
    IntBuffer nPtsBuf = nPts.getIntBuffer();
                    // for the number of corners in each image

    CvMat imPts1 = CvMat.create(1, totPoints, CV_32F, 2);
    FloatBuffer imPts1Buf  = imPts1.getFloatBuffer();

    CvMat imPts2 = CvMat.create(1, totPoints, CV_32F, 2);
    FloatBuffer imPts2Buf  = imPts2.getFloatBuffer();
        // for the pixel coordinates of corners in each image (left == 1, right == 2);
        // in same order of corners as objPts

    loadPairs(objPtsBuf, nPtsBuf, imPts1Buf, imPts2Buf, maxPairs);
    calibrateWithPairs(objPts, nPts, imPts1, imPts2);
  }  // end of calibrateCams()



  private void loadPairs(FloatBuffer objPtsBuf, IntBuffer nPtsBuf,
                         FloatBuffer imPts1Buf, FloatBuffer imPts2Buf, int maxPairs)
  /*  find the chessboard *interior* corner points in all the image pairs; 
      if there are any problems during the processing, abort at the end 
  */
  {
    IplImage[] images = new IplImage[2];   // for the left and right camera images
    CvPoint2D32f[] cornsPair =  new CvPoint2D32f[2];    
                          // for the corner pts found in the left and right images

    // read in the image pairs, finding the chessboards corners in each
    int numPairs =  0;
    for(int i=0; i < maxPairs; i++) {
      System.out.println("Loading left & right images with ID " + i);
      images[LEFT] = cvLoadImage( makeName("left", i), 0);
      images[RIGHT] = cvLoadImage(makeName("right", i), 0);

      if ((images[LEFT] == null) || (images[RIGHT] == null))
        System.out.println("  One of the images is null; not adding pair");
      else if (!isRightSize(images[LEFT]) || !isRightSize(images[LEFT]))
        System.out.println("  One of the images is the wrong size; not adding pair");
      else {
        cornsPair[LEFT] = findCorners("left" + i, images[LEFT]);
        cornsPair[RIGHT] = findCorners("right" + i, images[RIGHT]);

        if ((cornsPair[LEFT] != null) && (cornsPair[RIGHT] != null)) {
          // System.out.println("Adding pair to data");
          addCornersPair(cornsPair, objPtsBuf, nPtsBuf, imPts1Buf, imPts2Buf);
          numPairs++;
        }
      }
    }

    System.out.println("No. of valid image pairs: " + numPairs);
    if (numPairs < maxPairs) {    // give up if there were any errors during the loading
      System.out.println("Please fix " + (maxPairs - numPairs) + " invalid pairs");
      System.exit(1);
    }
  }  // end of loadPairs()



  private String makeName(String fnm, int i)
  {
    return ((i < 10) ?  STEREO_DIR + fnm + "0" + i +".jpg" :
                        STEREO_DIR + fnm + i +".jpg");
  }  // end of makeName()



  private boolean isRightSize(IplImage im)
  { return ((im.width() == IM_WIDTH) &&  (im.height() == IM_HEIGHT));  }



  private CvPoint2D32f findCorners(String fnm, IplImage im)
  // find the chessboard corner points in the image;
  // optionally draw each detected chessboard
  {
    int[] cornerCount = new int[1];
    cornerCount[0] = 0;
    CvPoint2D32f corners = new CvPoint2D32f(NUM_CORNS);

    // find the chessboards and its corners points
    int result = cvFindChessboardCorners(im, BOARD_SZ, corners, cornerCount,
                      CV_CALIB_CB_ADAPTIVE_THRESH | CV_CALIB_CB_NORMALIZE_IMAGE);
    // System.out.printf("%s: find() result = %s, corner count = %s\n",
    //                                              fnm, result, cornerCount[0]);

    if (result != 1) {
      System.out.println("Could not find a chessboard image in " + fnm);
      return null;
    }

    if (cornerCount[0] != NUM_CORNS) {
      System.out.println("The chessboard image in " +
                                fnm + " has the wrong number of corners");
      return null;
    }

    // improve corner locations by using subpixel interpolation
    cvFindCornerSubPix(im, corners, cornerCount[0],
                          cvSize(11, 11), cvSize(-1,-1),
                          cvTermCriteria(CV_TERMCRIT_ITER + CV_TERMCRIT_EPS, 30, 0.01));

    if (drawChessboards) {
      // draw chessboard with found corners (e.g. as in fig 11-10, OpenCV p.384)
      IplImage colImg = cvCreateImage(cvGetSize(im), 8, 3);
      cvCvtColor(im, colImg, CV_GRAY2BGR);    // so chessboard points/lines will be in color
      cvDrawChessboardCorners(colImg, BOARD_SZ, corners, cornerCount[0], result);
      
      if (displayFrame == null)
        displayFrame = new CanvasFrame("Chessboard " + fnm);
      else
        displayFrame.setTitle("Chessboard " + fnm);
      displayFrame.showImage(colImg);
      enterPause();
    }

    return corners;
  }  // end of findCorners()



  private void enterPause()
  // Pause until user presses <enter>
  { while (true) {
      String cmdStr = System.console().readLine();
      if (cmdStr.startsWith(""))
        return;
    }
  }  // end of enterPause()



  private void addCornersPair(CvPoint2D32f[] cornsPair,
                           FloatBuffer objPtsBuf, IntBuffer nPtsBuf,
                           FloatBuffer imPts1Buf, FloatBuffer imPts2Buf )
  // if we get a good pair of corners, add them to the image and object matricies
  {
    for (int i = 0; i < NUM_CORNS; i++) {
      imPts1Buf.put((float)cornsPair[LEFT].position(i).x());   // left image's chessboard corners
      imPts1Buf.put((float)cornsPair[LEFT].position(i).y());

      imPts2Buf.put((float)cornsPair[RIGHT].position(i).x());  // right image
      imPts2Buf.put((float)cornsPair[RIGHT].position(i).y());

      // order of object matrix is by row, starting at the top-left corner
      objPtsBuf.put((float)(i / CORNERS_COLS));  // row
      objPtsBuf.put((float)(i % CORNERS_COLS));  // col
      objPtsBuf.put(0);  // z (not used)
    }

    nPtsBuf.put(NUM_CORNS);    // record number of corners in this image pair
  }  // end of addCornersPair()



  private void calibrateWithPairs(CvMat objPts, CvMat nPts, 
                                  CvMat imPts1, CvMat imPts2)
  /*  Use cvStereoCalibrate() to generate calibration information
      for the two cameras 
  */
  {
    System.out.println("\nStarting calibration of the two cameras ...");
    long startTime = System.currentTimeMillis();

    // camera intrinsics matricies
    CvMat M1 = cvCreateMat(3, 3, CV_64F);    // for the left camera image
    cvSetIdentity(M1);
    CvMat M2 = cvCreateMat(3, 3, CV_64F);    // right camera
    cvSetIdentity(M2);

    // the distortion coefficients matricies
    CvMat D1 = cvCreateMat(1, 5, CV_64F);
    cvZero(D1);
    CvMat D2 = cvCreateMat(1, 5, CV_64F);
    cvZero(D2);

    CvMat R = cvCreateMat(3, 3, CV_64F);   // the rotation matrix
    CvMat T = cvCreateMat(3, 1, CV_64F);   // the translation vector
          /* these specify the position and orientation of the 
             second (right) camera relative to the first (left) camera */

    CvMat E = cvCreateMat(3, 3, CV_64F);   // the 'essential' matrix
       /* E contains info about the translation and rotation
          that relate the two cameras in physical space  */

    CvMat F = cvCreateMat(3, 3, CV_64F);   // the fundamental matrix
       /* F contains the same info as E, *and* info about the
          intrinsics of both cameras. F operates in image pixel coords 
          while E operates in physical coords  */

    CvSize imSize = cvSize(IM_HEIGHT, IM_WIDTH);

    /* calibrate the two cameras  (OpenCV p.428-429)
       by projecting the 3D chessboard points into the images, and 
       compares the position of the corners to get a measure of the 
       calibration quality.  
    */
    double rms = cvStereoCalibrate(objPts, imPts1, imPts2, nPts,
                             M1, D1, M2, D2,
                             imSize, R, T, E, F,
              cvTermCriteria(CV_TERMCRIT_ITER + CV_TERMCRIT_EPS, 100, 1e-5),
                             CV_CALIB_FIX_ASPECT_RATIO); 

    System.out.println("Calibration took " + 
                            (System.currentTimeMillis() - startTime) + " ms"); 

    System.out.printf( "  ***RMS reprojection error: %.3f\n", rms);
       /* the root mean square (RMS) reprojection error is an indicator of 
          how well the calibration parameters fit the data, and should be between 
          0.1 and 1.0 pixels in a good calibration. Nearer 0.1 is better. */

    printMatrix("M1", M1);
    printMatrix("M2", M2);
    printMatrix("D1", D1);
    printMatrix("D2", D2);
    printMatrix("Rotation Matrix R:", R);
    printMatrix("Translation Matrix T:", T);

    System.out.println("Undistorting image points...");
    cvUndistortPoints( imPts1, imPts1, M1, D1, null, M1);
    cvUndistortPoints( imPts2, imPts2, M2, D2, null, M2);
                        // intrinisic matricies also indistorted

    // check calibration quality
    showEpipolarError(imPts1, imPts2, F);

    // rectification using Hartley's method
    System.out.println("Calculating homography matricies...");
    CvMat H1 = cvCreateMat(3, 3, CV_64F);
    CvMat H2 = cvCreateMat(3, 3, CV_64F);
    cvStereoRectifyUncalibrated( imPts1, imPts2, F, imSize, H1, H2, 3);
    calculateLookupMaps(M1, M2, D1, D2, H1, H2);

    calculateQ(M1, M2, D1, D2, imSize, R, T);
  }  // end of calibrateWithPairs()



  private void showEpipolarError(CvMat imPts1, CvMat imPts2, CvMat F)
  /* check the quality of calibration using the
     epipolar geometry constraint
  */
  {
    CvMat L1 = CvMat.create(1, totPoints, CV_32F, 3);
    CvMat L2 = CvMat.create(1, totPoints, CV_32F, 3);
      /* Each output epipolar line is a Nx3 array. Each line a*x + b*y + c = 0
         is encoded by 3 numbers (a, b, c)
      */
    cvComputeCorrespondEpilines(imPts1, 1, F, L1);
    cvComputeCorrespondEpilines(imPts2, 2, F, L2);

    double avgErr = 0;
    for(int i = 0; i < totPoints; i++) {
        // calculate a*x + b*y + c for every image point (x, y), which should == 0
        double err = Math.abs((imPts1.get(0,i,0) * L2.get(0,i,0)) +
                              (imPts1.get(0,i,1) * L2.get(0,i,1)) + L2.get(0,i,2)) +
                     Math.abs((imPts2.get(0,i,0) * L1.get(0,i,0)) +
                              (imPts2.get(0,i,1) * L1.get(0,i,1)) + L1.get(0,i,2));
        avgErr += err;
    }
    System.out.printf("  ***Calibration average error: %.4f\n", avgErr/totPoints);    // 0 is best
  }  // end of showEpipolarError()




  private void calculateLookupMaps(CvMat M1, CvMat M2, CvMat D1, CvMat D2, CvMat H1, CvMat H2)
  {
    /* convert the 'uncalibrated' homography matricies (H1, H2) obtained
       from cvStereoRectifyUncalibrated() into rectification matricies Re1, Re2
       using Re =  iM*H*M  (M is the intrinsic matrix for a camera)
    */
    System.out.println("Calculating rectification matricies...");
    CvMat Re1 = cvCreateMat(3, 3, CV_64F);
    CvMat Re2 = cvCreateMat(3, 3, CV_64F);
    CvMat iM = cvCreateMat(3, 3, CV_64F);
    cvInvert(M1, iM);
    cvMatMul(H1, M1, Re1);
    cvMatMul(iM, Re1, Re1);   // Re1 =  iM1 * H1 * M1
    cvInvert(M2, iM);
    cvMatMul(H2, M2, Re2);
    cvMatMul(iM, Re2, Re2);   // Re2 =  iM2 * H2 * M2

    System.out.println("Calculating undistortion/rectification lookup maps...");
    /* calculates undistortion+rectification transformation that are
       stored as 'loopup maps' for the  left and right cameras; OpenCV p.437  
    */
    mx1 = cvCreateMat( IM_HEIGHT, IM_WIDTH, CV_32F);
    my1 = cvCreateMat( IM_HEIGHT, IM_WIDTH, CV_32F);
    mx2 = cvCreateMat( IM_HEIGHT, IM_WIDTH, CV_32F);
    my2 = cvCreateMat( IM_HEIGHT, IM_WIDTH, CV_32F);
    cvInitUndistortRectifyMap(M1, D1, Re1, M1, mx1, my1);   // left
    cvInitUndistortRectifyMap(M2, D2, Re2, M2, mx2, my2);   // right

    System.out.println("Saving maps");
    saveMatrix(STEREO_DIR + "mx1.txt", mx1);
    saveMatrix(STEREO_DIR + "my1.txt", my1);
    saveMatrix(STEREO_DIR + "mx2.txt", mx2);
    saveMatrix(STEREO_DIR + "my2.txt", my2);
  }  // end of calculateLookupMaps()




  private void calculateQ(CvMat M1, CvMat M2, CvMat D1, CvMat D2,
                          CvSize imSize, CvMat R, CvMat T)
  /* Repeat rectification, this time with Bouquet's method, in order to
     calculate the Q, Reb1, and Reb2 matricies. The main purpose here
     is to obtain Q's focal length and baseline distance which are
     used later to calculate depths.
  */
  {  
    // the rectification matrices using Bouquet
    CvMat Reb1 = cvCreateMat(3, 3, CV_64F);
    CvMat Reb2 = cvCreateMat(3, 3, CV_64F);

    // projection matrices in the rectified coordinate system
    CvMat P1 = cvCreateMat(3, 4, CV_64F);
    CvMat P2 = cvCreateMat(3, 4, CV_64F);

    // Q is the 4x4 reprojection matrix; OpenCV p.435
    Q = cvCreateMat(4, 4, CV_64F);
    cvStereoRectify(M1, M2, D1, D2, imSize, R, T, Reb1, Reb2, P1, P2,
                     Q, CV_CALIB_ZERO_DISPARITY, -1, cvSize(0,0), null, null);
    printMatrix("Reprojection Matrix Q:", Q);

    // save focal length and baseline distance in globals
    focalLength = Q.get(2,3);
    baselineDist = -1.0/Q.get(3,2);
    System.out.printf("Focal length: %.4f\n", focalLength);
    System.out.printf("Baseline distance: %.4f\n", baselineDist);
    System.out.println("Saving reprojection matrix");
    saveMatrix(STEREO_DIR + "q.txt", Q);

    /* If our cameras have roughly the same parameters and are set up in an 
       approximately horizontally aligned frontal parallel configuration, 
       then the Reb matricies will look very much like the rectified Re matricies */
    // test difference between the rectification approaches (should be all 0's)
/*
    CvMat diff = cvCreateMat(3, 3, CV_64F);
    cvSub(Reb1, Re1, diff, null);
    printMatrix("Rectification difference for camera 1:", diff);
    cvSub(Reb2, Re2, diff, null);
    printMatrix("Rectification difference for camera 2:", diff);
*/
  }  // end of calculateQ()



  // ----------------------- depth processing of a test image pair --------


  private void loadCalibrationInfo()
  {
    System.out.println("Loading undistortion/rectification lookup maps");
    mx1 = loadMatrix(STEREO_DIR + "mx1.txt", CV_32F);
    my1 = loadMatrix(STEREO_DIR + "my1.txt", CV_32F);
    mx2 = loadMatrix(STEREO_DIR + "mx2.txt", CV_32F);
    my2 = loadMatrix(STEREO_DIR + "my2.txt", CV_32F);

    System.out.println("Loading reprojection matrix");
    Q = loadMatrix(STEREO_DIR + "q.txt", CV_64F);    
                                  // reprojection matrix; see OpenCV p.435

    printMatrix("Reprojection Matrix Q:", Q);
    focalLength = Q.get(2,3); 
    baselineDist = -1.0/Q.get(3,2);
    System.out.printf("Focal length (pixels): %.4f\n", focalLength);
    System.out.printf("Baseline distance: %.4f\n", baselineDist);
  }  // end of loadCalibrationInfo()



  private void depthProcessing(int ID)
  /* load the test images, calculate their gray disparity image, and display the pair
     of rectified images used to calculate the disparity info, and their anaglyph 
     Depths can be accessed using the normalized disparity matrix.
  */
  {
    imagesRectified = rectify(ID);
    showRectifiedImages(imagesRectified);    
    showAnaglyph(imagesRectified);

    normalizedDisp = createDisparityMaps(imagesRectified);

/*
    System.out.println("Generating point cloud");
    xyzMat = cvCreateMat(IM_HEIGHT, IM_WIDTH, CV_32FC3);
    cvReprojectImageTo3D(normalizedDisp, xyzMat, Q, 0);
*/
/* The output of cvReprojectImageTo3D() is a point cloud with coordinates based
   on the calibrated centre line of the camera viewing axis. See OpenCV book, p.453.

   I decided to create the point cloud myself, in convertDisparities(), 
   using the gray disparity image and normalized disparity matrix created above.
*/
  }  // end of depthProcessing()



  private IplImage[] rectify(int ID)
  /* Load the images pair labelled with ID.
     Undistort and rectify the images using the lookup maps
     in mx1, my1, mx2, and my2. 
  */
  {
    System.out.println("\nDepth processing the image pair with ID " + ID + " ...");
    IplImage leftIm = cvLoadImage(makeName("left", ID), 0);
    IplImage rightIm = cvLoadImage(makeName("right", ID), 0);
    if ((leftIm == null) || (rightIm == null)) {
      System.out.println("Error loading image pair " + ID);
      System.exit(1);
    }

    // System.out.println("Undistorting and rectifying the images...");
    IplImage[] imagesRectified = new IplImage[2];
    imagesRectified[LEFT] = IplImage.create(cvGetSize(leftIm), IPL_DEPTH_8U, 1);
    imagesRectified[RIGHT] = IplImage.create(cvGetSize(rightIm), IPL_DEPTH_8U, 1);

    cvRemap(leftIm, imagesRectified[LEFT], mx1, my1,
                               CV_INTER_LINEAR | CV_WARP_FILL_OUTLIERS, CvScalar.ZERO);
    cvRemap(rightIm, imagesRectified[RIGHT], mx2, my2,
                               CV_INTER_LINEAR | CV_WARP_FILL_OUTLIERS, CvScalar.ZERO);

    return imagesRectified;
  }  // end of rectify()




  private void showRectifiedImages(IplImage[] imagesRectified)
  /* Draw the rectified images side-by-side as a single large image, 
     with colored lines drawn across them to show that the images are
     aligned. */
  {
    System.out.println("Stitching rectified images together");
    IplImage imPair = IplImage.create(IM_WIDTH*2, IM_HEIGHT, IPL_DEPTH_8U, 3);  
                       // a colour image big enough for both input images

    IplImage colTemp = IplImage.create(IM_WIDTH, IM_HEIGHT, IPL_DEPTH_8U, 3);
                                      // temporary colour image object
    // copy left image to left side
    cvCvtColor(imagesRectified[LEFT], colTemp, CV_GRAY2RGB);   // in color
    cvSetImageROI(imPair, cvRect(0, 0, IM_WIDTH, IM_HEIGHT));
    cvCopy(colTemp, imPair);

    // copy right image to right side
    cvCvtColor(imagesRectified[RIGHT], colTemp, CV_GRAY2RGB);  // in color
    cvSetImageROI(imPair, cvRect(IM_WIDTH, 0 , IM_WIDTH*2, IM_HEIGHT));
    cvCopy(colTemp, imPair);

    // reset large image's Region Of Interest
    cvResetImageROI(imPair);

    // draw yellow and blue lines across the combined image to check the rectification
    CvScalar color;
    int count = 0;
    for(int i = 0; i < IM_HEIGHT; i += 16) {
      color = (count%2 == 0) ? CvScalar.BLUE : CvScalar.YELLOW;
      cvLine( imPair, cvPoint(0,i), cvPoint(IM_WIDTH*2, i), color, 1, CV_AA, 0);
      count++;
    }

    // display the result
    if (displayFrame == null)
      displayFrame = new CanvasFrame("Rectified Pair");
    else {
      displayFrame.setTitle("Rectified Pair");
      displayFrame.setSize(IM_WIDTH*2, IM_HEIGHT);
    }

    displayFrame.showImage(imPair); 
  }  // end of showRectifiedImages()



  private void showAnaglyph(IplImage[] imagesRectified)
  /* Create an analglyph image from the two rectified images, display it
     in a frame, and save it to a file. */
  {
    // System.out.println("Creating anaglyph");
    IplImage anaImg = IplImage.create(IM_WIDTH, IM_HEIGHT, IPL_DEPTH_8U, 3);
                              // colour image object with BGR channel odering

    cvMerge(imagesRectified[RIGHT], imagesRectified[RIGHT], imagesRectified[LEFT], null, anaImg);
       // (blue, green) = cyan = right image;  red = left image
     
    // display the result
    CanvasFrame anaFrame = new CanvasFrame("Anaglyph");
    anaFrame.showImage(anaImg); 

    System.out.println("Saving anaglyph to " + ANA_FNM);
    cvSaveImage(ANA_FNM, anaImg);
  }  // end of showAnaglyph()



  private CvMat createDisparityMaps(IplImage[] imagesRectified)
  /* Create a disparity map for the two images
     using the fast block-matching stereo 
     algorithm implemented by cvFindStereoCorrespondenceBM(); OpenCV p.444

     The disparity map is converted into a grayscale disparity image which can
     be retrieved by the top-level and displayed.

     The disparity map is normalized, so all the disparity values are greater
     than 0, and divided by 16. 
  */
  {
    CvSize imSize = cvGetSize(imagesRectified[LEFT]);

    // System.out.println("Creating a disparity map ...");
    IplImage disparityMap = IplImage.create(imSize, IPL_DEPTH_16S, 1);
    cvFindStereoCorrespondenceBM(imagesRectified[LEFT], imagesRectified[RIGHT],
                                                       disparityMap, bmState);

    // for debugging 
    double[] minVal = new double[1];
    double[] maxVal = new double[1];
    cvMinMaxLoc(disparityMap, minVal, maxVal);
    System.out.println("  Disparity range: " + minVal[0] + " - " + maxVal[0]);

    // convert disparity map to a grayscale disparity image
    gDispMap = IplImage.create(imSize, IPL_DEPTH_8U, 1);
    cvNormalize(disparityMap, gDispMap, 0, 255, CV_MINMAX, null); 

    cvMinMaxLoc(gDispMap, minVal, maxVal);
    System.out.println("  Gray disparity image range: " + minVal[0] + " - " + maxVal[0]);

    // normalize the disparity map; this is what I use for depth calculations
    CvMat normalizedDisp = cvCreateMat(IM_HEIGHT, IM_WIDTH, CV_32F);
    cvConvertScale(disparityMap, normalizedDisp, 1.0/16, 0);

    cvMinMaxLoc(normalizedDisp, minVal, maxVal);
    System.out.println("  Normalized disparity map range: " + minVal[0] + " - " + maxVal[0]);

    if (minVal[0]*maxVal[0] < 0)   // i.e. there's a sign change -ve to +ve
      cvConvertScale(normalizedDisp, normalizedDisp, 1, -minVal[0]);    // move to be all positive

    cvMinMaxLoc(normalizedDisp, minVal, maxVal);
    System.out.println("  Translated norm disparity map range: " + minVal[0] + " - " + maxVal[0]);

    return normalizedDisp;
  }  // end of createDisparityMaps()



  public int getDepth(int x, int y)
  /* called from ImagePanel to get the depth at a given (x,y)
     coordinate. Use the normalized disparity matrix. */
  {
    if (normalizedDisp == null)
      return 0;

    int depth = disparity2Depth( normalizedDisp.get(y,x));
    System.out.println("Depth at (" + x + ", " + y + "): " + depth);
    return depth;
  }  // end of getDepth()



  private int disparity2Depth(double disp)
  /* Each depth is calculated using the equation Z = (f*T)/d
     to convert disparity values (d) into depths (Z); f is the focal length and 
     T the baseline distance.

     Each depth is converted to a positive physical space value in mm's
  */
  { if (disp == 0)    // an infinite depth
      return 0;
    else
      return -(int)Math.round((focalLength*baselineDist)/disp * CHESS_SQ_LENGTH);
  } 


  // ------------------------ called at termination -------------------

  public void storeDepthInfo()
  /* This method is called from DepthViewer at termination time to 
     store the current block-matching attributes, gray disparity image, and a 
     point cloud as a PLY file (see  http://en.wikipedia.org/wiki/PLY_(file_format))
  */
  {
    System.out.println();
    saveBMAttributes(BS_FNM);

    if (gDispMap == null)
      System.out.println("No gray disparity image to save");
    else {
      System.out.println("Saving gray disparity image to " + VDISP_MAP_FNM);
      cvSaveImage(VDISP_MAP_FNM, gDispMap);
      savePly(PCL_FNM, gDispMap.asCvMat());
    }
  }  // end of storeDepthInfo



  private void saveBMAttributes(String fnm)
  {
    System.out.println("Saving block-matching attributes to " + fnm);
    try {
      PrintWriter out = new PrintWriter(new FileWriter(fnm));

      out.println("Prefilter Size: " + bmState.preFilterSize());
      out.println("Prefilter Cap: " + bmState.preFilterCap());
      out.println("SAD Window Size: " + bmState.SADWindowSize());
      out.println("Min Disparity: " + bmState.preFilterSize());
      out.println("No. of Disparities: " + bmState.numberOfDisparities());
      out.println("Uniq Ratio: " + bmState.uniquenessRatio());
      out.println("Texture Thresh: " + bmState.textureThreshold());
      out.println("Speckle Size: " + bmState.speckleWindowSize());
      out.println("Speckle Range: " + bmState.speckleRange());

      out.flush();
      out.close();
    }
    catch(IOException ex) 
    {  System.out.println("Unable to save");  }
  }  // end of saveBMAttributes()




  private void savePly(String fnm, CvMat gDispMat)
  /* Store a point cloud as a PLY file.
     The point cloud depths are obtained from normalized disparity map, and the
     points colours from the grayscale disparity image.
  */
  {
    int rows = gDispMat.rows();
    int cols = gDispMat.cols();
    int totalVerts = rows*cols;
    System.out.println();

    double[][] pclCoords = new double[cols][rows];    // x then y
    int numZeros = convertDisparities(normalizedDisp, pclCoords, rows, cols);
    if (numZeros > 0) {
      double percentZeros = 100.0*((double)numZeros)/totalVerts;
      System.out.printf("No. of vertices with 0 depth: %d (%.1f%%)\n",
                                                        numZeros, percentZeros);
    }
    int dataVerts = totalVerts - numZeros;   // ignore points with 0 depth

    System.out.println("Saving point cloud coordinates to " + fnm);
    try {
      PrintWriter out = new PrintWriter(new FileWriter(fnm));

      out.println("ply");
      out.println("format ascii 1.0");
      out.println("comment Point Cloud output from DepthCalc");

      // x, y, z coordinate for a point and colors
      out.println("element vertex " + dataVerts);
      out.println("property double x");    // vertex coordinates
      out.println("property double y");
      out.println("property double z");
      out.println("property uchar red");   // vertex colors
      out.println("property uchar green");
      out.println("property uchar blue");
      out.println("end_header");

      for (int x=0; x < cols; x++)
        for (int y=0; y < rows; y++) {
          double d = (double)pclCoords[x][y];
          int gray = (int) gDispMat.get((rows-1-y),x);   // grayscale value
          if (d != 0)    // do not save depths == 0
            out.printf("%d  %d  %.3f  %d  %d  %d\n", x, y, d, gray, gray, gray);    
        }
      out.flush();
      out.close();
    }
    catch(IOException ex) 
    {  System.out.println("Unable to save");  }
  }  // end of savePly()



  private int convertDisparities(CvMat normalizedDisp,
                           double[][] pclCoords, int rows, int cols)
  /* Convert the normalized disparity map matrix into a point cloud 2D array
     of depths.
     The origin of the depth coords is altered to be at the bottom left, and
     the array is organized in column order (x then y). 

     The bottom left positioning of the origin is required by the PLY
     format used in MeshLab (http://meshlab.sourceforge.net/)

     To accurately scale the depths, they have to be sorted so that
     the 98th percentile can be assigned as the 'max depth' in the very skewed data
  */
  {
    System.out.println("Converting normalized disparity map into a point cloud");

    ArrayList<Integer> depths = new ArrayList<Integer>();

    // convert disparities to depths
    int numZeros = 0;
    int z;
    for (int i=0; i < rows; i++) {
      for (int j=0; j < cols; j++) {
        z = disparity2Depth( normalizedDisp.get(i,j));
        if (z == 0)
          numZeros++;
        else {
          pclCoords[j][rows-1-i] = z; 
                 // x-axis goes to the right, y-axis runs up screen
          depths.add(z);
        }
      }
    }

    // calculate a scale factor 
    Collections.sort(depths);
    int minDepth = depths.get(0);
    int maxDepth = findMaxDepth(depths);
    System.out.println("min - max depth: " + minDepth + " - " + maxDepth);

    double scaleFactor = ((double)cols)/(maxDepth - minDepth);   
    System.out.printf("Scale factor: %.3f\n", scaleFactor);

    // scale the depths
    double pz;
    for (int x=0; x < cols; x++) {
      for (int y=0; y < rows; y++) {
        pz = pclCoords[x][y];
        if (pz != 0) {
          if (pz > maxDepth) {    // ignore depths that are too big
            pclCoords[x][y] = 0;
            numZeros++;
          }
          else
            pclCoords[x][y] = -scaleFactor*(pz - (minDepth-10));   
                        // spread depths along -z axis, and scale
        }
      }
    }

    return numZeros;
  }  // end of convertDisparities()



  private int findMaxDepth(ArrayList<Integer> depths)
  /* The maximum depth is the value at the 98th percentile of
     the sorted depths. I use this since the data is skewed by
     one or two very large depths, so the mean is inaccurate.
  */
  {
    int dVal = getPercentile(depths, 0.98);
    // System.out.println("98% value: " + dVal);

    int maxMult =  10*depths.get(0);   // large multiple of the minimum depth
    int maxDepth = (maxMult < dVal) ? maxMult : dVal;   // use smaller of two
    // System.out.println("Max Depth: " + maxDepth);

    return maxDepth;
  }  // end of findMaxDepth()



  private int getPercentile(ArrayList<Integer> list, double percent)
  {
    if ((percent < 0) || (percent > 1.0)) {
      System.out.println("percentage should be between 0 and 1; using 0.5");
      percent = 0.5;
    }

    int pcPosn = (int)Math.round(list.size()-1 * percent);
    // System.out.println("percentage " + percent + " position = " + pcPosn);
    return list.get(pcPosn);
  }  // end of getPercentile()




  // ----------------------------- matrix IO ----------------------------


  private void printMatrix(String label, CvMat matrix)
  {
    int rows = matrix.rows();
    int cols = matrix.cols();
    System.out.println(label);
    StringBuilder matStr = new StringBuilder();
    for (int i=0; i < rows; i++) {
      matStr.append("| ");
      for (int j=0; j < cols; j++) {
        matStr.append( String.format("% 6.3f", matrix.get(i,j)));
        if (j < cols-1)
          matStr.append("  ");
      }
      matStr.append(" |\n");
    }
    System.out.println(matStr);
  }  // end of printMatrix()



  private void saveMatrix(String fnm, CvMat matrix)
  {
    int rows = matrix.rows();
    int cols = matrix.cols();
    System.out.println("Saving matrix to " + fnm);

    BufferedWriter out = null;
    try {
      out = new BufferedWriter(new FileWriter(fnm));
      out.write(rows + "  " + cols + "\n");
      StringBuilder matStr;
      for (int i=0; i < rows; i++) {
        matStr = new StringBuilder();
        for (int j=0; j < cols; j++)
          matStr.append( matrix.get(i,j) + "  ");
        matStr.append("\n");
        out.write( matStr.toString());
      }
      out.close();
    }
    catch (IOException e) 
    {  System.out.println("Could not write matrix to " + fnm); }
    finally {
      try {
        if (out != null)
          out.close();
      } 
      catch (IOException e) {}
    }
  }  // end of saveMatrix()




  private CvMat loadMatrix(String fnm, int type)
  {
    System.out.println("Reading matrix from " + fnm);
    CvMat mat = null;
    try {
      BufferedReader br = new BufferedReader(new FileReader(fnm));

      String line = br.readLine();      // 1st line holds rows cols
      float[] vals = getValues(line);
      if (vals.length != 2) {
        System.out.println("Could not read dimensions line"); 
        System.exit(1);
      }
      int rows = (int) Math.round(vals[0]);
      int cols = (int) Math.round(vals[1]);
      mat = cvCreateMat(rows, cols, type);

      int i = 0;
      while(((line = br.readLine()) != null) && (i < rows)) {
        vals = getValues(line);
        if (vals.length != cols) {
          System.out.println("Wrong number of values in row"); 
          System.exit(1);
        }
        for (int j=0; j < cols; j++)
          mat.put(i, j, vals[j]);
        i++;
      }
      br.close();
    }
    catch (IOException e) 
    {  System.out.println("Could not read matrix from " + fnm); 
       System.exit(1);
    }
    return mat;
  }  // end of loadMatrix()



  private float[] getValues(String line)
  /* format:
        <double>  <double>  ....
     either 2 integers on first line, or col number of floats after that
  */
  {
    String[] tokens = line.split("\\s+");
    float[] vals = new float[tokens.length];
    for (int i=0; i < tokens.length; i++) {
      try {
        vals[i] = Float.parseFloat(tokens[i]); 
      }
      catch (NumberFormatException e)
      { System.out.println("Error extracting values from a matrix row");  
        break;
      }
    }
    return vals;
  }  // end of getImageInfo()



// -----------------adjusting BM attributes & gray disparity image ---------------------------


  public BufferedImage changeBM(String attribute, int val)
  /* The disparity map is created using the fast block-matching stereo 
     algorithm implemented by cvFindStereoCorrespondenceBM(); see OpenCV p.444

     Update the BM attributes, and then recalculate the disparity
     (if the rest of the calibration processing has been completed).
     The Slider title strings used here match those used in
     DepthViewer. This method is called from DepthViewer 
  */
  {
    boolean isChanged = true;

    if (attribute.equals("Prefilter Size")) {
      if (val%2 ==0) {
        System.out.println("Even prefilter sizes not allowed; using " + (val+1));
         val++;
      }
      bmState.preFilterSize(val);
    } 
    else if (attribute.equals("Prefilter Cap"))
      bmState.preFilterCap(val);
    else if (attribute.equals("SAD Window Size")) {
      int maxDim = (IM_WIDTH < IM_HEIGHT) ? IM_WIDTH : IM_HEIGHT;
      if (val >= maxDim) {
        System.out.println("SAD size must be smaller than smallest image dimension; changing val to " + (maxDim-1));
        val = maxDim-1;
      }
      if (val%2 ==0) {
        System.out.println("Even SAD sizes not allowed; using " + (val+1));
         val++;
      }
      bmState.SADWindowSize(val);
    } 
    else if (attribute.equals("Min Disparity"))
      bmState.minDisparity(val);
    else if (attribute.equals("No. of Disparities")) {
      if (val%16 != 0) {
        System.out.println("No of disparities must be x16; using " + 
                (val - val%16));
         val -= val%16;
      }
      // System.out.println("Disparities num: " + val);
      bmState.numberOfDisparities(val);
    } 
    else if (attribute.equals("Uniq Ratio"))
      bmState.uniquenessRatio(val);
    else if (attribute.equals("Texture Thresh"))
      bmState.textureThreshold(val);
    else if (attribute.equals("Speckle Size"))
      bmState.speckleWindowSize(val);
    else if (attribute.equals("Speckle Range"))
      bmState.speckleRange(val);
    else {
      System.out.println("Did not recognize BM attribute :\"" + attribute + "\"");
      isChanged = false;
    }

    if (isChanged && imagesRectified != null) {    // recalculate disparity maps
      normalizedDisp = createDisparityMaps(imagesRectified);
      return gDispMap.getBufferedImage();
    }
    else
      return null;
  }  // end of changeBM()


  public BufferedImage getGDispMap()
  {  
    if (gDispMap == null)
      return null;
    else 
      return gDispMap.getBufferedImage(); 
  }  // end of getGDispMap()


}  // end of DepthCalc class