
// ImageUtils.java
// Andrew Davison, September 2012, ad@fivedots.coe.psu.ac.th

/* A collection of useful image manipulation methods.
*/

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;


public class ImageUtils
{
  private static final int SCALE_BRIGHTNESS = 10;   // reduces brightness size


  private ImageUtils()
  {}


  public static BufferedImage loadImage(String fnm)
  // load the image stored in fnm
  {
    BufferedImage image = null;
    try {
      image = ImageIO.read(new File(fnm));
      System.out.println("Loaded " + fnm);
    }
    catch (IOException e) {
      System.out.println("Could not load " + fnm);
      System.exit(1);
    }
    return image;
  }  // end of loadImage()



  public static BufferedImage[] split(BufferedImage im, int divs)
  // split the image into (divs*divs) subimages stored in an array
  {
    // test that the image's width and height are suitable for splitting
    int imWidth = im.getWidth();
    if (imWidth % divs != 0) {
      System.out.println("Width of image (" + imWidth + 
                                       ") is not a multiple of " + divs);
      System.exit(1);
    }
    int partWidth = imWidth / divs;

    int imHeight = im.getHeight();
    if (imHeight % divs != 0) {
      System.out.println("Height of image (" + imHeight + 
                                      ") is not a multiple of " + divs);
      System.exit(1);
    }
    int partHeight = imHeight / divs;
    
    int numParts = divs*divs;
    BufferedImage[] imParts = new BufferedImage[numParts];
    int idx = 0;
    for (int y=0; y < divs; y++)
      for (int x=0; x < divs; x++) {
        imParts[idx] = im.getSubimage(x*partWidth, y*partHeight, partWidth, partHeight);
        idx++;
      }
    return imParts;
  }  // end of split()



  public static byte[] imToBytes(int idx, BufferedImage im)
  // convert the BufferedImage into a byte array
  {
    byte[] imBytes = null;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(im, "jpg", baos);
      baos.flush();
      
      imBytes = baos.toByteArray();
      baos.close();
    }
    catch(IOException e)
    {  System.out.println("Could not convert part " + idx + " to bytes");  }

    return imBytes;
  }  // end of imToBytes



  public static int getAvgBrightness(BufferedImage image) 
  /* returns average brightness of the gray-version of the image
     Based on getAverageBrightness() code described in 
        http://mindmeat.blogspot.com/2008/07/java-image-comparison.html
  */
  {
    BufferedImage im = toGray(image);
    Raster r = im.getData();     //  rectangular array of pixels
    int rWidth = r.getWidth();
    int rHeight = r.getHeight();
    int minX = r.getMinX();
    int minY = r.getMinY();

    // sum grayscales
    int total = 0;
    for (int y = 0; y < rHeight; y++) {
      for (int x = 0; x < rWidth; x++)
        total += r.getSample(minX+x, minY+y, 0);   // band == 0 for grayscale
    }
    return (int)(total / ((rWidth/SCALE_BRIGHTNESS) * 
                          (rHeight/SCALE_BRIGHTNESS)) );
  }  // end of getAvgBrightness()



  public static BufferedImage toGray(BufferedImage im)
  // convert a (colored) image to grayscale
  {
    BufferedImage grayIm = new BufferedImage(im.getWidth(), im.getHeight(),  
                                                          BufferedImage.TYPE_BYTE_GRAY);  
    Graphics g = grayIm.getGraphics();  
    g.drawImage(im, 0, 0, null);  
    g.dispose();  
    return grayIm;
  }  // end of toGray()



  public static BufferedImage bytesToIm(int idx, byte[] imBytes)
  // convert byte array to a BufferedImage
  {
    BufferedImage im = null;
    try {
      InputStream in = new ByteArrayInputStream(imBytes);
      im = ImageIO.read(in);
    }
    catch(IOException e)
    {  System.out.println("Conversion of part " + idx + " to an image failed");  }
    return im;
  }  // end of bytesToIm()



  public static BufferedImage join(BufferedImage[] imParts, int divs)
  /* combine all the subimages in imParts into a single image.
     The parts are numbered row-by-row, top-to-bottom, when they are
     divs row and divs columns. I assume that all the image parts have
     the same dimensions as the first one in the array.
  */
  {
    if (imParts.length == 0) {
      System.out.println("No parts to combine");
      return null;
    }

    if (imParts[0] == null) {
      System.out.println("Image part is empty");
      return null;
    }

    int partWidth = imParts[0].getWidth();
    int partHeight = imParts[0].getHeight();
    if ((partWidth == 0) || (partHeight == 0)) {
      System.out.println("Error in image part dimensions");
      return null;
    }

    BufferedImage im = new BufferedImage( partWidth*divs, partHeight*divs,
                                          BufferedImage.TYPE_INT_RGB);
    Graphics g = im.getGraphics();
    for (int i=0; i < imParts.length; i++) {
      if (imParts[i] == null)
        System.out.println("No data found for part " + i);
      else {
        int xCoord = (i%divs)*partWidth;
        int yCoord = (i/divs)*partHeight;
        g.drawImage(imParts[i], xCoord, yCoord, null);
      }
    }
    g.dispose();

    return im;
  }  // end of join()




  public static BufferedImage scale(BufferedImage im, double scale)
  // return a scaled version of the image
  {
    if(im == null)
      return null;

    if (scale == 1)  // no change
      return im;

    int newWidth = (int) Math.round(im.getWidth()*scale);
    int newHeight = (int) Math.round(im.getHeight()*scale);

    BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2d = result.createGraphics();

    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                         RenderingHints.VALUE_INTERPOLATION_BICUBIC);   // best quality
    // g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
    //                   RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                         RenderingHints.VALUE_RENDER_QUALITY);
    
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    
    g2d.drawImage(im, 0, 0, newWidth, newHeight, null);
    g2d.dispose();

    return result;
  }  // end of scale()



  public static void saveImage(BufferedImage im, String fnm)
  // save the image into the JPG file called fnm
  {
    try {
      ImageIO.write(im, "jpg", new File(fnm));
      System.out.println("Saved image to " + fnm); 
    }
    catch(IOException e)
    {  System.out.println("Could not save image to " + fnm);  }

    System.out.println("----------------------------");
  }  // end of saveImage()

}  // end of ImageUtils class