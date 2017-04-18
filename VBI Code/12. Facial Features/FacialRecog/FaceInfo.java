
// FaceInfo.java
// Andrew Davison, May 2013, ad@fivedots.coe.psu.ac.th

/* This class maintains a list of face feature points which it
   extracts from an image when update() is called. When draw()
   is called, the points are drawn as lines and coloured dots.
   The lines are used to connect related feature points.

   The facial features atre obtained using 
   the FaceSDK API (http://luxand.com/facesdk/).
*/

import java.awt.*;
import java.io.*;
import java.awt.image.*;

import Luxand.*;
import Luxand.FSDK.*;



public class FaceInfo
{
  private static final String FACE_SDK_LICENSE = "D62bLQDLnB+iwCBPhaqkp6+8UAeqxePB7lYQqj+pS1xBrzmBKoA1TWH0B+xaBnTRRbjZqUhcRW5P91HExjnhktAeTgaZPDSh3+m6AzeQcPfLWUl7wyLIHigvB4ZaCWRyUS7GZkCxzdXb2eTMrNh10a0V4xt5+1/4l2/OkCnRIg8=";   
    // obtained from http://luxand.com/facesdk/; you need to get your own


  private Point[] featurePts;
  private boolean hasPoints = false;



  public FaceInfo()
  { 
    initFaceSDK();

    FSDK.SetFaceDetectionParameters(false, true, 384);
            // DetermineFaceRotationAngle == true needed for facial features detection

/*
    // report on core support
    int[] numThreadsRef = new int[1];
    if (FSDK.GetNumThreads(numThreadsRef) != FSDK.FSDKE_OK)
      System.out.println("Could not get number of processor cores");
    else 
      System.out.println("Number of processor cores: " + numThreadsRef[0]);
*/

    featurePts = new Point[FSDK.FSDK_FACIAL_FEATURE_COUNT];
    for (int i=0; i < FSDK.FSDK_FACIAL_FEATURE_COUNT; i++)
      featurePts[i] = new Point(0, 0);
  }  // end of FaceInfo()




  private void initFaceSDK()
  {
    try {
      if (FSDK.ActivateLibrary(FACE_SDK_LICENSE) != FSDK.FSDKE_OK){
        System.out.println("License error while activating FaceSDK"); 
        System.exit(1);
      }
    } 
    catch(UnsatisfiedLinkError e) {
      System.out.println("Could not link to FaceSDK library");
      System.exit(1);
    }  
      
    if (FSDK.Initialize() != FSDK.FSDKE_OK) {
      System.out.println("Could not initialize FaceSDK");
      System.exit(1);
    }
  }  // end of initFaceSDK()



  public void close()
  { FSDK.Finalize();  }



  // ------------------------- obtain face features ----------------------------


  public synchronized void update(BufferedImage im) 
  /* Convert image to FaceSDK format, find the face, and then 
     the facial features
  */
  { 
    // convert image to FaceSDK format
    HImage imHandle = new HImage();
    if (FSDK.LoadImageFromAWTImage(imHandle, im,
                 FSDK_IMAGEMODE.FSDK_IMAGE_COLOR_32BIT) != FSDK.FSDKE_OK) {
      System.out.println("Failed to create FaceSDK image");
      return;
    }

    // printImageSize(imHandle);

    // find face rectangle
    FSDK.TFacePosition.ByReference facePos = 
                                     new FSDK.TFacePosition.ByReference();
    if (FSDK.DetectFace(imHandle, facePos) != FSDK.FSDKE_OK) {
      System.out.println("Failed to find a face");
     return;
    }

    // printFaceSize(facePos);

    // extract facial features from face region
    FSDK_Features.ByReference facialFeatures = new FSDK_Features.ByReference();
    FSDK.DetectFacialFeaturesInRegion(imHandle, 
                 (FSDK.TFacePosition)facePos, facialFeatures);


    for (int i=0; i < FSDK.FSDK_FACIAL_FEATURE_COUNT; i++) {
      featurePts[i].x = facialFeatures.features[i].x;
      featurePts[i].y = facialFeatures.features[i].y;
      // System.out.print(" (" + featurePts[i].x + ", " + featurePts[i].y + ")");
    }
    // System.out.println();
    hasPoints = true;


    FSDK.FreeImage(imHandle);
  }  // end of update()



  private void printImageSize(HImage imHandle)
  // print the size of the FaceSDK version of the image
  {
    int imWidthRef[] = new int[1];
    int imHeightRef[] = new int[1];
    FSDK.GetImageWidth(imHandle, imWidthRef);
    FSDK.GetImageHeight(imHandle, imHeightRef);
    System.out.println("  (width, height): (" + imWidthRef[0] + ", " +
                                                imHeightRef[0] + ")");
  }  // end of printImageSize()



  private void printFaceSize(FSDK.TFacePosition.ByReference facePos)
  // print the face position and angle
  {
    int left = facePos.xc - facePos.w/2;
    int top = facePos.yc - facePos.w/2;    // no h
    System.out.println("   face (left, top): (" + left + ", " + top + "); angle: " + facePos.angle);
  }  // end of printFaceSize()



  public Point getPt(FeatureID id)
  {
    if (!hasPoints)
      return null;
    return new Point( featurePts[id.getIndex()]);
  }


  public boolean hasPoints()
  {   return hasPoints; }


  // -------------------------------- drawing -------------------------------

  public synchronized void draw(Graphics2D g2)
  // draw the face features using lines and dots
  {
    if (!hasPoints)
      return;

    // draw lines & blue points for the 10 face regions
    connectPoints(g2, FeatureID.leftEyeBrow(), false);
    connectPoints(g2, FeatureID.leftEye(), true);

    connectPoints(g2, FeatureID.rightEyeBrow(), false);
    connectPoints(g2, FeatureID.rightEye(), true);

    connectPoints(g2, FeatureID.nose(), true);

    connectPoints(g2, FeatureID.leftCheek(), false);
    connectPoints(g2, FeatureID.rightCheek(), false);

    connectPoints(g2, FeatureID.topLip(), true);
    connectPoints(g2, FeatureID.bottomLip(), true);

    connectPoints(g2, FeatureID.chin(), false);

    // draw  7 green dots for pupils, irises, nose tip
    g2.setColor(Color.GREEN);

    drawPoint(g2, FeatureID.LEFT_EYE);     // left pupil
    drawPoint(g2, FeatureID.LEFT_EYE_LEFT_IRIS_CORNER);      // left iris (left edge)
    drawPoint(g2, FeatureID.LEFT_EYE_RIGHT_IRIS_CORNER);     // left iris (right edge)

    drawPoint(g2, FeatureID.RIGHT_EYE);     // right pupil
    drawPoint(g2, FeatureID.RIGHT_EYE_LEFT_IRIS_CORNER);     // right iris  (left edge)
    drawPoint(g2, FeatureID.RIGHT_EYE_RIGHT_IRIS_CORNER);    // right iris  (right edge)

    drawPoint(g2, FeatureID.NOSE_TIP);      // nose tip
  }  // end of draw()



  private void connectPoints(Graphics2D g2, FeatureID[] ids, boolean isPolygon)
  // draw yellow lines between blue points
  {
    // draw yellow lines
    g2.setColor(Color.YELLOW);
    g2.setStroke(new BasicStroke(3));
    for(int i=0; i < ids.length-1; i++)
      drawLine(g2, ids[i], ids[i+1]);

    if (isPolygon)    // draw a line back to the first point
      drawLine(g2, ids[ids.length-1], ids[0]);

    // draw blue dots
    g2.setColor(Color.BLUE);
    for(int i=0; i < ids.length; i++)
      drawPoint(g2, ids[i]);

  }  // end of connectPoints()



  private void drawLine(Graphics2D g2, FeatureID fromID, FeatureID toID)
  // draw a line between the two feature IDs
  {
    Point fromPt = featurePts[fromID.getIndex()];
    Point toPt = featurePts[toID.getIndex()];
    g2.drawLine(fromPt.x, fromPt.y, toPt.x, toPt.y);
  }  // end of drawLine()



  private void drawPoint(Graphics2D g2, FeatureID id)
  // draw a point for the feature ID
  {
    Point pt = featurePts[id.getIndex()];
    g2.drawOval(pt.x-2, pt.y-2, 4, 4);
  }  // end of drawPoint()



}  // end of FaceInfo class
