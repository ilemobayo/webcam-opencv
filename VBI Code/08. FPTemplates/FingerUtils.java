
// FingerUtils.java
// Andrew Davison, February 2013, ad@fivedots.coe.psu.ac.th

/* The fingerprint analysis methods are from the CFingerPrint class in the
  Biometric SDK, Version 1.3 (http://sourceforge.net/projects/biometricsdk/)
  by Scott Johnston, 2005, moleisking@googlemail.com:
     * binarize()
     * buildTemplate()
     * thinningHilditch()
     * thinningHitAndMiss()
     * getOrigin()
     * match()

  I've reformatted his code slightly, and added some extra comments.

  The rest of the methods are for loading and saving the fingerprint image,
  the template text data, and the labelled fingerprint image.

  The template contains minutiae data for the fingerprint on ridge endings and bifurcations
  and the print's core.
*/


import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;



public class FingerUtils
{
  // template size must always be 1 larger than a number divisible by 6
  private final static int MAX_SIZE = 601;

  // max distance between two points to count as a match
  private final static int MAX_DIST = 10;

  // max rotation between two points to count as a match
  private final static int MAX_ROT = 10;    // in degrees

  private final static int SEARCH_RADIUS = 1;


  // directory and file constants
  public static final String PRINT_DIR = "prints/";
  public static final String LABEL_EXT = "Labelled.png";
  public static final String TEMPLATE_EXT = "Template.txt";




  public static byte[][] binarize(BufferedImage im)
  // from CFingerPrint, by Scott Johnston
  // convert the image into a 2D byte array of 1's and 0's
  {
    int imWidth = im.getWidth();
    int imHeight = im.getHeight();
    byte[][] skel = new byte[imWidth][imHeight];

    for (int i = 0; i < imWidth; i++) {
      for (int j = 0; j < imHeight; j++) {
        Color c = new Color(im.getRGB(i, j));
        if ((c.getBlue() < 128) && (c.getRed() < 128) && (c.getGreen() < 128))
          skel[i][j] = 1;     // if color not white
        else
          skel[i][j] = 0;     // if color is white
      }
    }

    // set edges to 0
    for (int i = 0; i < imWidth; i++) {
      skel[i][0] = 0;
      skel[i][imHeight-1] = 0;
    }
    for (int j = 0; j < imHeight; j++) {
      skel[0][j] = 0;
      skel[imWidth-1][j] = 0;
    }

    return skel;
  }  // end of binarize()



  // -----------------------------------------------------------------------------


  public static double[] buildTemplate(byte[][] skel, int imWidth, int imHeight)
  // from CFingerPrint, by Scott Johnston
  /*  The template contains minutiae data for the fingerprint on ridge endings 
      and bifurcations and the print's core.

    The template array is formatted according to the NIST ISO standard:
      * The 0th element in the size.
      * The next 6 elements are: (originx, originy, 0, 0, 0 ,0)
     The format thereafter is (x, y, radius, degree, number-of-ends, resultant-degree)

    Later, number-of-ends == 1 will be labelled as a "ridge end", 
    number-of-ends > 1 will be labeled as a "ridge bifurcation/split"
  */
  { double x = 0;
    double y = 0;
    double r = 0;    // radius
    double d = 0;    // degree
    double tmplt[] = new double[MAX_SIZE];

    // skeletonization -- thin out the lines in skel[][] twice
    thinningHilditch(skel, imWidth, imHeight);
    thinningHitAndMiss(skel, imWidth, imHeight);
    thinningHilditch(skel, imWidth, imHeight);
    thinningHitAndMiss(skel, imWidth, imHeight);

    Point origin = getOrigin(skel, imWidth, imHeight);
    tmplt[1] = origin.x;
    tmplt[2] = origin.y;

    int c = 7;    // count
    int iPrev = 0;
    int jPrev = 0;

    boolean isFirst = true;

    /* start 5 units in to avoid detection of edges of fingerprint 
       and out of bound exceptions */
    for (int j = 5; j < imHeight-5; j++) {
      isFirst = true;
      for (int i = 5; i < imWidth-5; i++) {
        if ((c < MAX_SIZE) && (skel[i][j] == 1) &&
            (i != imWidth-1) && (i != 0) &&
            (j != imHeight-1) && (j != 0)) {
          /* Must not capture first and last feature because those are 
             the edges of the fingerprint
             and will provide no value to the template.
          */
          if (isFirst == true) {
            isFirst = false;
            // check to see if previous item in array was also end
            if ((c > 7) && ((tmplt[c-6] + origin.x) == iPrev) &&
                           ((tmplt[c-5] + origin.y) == jPrev)) {
              // delete previous feature
              tmplt[c--] = 0; tmplt[c--] = 0;
              tmplt[c--] = 0; tmplt[c--] = 0;
              tmplt[c--] = 0; tmplt[c--] = 0;
            }
          }
          else {
            int tc = 0;
            for (int m = -SEARCH_RADIUS; m <= SEARCH_RADIUS; m++) {
              for (int n = -SEARCH_RADIUS; n <= SEARCH_RADIUS; n++) {
                if ((m == SEARCH_RADIUS) || (m == -SEARCH_RADIUS) ||
                    (n == SEARCH_RADIUS) || (n == -SEARCH_RADIUS)) {
                  if (skel[i + m][j + n] == 1)
                    tc++;
                }
              }
            }

            // calculate parameters necessary for the template
            if ((tc == 1) || (tc == 3)) {
              x = i - origin.x;     // calculate (x,y) relative to origin
              y = j - origin.y;
              r = Math.hypot(x, y);    // radius
              if ((x > 0) && (y > 0))
                d = Math.atan(y/x);    // degree
              else if ((x < 0) && (y > 0))
                d = Math.atan(y/x) - Math.PI;
              else if ((x < 0) && (y < 0))
                d = Math.PI + Math.atan(y/x);
              else if ((x > 0) && (y < 0))
                d = 2*Math.PI + Math.atan(y/x);
            }

            // check to see if point has already been captured
            boolean xFound = false;
            boolean yFound = false;
            for (int m = 7; m <= c; m = m+6) {
              if (tmplt[m+4] == 3) {
                if (Math.abs( Math.abs((int)tmplt[m]) - Math.abs(x)) < 4)
                  xFound = true;
                if (Math.abs( Math.abs((int)tmplt[m+1]) - Math.abs(y)) < 4)
                  yFound = true;
              }
            }

            // 1 surrounding 1's
            if ((tc == 1) && (c <= MAX_SIZE-6) && (x != 0) && (y != 0) &&
                ((xFound == false) || (yFound == false))) {

              if (skel[i-1][j+1] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;      // radius, degree
                tmplt[c++] = 1; tmplt[c++] = 135;    // number-of-ends, resultant-degree
              }
              else if (skel[i][j+1] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;
                tmplt[c++] = 1; tmplt[c++] = 90;
              }
              else if (skel[i+1][j+1] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;
                tmplt[c++] = 1; tmplt[c++] = 45;
              }
              else if (skel[i+1][j] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;
                tmplt[c++] = 1; tmplt[c++] = 0;
              } 
              else if (skel[i+1][j-1] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;
                tmplt[c++] = 1; tmplt[c++] = 315;
              }
              else if (skel[i][j-1] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;
                tmplt[c++] = 1; tmplt[c++] = 270;
              }
              else if (skel[i-1][j-1] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;
                tmplt[c++] = 1; tmplt[c++] = 225;
              }
              else if (skel[i-1][ j] == 1) {
                tmplt[c++] = x; tmplt[c++] = y;
                tmplt[c++] = r; tmplt[c++] = d;
                tmplt[c++] = 1; tmplt[c++] = 180;
              }
            }
            else if ((tc >= 3) && (c <= MAX_SIZE - 6) &&
                     (x != 0) && (y != 0) && 
                     ((xFound == false) || (yFound == false))) {
              // 3 surrounding 1's
              tmplt[c++] = x; tmplt[c++] = y;
              tmplt[c++] = r; tmplt[c++] = d;
              tmplt[c++] = 3; tmplt[c++] = 0;    // number-of-ends, resultant-degree
            }
          }

          iPrev = i;
          jPrev = j;
        }
      }
    }
    tmplt[0] = c;    // store count as first value in template
    return tmplt;
  } // end of buildTemplate()




  private static void thinningHilditch(byte[][] skel, int imWidth, int imHeight)
  // from CFingerPrint, by Scott Johnston
  /* implements Hilditch's Algorithm for skeletonization  
     e.g. see http://jeff.cs.mcgill.ca/~godfried/teaching/projects97/azar/skeleton.html
     and http://fourier.eng.hmc.edu/e161/lectures/morphology/node2.html
  */
  {
    boolean isChanged = true;
    boolean mbool = true;

    while (isChanged) {
      isChanged = false;
      for (int i = 2; i < imWidth-1; i++) {
        for (int j = 2; j < imHeight-1; j++) {
          if (skel[i][j] == 1) {
            int c = 0;
            // count surrounding 1's
            // a) Make sure pixel 1, has 2 to 6 (inclusive) neighbors
            if (skel[i][j+1] == 1)
              c++;
            if (skel[i+1][j+1] == 1)
              c++;
            if (skel[i+1][j] == 1)
              c++;
            if (skel[i+1][j-1] == 1)
              c++;
            if (skel[i][j-1] == 1)
              c++;
            if (skel[i-1][j-1] == 1)
              c++;
            if (skel[i-1][j] == 1)
              c++;
            if (skel[i-1][j+1] == 1)
              c++;

            if ((c >= 2) && (c <= 6)) {
              c = 0;
              /* b) starting from position 2, move clockwise until position 9,
                    counting the number of 0 to 1 transitions.  
                    This should be equal to 1.
              */
              if ((skel[i-1][j+1] == 0) && (skel[i][j+1] == 1))
                c++;
              if ((skel[i][j+1] == 0) && (skel[i+1][j+1] == 1))
                c++;
              if ((skel[i+1][j+1] == 0) && (skel[i+1][j] == 1))
                c++;
              if ((skel[i+1][j] == 0) && (skel[i+1][j-1] == 1))
                c++;
              if ((skel[i+1][j-1] == 0) && (skel[i][j-1] == 1))
                c++;
              if ((skel[i][j-1] == 0) && (skel[i-1][j-1] == 1))
                c++;
              if ((skel[i-1][j-1] == 0) && (skel[i-1][j] == 1))
                c++;
              if ((skel[i-1][j] == 0) && (skel[i-1][j+1] == 1))
                c++;

              if (c == 1) {
                c = 0;
                if (mbool == true) {
                  // c) 2*4*6 == 0  (i.e. either 2,4, or 6 is off)
                  if ((skel[i][j+1] * skel[i+1][j] * skel[i+1][j-1]) == 0) {
                    // d) 4*6*8 == 0 
                    if ((skel[i+1][j] * skel[i+1][j-1] * skel[i-1][j]) == 0) {
                      skel[i][j] = 0;
                      isChanged = true;
                    }
                  }
                  mbool = false;
                }
                else {
                  // c') 2*6*8 == 0  (i.e. either 2,6, or 8 is off)
                  if ((skel[i][j+1] * skel[i+1][j-1] * skel[i-1][j]) == 0) {
                    // d') 2*4*8 == 0 
                    if ((skel[i][j+1] * skel[i+1][j] * skel[i-1][j]) == 0) {
                      skel[i][j] = 0;
                      isChanged = true;
                    }
                  }
                  mbool = true;
                }
              }
            }
          }
        }
      }
    }
  } // end of thinningHilditch()



  private static void thinningHitAndMiss(byte[][] skel, int imWidth, int imHeight)
  // from CFingerPrint, by Scott Johnston
  /* implements the Hit-and-Miss Algorithm for skeletonization  
     e.g. see http://fourier.eng.hmc.edu/e161/lectures/morphology/node4.html
     Look for lines/groups of 1's:
       111    000
       XXX or XXX  and so on ('X' means doesn't matter)
       000    111
     If any of these patterns are found then change the center value to a 0
  */
  { int c = 1;
    while (c != 0) {
      c = 0;
      for (int i = 1; i < imWidth; i++) {
        for (int j = 1; j < imHeight; j++) {
          if ((skel[i][j] == 1) && (i != 0) && (j != imHeight-1) &&
                                   (j != 0) && (i != imWidth-1)) {
            if ((skel[i-1][j-1] == 1) && (skel[i][j-1] == 1) && (skel[i+1][j-1] == 1) &&
                (skel[i-1][j+1] == 0) && (skel[i][j+1] == 0) && (skel[i+1][j+1] == 0)) {
              // 111 
              // XXX
              // 000
              skel[i][j] = 0;
              c++;
            }
            else if ((skel[i-1][j+1] == 1) && (skel[i][j+1] == 1) && (skel[i+1][j+1] == 1) &&
                     (skel[i-1][j-1] == 0) && (skel[i][j-1] == 0) && (skel[i+1][j-1] == 0)) {
              // 000
              // XXX
              // 111
              skel[i][j] = 0;
              c++;
            }
            else if ((skel[i-1][j] == 1) && (skel[i-1][j-1] == 1) && (skel[i-1][j+1] == 1) && 
                     (skel[i+1][j] == 0) && (skel[i+1][j+1] == 0) && (skel[i+1][j-1] == 0)) {
              // 1X0
              // 1X0
              // 1X0
              skel[i][j] = 0;
              c++;
            }
            else if ((skel[i+1][j] == 1) && (skel[i+1][j-1] == 1) && (skel[i+1][j+1] == 1) && 
                     (skel[i-1][j] == 0) && (skel[i-1][j+1] == 0) && (skel[i-1][j-1] == 0)) {
              // 0X1
              // 0X1
              // 0X1
              skel[i][j] = 0;
              c++;
            }
            else if ((skel[i-1][j] == 1) && 
                     (skel[i][j-1] == 1) && (skel[i][j+1] == 0) && 
                     (skel[i+1][j+1] == 0) && (skel[i+1][j] == 0)) {
              // X1X
              // 110
              // X00
              skel[i][j] = 0; // 1's at the top-left
              c++;
            }
            else if ((skel[i-1][j] == 1) && 
                     (skel[i][j+1] == 1) && (skel[i][j-1] == 0) && 
                     (skel[i+1][j-1] == 0) && (skel[i+1][j] == 0)) {
              // X00
              // 110
              // X1X
              skel[i][j] = 0;  // 1's at the bottom-left
              c++;
            }
            else if ((skel[i][j+1] == 1) && (skel[i+1][j] == 1) &&
                     (skel[i-1][j] == 0) && (skel[i-1][j-1] == 0) &&
                     (skel[i][j-1] == 0)) {
              // 00X
              // 011
              // X1X
              skel[i][j] = 0; // 1's at the bottom-right
              c++;
            }
            else if ((skel[i][j-1] == 1) && (skel[i+1][j] == 1) &&
                     (skel[i-1][j] == 0) && (skel[i-1][j+1] == 0) &&
                     (skel[i][j+1] == 0)) {
              // X1X
              // 011
              // 00X
              skel[i][j] = 0;   // 1's at the top-right
              c++;
            }
          }
        }
      }
    }
  } // end of thinningHitAndMiss()




  private static Point getOrigin(byte[][] skel, int imWidth, int imHeight)
  // from CFingerPrint, by Scott Johnston
  /* Often called the core -- the point of maximum ridge line 
     curvature (gradient change), or the north-most loop or whorl. 
     The gradient represents the steepness 
     (greatest rate of increase) and direction of that change.
     e.g. see http://en.wikipedia.org/wiki/Gradient

     Also called a 'directional field'; see 
        "Fingerprint Classification by Directional Fields" by Sen Wang et al.
        http://aya.technion.ac.il/projects/2005winter/Fingerprint1.pdf
     which uses a similar but more complex approach.
  */
  { Point pt = new Point();
    double gradCurr = 0;
    double gradPrev = 0;

    double gradBigChange = 0;
    double gradChange = 0;

    double gradBigDist = 0;
    double gradDist = 0;

    double xPrev = 0;
    double yPrev = 0;

    for (int j = 50; j <= imHeight-50; j++) {
      for (int i = 50; i <= imWidth-50; i++) {
        if (skel[i][j] == 1) {
          // count surrounding pixels
          int tc,x1, y1, x2, y2;
          tc = x1 = y1 = x2 = y2 = 0;

          // find surrounding 1's
          for (int m = -SEARCH_RADIUS; m <= SEARCH_RADIUS; m++) {
            for (int n = -SEARCH_RADIUS; n <= SEARCH_RADIUS; n++) {
              if ((m == SEARCH_RADIUS) || (m == -SEARCH_RADIUS) ||
                  (n == SEARCH_RADIUS) || (n == -SEARCH_RADIUS)) {
                if (skel[i+m][j+n] == 1) {
                  tc++;
                  if (tc == 1) {
                    x1 = i+m;
                    y1 = j+n;
                  }
                  if (tc == 2) {
                    x2 = i+m;
                    y2 = j+n;
                  }
                }
              }
            }
          }

          // does the hard work of finding the greatest change in gradient
          if (tc == 2) {
            if ((x2 - x1) > 0) {
              gradCurr = (y2 - y1)/(x2 - x1);
              // check to see gradient change by at least 270 degrees
              if ((gradCurr > 0) && (gradPrev < 0)) {
                gradChange = Math.abs(gradCurr) + Math.abs(gradPrev);
                gradDist = Math.abs(i) - Math.abs(xPrev);
                if (gradBigChange < gradChange) {
                  if (gradBigDist < gradDist) {
                    gradBigChange = gradChange;
                    gradBigDist = gradDist;
                    pt.x = i; 
                    pt.y = j; 
                  }
                }
                break;
              }
              // reset varibles for new checks
              gradPrev = gradCurr;
              gradCurr = 0;
              xPrev = i;
              yPrev = j;
            }
          }
        }
      }
    }
    return pt;
  }  // end of getOrigin()




  public static int match(double[] tmplt1, double[] tmplt2, int threshold, boolean isFastMatch)
  // from CFingerPrint, by Scott Johnston
  /* The two templates are compared by pairing up their minutiae
     (ridge endings and ridge bifurcations/splits) which are recorded as the
     number-of-ends value in (x, y, r, degree, number-of-ends, resultant-degree)
  */
  {
    double numMatches = 0;
    double numPoints = (tmplt1[0]-6)/6;     // the number of minutiae
    double bestMatches = 0;
    boolean foundPt;

    for (int rot = -MAX_ROT; rot <= MAX_ROT; rot++) {   // try different rotations
      for (int i = 7; i < tmplt1[0]-6; i=i+6) {   // cycle through first template
        foundPt = false;
        for (int j = 7; j < tmplt2[0]-6; j=j+6) {  // cycle through second template
          if (foundPt == false) {
            // compare points in two templates
            double x2 = tmplt1[i];         // a point in the first template
            double y2 = tmplt1[i+1];

            double radius = tmplt2[j+2];   // get the point in the second template, using its polar coords
            double degree = tmplt2[j+3];

            // rotate and then calculate x-coord difference from first template
            double x1 = radius * Math.cos(degree + (rot * Math.PI/180));
            int xDiff = Math.abs((int)x2 + (int)(-x1));

            // rotate and then calculate y-coord difference from first template
            double y1 = radius * Math.sin(degree + (rot * Math.PI/180));
            int yDiff = Math.abs((int)y2 + (int)(-y1));

            // if the difference is small enough
            if ((xDiff < MAX_DIST) && (yDiff < MAX_DIST)) {
              if (tmplt1[i+4] == tmplt2[j+4]) {  // check if the 2 points are the same kind of minutiae
                numMatches++;
                foundPt = true;
              }
            }
          }
        }
      }

      if ((((numMatches/numPoints)*100) >= threshold) && (isFastMatch))  
        // found enough matches; return score if isFastMatch == true
        return (int)((numMatches/numPoints)*100);
      else {  // try all the other rotations before returning the score
        if (numMatches > bestMatches)
          bestMatches = numMatches;
        numMatches = 0;   // reset match counter
      } 
    }
    return (int)((bestMatches/numPoints)*100);
  } // end of match()




  // ------------------------ file loading / saving ----------------------------

  /* for loading and saving the fingerprint image,
     the template text data, and the labelled fingerprint image.
  */


  public static String extractPrintName(String pathNm)
  // pull out printname from file path name
  {
    int slashPos = pathNm.lastIndexOf('/');
    String fnm = (slashPos == -1) ? pathNm : pathNm.substring(slashPos+1);  // remove directories

    int dotPos = fnm.lastIndexOf('.');
    String printName = (dotPos == -1) ? fnm : fnm.substring(0, dotPos);  // remove extension
    return printName;
  }  // end of extractPrintName()



  public static boolean hasLabel(String printName)
  {  return new File(PRINT_DIR + printName + LABEL_EXT).exists();  }


  public static boolean hasTemplate(String printName)
  {  return new File(PRINT_DIR + printName + TEMPLATE_EXT).exists();  }



  public static BufferedImage loadPrint(String printName)
  {  return loadImage(PRINT_DIR + printName + ".png");  }


  public static BufferedImage loadLabel(String printName)
  {  return loadImage(PRINT_DIR + printName + LABEL_EXT);  }


  private static BufferedImage loadImage(String fnm)
  {
    BufferedImage im = null;
    System.out.println("Loading " + fnm);
    try {
      im = ImageIO.read(new File(fnm));
    }
    catch (Exception ex) {
      System.out.println("Cannot load image");
      System.exit(1);
    }
    return im;
  }  // end of loadImage()



  public static void savePrint(String printName, BufferedImage im)
  {  saveImage(PRINT_DIR + printName + ".png", im);  }


  public static void saveLabel(String printName, BufferedImage im)
  {  saveImage(PRINT_DIR + printName + LABEL_EXT, im);  }


  private static void saveImage(String fnm, BufferedImage im)
  {
    System.out.println("Saving image to " + fnm);
    try {
      ImageIO.write(im, "png", new File(fnm));
    }
    catch (IOException e)
    {  System.out.println("Unable to save");   }
  }  // end of saveImage()




  public static String templateToString(double[] tmplt)
  /* Array format:
       * The 0th element in the size of the array.
       * The next 6 elements are: originx, originy, 0, 0, 0 ,0   
     The format thereafter is (x, y, radius, degree, number-of-ends, resultant-degree)

     Convert each 6 elements to a single line of text, ending with a newline
  */
  {
    StringBuilder sb = new StringBuilder();
    sb.append(tmplt[0] + "\n");     // size of data is on its own line

    int count = 0;
    for (int i = 1; i < tmplt[0]; i++) {
      sb.append( tmplt[i] + "  ");
      count++;
      if (count%6 == 0)
        sb.append("\n");
    }
    return sb.toString();
  }  // end of templateToString()



  public static double[] loadTemplate(String printName)
 // load the template data from a file
  {
    double[] tmplt = null;
    int size = 0;
    int count = 0;
    boolean isSizeLine = true;

    String fnm = PRINT_DIR + printName + TEMPLATE_EXT;
    System.out.println("Loading template from " + fnm);

    try {
      BufferedReader input =  new BufferedReader(new FileReader(fnm));
      try {
        String line = null; 
        while ((line = input.readLine()) != null) {
          if (isSizeLine) {
            size = (int)getNumber(line);
            if (size == 0){ 
              System.out.println("Incorrect size format: " + line); 
              return null;
            }
            tmplt = new double[size];
            tmplt[count++] = size;
            isSizeLine = false;
          }
          else    // x, y, radius, degree, number-of-ends, resultant-degree
            count = loadEntry(line, tmplt, count, size);
        }
      }
      finally {
        input.close();
      }
    }
    catch (IOException e)
    {  System.out.println("Could not load template");  }

    return tmplt;
  }  // end of loadTemplate()



  private static int loadEntry(String line, double[] tmplt, int count, int size)
  /* an entry has the format:
        x  y  radius  degree  number-of-ends  resultant-degree
     It becomes 6 doubles in the template array.
  */
  {
    String[] tokens = line.split("\\s+");
    if (tokens.length != 6) {
      System.out.println("Wrong number of arguments in template entry");
      return count;
    }
    
    if ((count+6) > size) {
      System.out.println("Too many elements; ignoring template entry");
      return count;
    }

    for (String token : tokens)
       tmplt[count++] = getNumber(token);
    return count;
  }  // end of loadEntry()



  private static double getNumber(String token)
  // extract a number or return 0
  { double num = 0;
    try {
      num = Double.parseDouble(token);
    }
    catch (NumberFormatException ex){ 
      System.out.println("Incorrect format for " + token); 
    }
    return num;
  }  // end of getNumber()





  public static void saveTemplate(String printName, double[] tmplt)
  {
    String fnm = PRINT_DIR + printName + TEMPLATE_EXT;
    System.out.println("Saving template to " + fnm);
    System.out.println("Size: " + (int)tmplt[0]);

    try {
      PrintWriter out = new PrintWriter(new FileWriter(fnm));
    
      out.println(tmplt[0]);     // size of data
      int count = 0;
      for (int i = 1; i < tmplt[0]; i++) {    // was tmplt.length
        out.print( tmplt[i] + "  ");
        count++;
        if (count%6 == 0)
          out.println();
      }
      out.close();
    } 
    catch (IOException e)
    {  System.out.println("Could not save template");  }
  }  // end of savetemplate()



} // end of FingerUtils class
