
// FingerPrint.java
// Andrew Davison, February 2013, ad@fivedots.coe.psu.ac.th

/*  A Fingerprint object manages four pieces of data:
       * the fingerprint name

       * the fingerprint image (saved in a file using the name + ".png")

       * the fingerprint template   
           -- this is minutiae data for the fingerprint on ridge endings and bifurcations
              and the print's core
           -- I build it using code from Scott Johnston's Biometric SDK
              (http://sourceforge.net/projects/biometricsdk/)

       * a labelled fingerprint image which combines the fingerprint and template data

    The fingerprint image, template and labelled image are stored in three files.
    If the fingerprint image is stored in XXX.png (in prints/), then the template
    information will be in XXXTemplate.txt, and the labelled image in XXXLabelled.png.
*/


import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.lang.Exception;



public class FingerPrint
{
  private static final int LINE_LEN = 5;

  private String printName; 
  private double[] template;         
  private BufferedImage labelIm;


  public FingerPrint(String pName, BufferedImage im) 
  { 
    printName = pName;
    if (im == null)
      return;

    int imWidth = im.getWidth();
    int imHeight = im.getHeight();
    FingerUtils.savePrint(printName, im);   // save the fingerprint to a file

    // create the template and labelled image
    byte[][] skel = FingerUtils.binarize(im);
    template = FingerUtils.buildTemplate(skel, imWidth, imHeight);
    labelIm = labelImage(skel, template, imWidth, imHeight);

    // save the template and labelled image in files
    System.out.println();
    FingerUtils.saveTemplate(printName, template);
    FingerUtils.saveLabel(printName, labelIm);
  }  // end of FingerPrint()




  private BufferedImage labelImage(byte[][] skel, double[] tmplt,
                                              int imWidth, int imHeight)
  /* The labelled fingerprint image combines the fingerprint and template data.

     The fingerprint ridges are drawn in blue.
 
     The template's minutiae data is drawn in the following ways:
         - bifurcations: red boxes;
         - ridge ends: green boxes;
         - the print's core: a red '+' drawn using lines.
  */
  {
    double xCore = tmplt[1];
    double yCore = tmplt[2];

    // draw the finger print
    BufferedImage im = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < imWidth; i++) {
      for (int j = 0; j < imHeight; j++) {
        if (skel[i][j] == 1)
          im.setRGB(i, j, Color.BLUE.getRGB());    // blue for ridge points
        else
          im.setRGB(i, j, Color.WHITE.getRGB());   // white for background
      }
    }

    /* draw a red box for each bifurcation (ridge split) and a green box for each
       ridge end */
    Graphics2D g2d = im.createGraphics();    // get a drawing context for the image

    for (int i = 7; i < tmplt[0]; i = i+6) {
      if (tmplt[i+4] > 1) {    // examine the "number-of-ends" field for each template entry
        g2d.setColor(Color.RED);   // ridge bifurcation
        g2d.drawRect((int)tmplt[i] + (int)xCore-3,
                     (int)tmplt[i+1] + (int)yCore-2, LINE_LEN, LINE_LEN);
      }
      else if (tmplt[i+4] == 1) {
        g2d.setColor(Color.GREEN);  // ridge end
        g2d.drawRect((int)tmplt[i] + (int)xCore-3,
                     (int)tmplt[i+1] + (int)yCore-2, LINE_LEN, LINE_LEN);
      }
    }

    // draw the print's center (core)
    g2d.setColor(Color.RED);
    int len = 2*LINE_LEN;
    g2d.drawLine((int)xCore-len, (int)yCore, (int)xCore+len, (int)yCore);   // x-axis
    g2d.drawLine((int)xCore, (int)yCore-len, (int)xCore, (int)yCore+len);   // y-axis

    return im;
  }  // end of labelImage()




  public BufferedImage getLabelledImage()
  {  return labelIm; }


  public double[] getTemplate()
  {  return template;  }


  public String getTemplateString()
  {  return FingerUtils.templateToString(template);  }


} // end of FingerPrint class
